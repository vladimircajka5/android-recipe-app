package com.siit.recipeapp.data.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.local.entity.SyncTombstoneEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FirestoreSyncManager {
    private interface TaskOperation {
        com.google.android.gms.tasks.Task<Void> run();
    }
    public interface Callback {
        void onComplete();
        void onError(@NonNull Exception exception);
    }

    private final Context context;
    private final RecipeDatabase database;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FirestoreSyncManager(Context context, RecipeDatabase database) {
        this.context = context.getApplicationContext();
        this.database = database;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void sync(String userId, Callback callback) {
        executor.execute(() -> {
            try {
                if (userId == null || userId.trim().isEmpty()) {
                    callback.onError(new IllegalArgumentException(
                            "A signed-in user is required."));
                    return;
                }

                if (!userId.equals(UserDataSession.currentOwnerId(context))) {
                    callback.onError(new IllegalStateException(
                            "Account changed before synchronization."));
                    return;
                }

                if (!isOnline()) {
                    callback.onComplete();
                    return;
                }

                pushPending(userId, callback);
            } catch (Exception error) {
                callback.onError(error);
            }
        });
    }

    private void pushPending(String userId, Callback callback) {
        List<PantryEntity> pantry = database.pantryDao().getPending(userId);
        List<ShoppingListEntity> lists = database.shoppingDao().getPendingLists(userId);
        List<ShoppingListItemEntity> items = database.shoppingDao().getPendingItems(userId);
        List<SyncTombstoneEntity> tombstones = database.syncDao().getTombstones(userId);


        com.google.android.gms.tasks.Task<Void> task = com.google.android.gms.tasks.Tasks.forResult(null);
        for (SyncTombstoneEntity tombstone : tombstones) {
            task = next(task, () -> firestore
                    .collection("users").document(userId)
                    .collection(tombstone.collection)
                    .document(tombstone.documentId)
                    .set(tombstoneMap(tombstone)));
            task = markAfter(task, () ->
                    database.syncDao().deleteSyncedTombstone(
                            tombstone.key,
                            userId,
                            tombstone.localRevision));
        }
        for (PantryEntity item : pantry) {
            task = next(task, () -> firestore
                    .collection("users").document(userId).collection("pantry")
                    .document(item.id).set(pantryMap(item)));
            task = markAfter(task, () ->
                    database.pantryDao().markSynced(
                            item.id,
                            userId,
                            item.localRevision
                    ));
        }
        for (ShoppingListEntity list : lists) {
            task = next(task, () -> firestore
                    .collection("users").document(userId).collection("shoppingLists")
                    .document(list.id).set(listMap(list)));
            task = markAfter(task, () ->
                    database.shoppingDao().markListSynced(
                            list.id,
                            userId,
                            list.localRevision
                    ));
        }
        for (ShoppingListItemEntity item : items) {
            task = next(task, () -> firestore
                    .collection("users").document(userId).collection("shoppingItems")
                    .document(item.id).set(itemMap(item)));
            task = markAfter(task, () ->
                    database.shoppingDao().markItemSynced(
                            item.id,
                            userId,
                            item.localRevision
                    ));
        }

        task.addOnSuccessListener(executor, ignored -> pullRemote(userId, callback))
                .addOnFailureListener(executor, callback::onError);
    }

    private com.google.android.gms.tasks.Task<Void> next(
            com.google.android.gms.tasks.Task<Void> previous, TaskOperation operation) {
        return previous.continueWithTask(executor, result -> {
            if (!result.isSuccessful()) {
                return com.google.android.gms.tasks.Tasks.forException(
                        result.getException() == null
                                ? new IllegalStateException("Synchronization failed")
                                : result.getException());
            }
            return operation.run();
        });
    }
    private com.google.android.gms.tasks.Task<Void> markAfter(
            com.google.android.gms.tasks.Task<Void> task, Runnable action) {
        return task.continueWithTask(executor, previous -> {
            if (!previous.isSuccessful()) {
                return com.google.android.gms.tasks.Tasks.forException(
                        previous.getException() == null
                                ? new IllegalStateException("Synchronization failed")
                                : previous.getException());
            }
            action.run();
            return com.google.android.gms.tasks.Tasks.forResult(null);
        });
    }

    private void pullRemote(String userId, Callback callback) {
        com.google.android.gms.tasks.Task<QuerySnapshot> pantryTask = firestore
                .collection("users").document(userId).collection("pantry").get();

        com.google.android.gms.tasks.Task<QuerySnapshot> listsTask = firestore
                .collection("users").document(userId).collection("shoppingLists").get();

        com.google.android.gms.tasks.Task<QuerySnapshot> itemsTask = firestore
                .collection("users").document(userId).collection("shoppingItems").get();

        com.google.android.gms.tasks.Tasks
                .whenAllSuccess(pantryTask, listsTask, itemsTask)
                .addOnSuccessListener(executor, results -> {
                    try {
                        database.runInTransaction(() -> {
                            if (!userId.equals(
                                    UserDataSession.currentOwnerId(context))) {
                                throw new IllegalStateException(
                                        "Account changed during synchronization.");
                            }

                            mergePantry(userId, (QuerySnapshot) results.get(0));
                            mergeLists(userId, (QuerySnapshot) results.get(1));
                            mergeItems(userId, (QuerySnapshot) results.get(2));
                        });
                    } catch (Exception error) {
                        callback.onError(error);
                        return;
                    }

                    callback.onComplete();
                })
                .addOnFailureListener(executor, callback::onError);
    }

    private void mergePantry(String userId, QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            PantryEntity local = database.pantryDao()
                    .findById(document.getId());

            if (local != null) {
                requireSameOwner(userId, local.ownerId);
            }

            if (!canApplyRemote(
                    userId,
                    "pantry",
                    document,
                    local == null ? null : local.syncState,
                    local == null ? 0 : local.updatedAt)) {
                continue;
            }

            if (Boolean.TRUE.equals(document.getBoolean("deleted"))) {
                if (local != null) {
                    database.pantryDao().deleteById(local.id);
                }
                continue;
            }

            PantryEntity remote = pantryEntity(document);

            remote.ownerId = userId;

            database.pantryDao().upsert(remote);
        }
    }

    private void mergeLists(String userId, QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ShoppingListEntity local = database.shoppingDao()
                    .findList(document.getId());

            if (local != null) {
                requireSameOwner(userId, local.ownerId);
            }

            if (!canApplyRemote(
                    userId,
                    "shoppingLists",
                    document,
                    local == null ? null : local.syncState,
                    local == null ? 0 : local.updatedAt)) {
                continue;
            }

            if (Boolean.TRUE.equals(document.getBoolean("deleted"))) {
                if (local != null) {
                    if (hasPendingItems(userId, local.id)) {
                        continue;
                    }

                    database.shoppingDao().deleteItemsForList(local.id);
                    database.shoppingDao().deleteList(local.id);
                }
                continue;
            }

            ShoppingListEntity remote = listEntity(document);
            remote.ownerId = userId;

            database.shoppingDao().upsertList(remote);
        }
    }

    private void mergeItems(String userId, QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ShoppingListItemEntity local = database.shoppingDao()
                    .findItem(document.getId());

            if (local != null
                    && database.shoppingDao()
                    .findItemForOwner(local.id, userId) == null) {
                throw new IllegalStateException(
                        "Shopping item ID is not owned by this account.");
            }

            if (!canApplyRemote(
                    userId,
                    "shoppingItems",
                    document,
                    local == null ? null : local.syncState,
                    local == null ? 0 : local.updatedAt)) {
                continue;
            }

            if (Boolean.TRUE.equals(document.getBoolean("deleted"))) {
                if (local != null) {
                    database.shoppingDao().deleteItem(local.id);
                }
                continue;
            }

            ShoppingListItemEntity remote = itemEntity(document);

            if (remote.shoppingListId == null
                    || hasPendingDeletion(
                    userId, "shoppingLists", remote.shoppingListId)) {
                continue;
            }

            ShoppingListEntity parent = database.shoppingDao()
                    .findListForOwner(remote.shoppingListId, userId);

            if (parent == null) {
                continue;
            }

            database.shoppingDao().upsertItem(remote);
        }
    }

    private boolean canApplyRemote(
            String userId,
            String collection,
            DocumentSnapshot document,
            String localState,
            long localUpdatedAt) {

        if (SyncState.PENDING.name().equals(localState)) {
            return false;
        }

        if (hasPendingDeletion(userId, collection, document.getId())) {
            return false;
        }

        return longValue(document, "updatedAt") >= localUpdatedAt;
    }

    private boolean hasPendingDeletion(
            String userId, String collection, String documentId) {

        return database.syncDao().findTombstone(
                collection + "/" + documentId,
                userId
        ) != null;
    }

    private boolean hasPendingItems(String userId, String listId) {
        for (ShoppingListItemEntity item :
                database.shoppingDao().itemsForOwner(listId, userId)) {

            if (SyncState.PENDING.name().equals(item.syncState)) {
                return true;
            }
        }

        return false;
    }

    private void requireSameOwner(String userId, String localOwnerId) {
        if (!userId.equals(localOwnerId)) {
            throw new IllegalStateException(
                    "Local record ID belongs to another owner.");
        }
    }

    private Map<String, Object> pantryMap(PantryEntity item) {
        Map<String, Object> values = new HashMap<>();
        values.put("id", item.id);
        values.put("ownerId", item.ownerId);
        values.put("ingredientId", item.ingredientId);
        values.put("productId", item.productId);
        values.put("brand", item.brand);
        values.put("quantity", item.quantity);
        values.put("unitId", item.unitId);
        values.put("expirationDate", item.expirationDate == null ? null : item.expirationDate.toString());
        values.put("updatedAt", item.updatedAt);
        return values;
    }

    private Map<String, Object> tombstoneMap(SyncTombstoneEntity tombstone) {
        Map<String, Object> values = new HashMap<>();
        values.put("id", tombstone.documentId);
        values.put("ownerId", tombstone.ownerId);
        values.put("deleted", true);
        values.put("updatedAt", tombstone.updatedAt);
        return values;
    }

    private Map<String, Object> listMap(ShoppingListEntity list) {
        Map<String, Object> values = new HashMap<>();
        values.put("id", list.id);
        values.put("ownerId", list.ownerId);
        values.put("name", list.name);
        values.put("createdAt", list.createdAt == null ? null : list.createdAt.toString());
        values.put("plannedPurchaseAt", list.plannedPurchaseAt == null ? null : list.plannedPurchaseAt.toString());
        values.put("permanent", list.permanent);
        values.put("status", list.status);
        values.put("updatedAt", list.updatedAt);
        return values;
    }

    private Map<String, Object> itemMap(ShoppingListItemEntity item) {
        Map<String, Object> values = new HashMap<>();
        values.put("id", item.id);
        values.put("shoppingListId", item.shoppingListId);
        values.put("ingredientId", item.ingredientId);
        values.put("quantity", item.quantity);
        values.put("unitId", item.unitId);
        values.put("purchased", item.purchased);
        values.put("updatedAt", item.updatedAt);
        return values;
    }

    private PantryEntity pantryEntity(DocumentSnapshot document) {
        PantryEntity result = new PantryEntity();
        result.id = document.getId();
        result.ownerId = document.getString("ownerId");
        result.ingredientId = document.getString("ingredientId");
        result.productId = document.getString("productId");
        result.brand = document.getString("brand");
        result.quantity = document.getDouble("quantity");
        result.unitId = document.getString("unitId");
        String expiration = document.getString("expirationDate");
        result.expirationDate = expiration == null ? null : java.time.LocalDate.parse(expiration);
        result.updatedAt = longValue(document, "updatedAt");
        result.syncState = SyncState.SYNCED.name();
        return result;
    }

    private ShoppingListEntity listEntity(DocumentSnapshot document) {
        ShoppingListEntity result = new ShoppingListEntity();
        result.id = document.getId();
        result.ownerId = document.getString("ownerId");
        result.name = document.getString("name");
        String created = document.getString("createdAt");
        String planned = document.getString("plannedPurchaseAt");
        result.createdAt = created == null ? null : java.time.LocalDateTime.parse(created);
        result.plannedPurchaseAt = planned == null ? null : java.time.LocalDateTime.parse(planned);
        result.permanent = Boolean.TRUE.equals(document.getBoolean("permanent"));
        result.status = document.getString("status");
        result.updatedAt = longValue(document, "updatedAt");
        result.syncState = SyncState.SYNCED.name();
        return result;
    }

    private ShoppingListItemEntity itemEntity(DocumentSnapshot document) {
        ShoppingListItemEntity result = new ShoppingListItemEntity();
        result.id = document.getId();
        result.shoppingListId = document.getString("shoppingListId");
        result.ingredientId = document.getString("ingredientId");
        Double quantity = document.getDouble("quantity");
        result.quantity = quantity == null ? 0 : quantity;
        result.unitId = document.getString("unitId");
        result.purchased = Boolean.TRUE.equals(document.getBoolean("purchased"));
        result.updatedAt = longValue(document, "updatedAt");
        result.syncState = SyncState.SYNCED.name();
        return result;
    }

    private long longValue(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value == null ? 0L : value;
    }

    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = manager == null ? null : manager.getActiveNetwork();
        NetworkCapabilities capabilities = network == null ? null : manager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}