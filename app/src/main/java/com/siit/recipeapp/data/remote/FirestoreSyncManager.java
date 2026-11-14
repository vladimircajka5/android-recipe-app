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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FirestoreSyncManager {
    public interface Callback {
        void onComplete();
        void onError(@NonNull Exception exception);
    }

    private final Context context;
    private final RecipeDatabase database;
    private final FirebaseFirestore firestore;

    public FirestoreSyncManager(Context context, RecipeDatabase database) {
        this.context = context.getApplicationContext();
        this.database = database;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void sync(String userId, Callback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("A signed-in user is required"));
            return;
        }
        if (!isOnline()) {
            callback.onComplete();
            return;
        }

        pushPending(userId, callback);
    }

    private void pushPending(String userId, Callback callback) {
        List<PantryEntity> pantry = database.pantryDao().getPending();
        List<ShoppingListEntity> lists = database.shoppingDao().getPendingLists();
        List<ShoppingListItemEntity> items = database.shoppingDao().getPendingItems();

        com.google.android.gms.tasks.Task<Void> task = com.google.android.gms.tasks.Tasks.forResult(null);
        for (PantryEntity item : pantry) {
            task = task.continueWithTask(ignored -> firestore
                    .collection("users").document(userId).collection("pantry")
                    .document(item.id).set(pantryMap(item)));
            task = markAfter(task, () -> database.pantryDao().markSynced(item.id));
        }
        for (ShoppingListEntity list : lists) {
            task = task.continueWithTask(ignored -> firestore
                    .collection("users").document(userId).collection("shoppingLists")
                    .document(list.id).set(listMap(list)));
            task = markAfter(task, () -> database.shoppingDao().markListSynced(list.id));
        }
        for (ShoppingListItemEntity item : items) {
            task = task.continueWithTask(ignored -> firestore
                    .collection("users").document(userId).collection("shoppingItems")
                    .document(item.id).set(itemMap(item)));
            task = markAfter(task, () -> database.shoppingDao().markItemSynced(item.id));
        }

        task.addOnSuccessListener(ignored -> pullRemote(userId, callback))
                .addOnFailureListener(callback::onError);
    }

    private com.google.android.gms.tasks.Task<Void> markAfter(
            com.google.android.gms.tasks.Task<Void> task, Runnable action) {
        return task.continueWith(previous -> {
            action.run();
            return null;
        });
    }

    private void pullRemote(String userId, Callback callback) {
        com.google.android.gms.tasks.Task<QuerySnapshot> pantryTask = firestore
                .collection("users").document(userId).collection("pantry").get();
        com.google.android.gms.tasks.Task<QuerySnapshot> listsTask = firestore
                .collection("users").document(userId).collection("shoppingLists").get();
        com.google.android.gms.tasks.Task<QuerySnapshot> itemsTask = firestore
                .collection("users").document(userId).collection("shoppingItems").get();

        com.google.android.gms.tasks.Tasks.whenAllSuccess(pantryTask, listsTask, itemsTask)
                .addOnSuccessListener(results -> {
                    mergePantry((QuerySnapshot) results.get(0));
                    mergeLists((QuerySnapshot) results.get(1));
                    mergeItems((QuerySnapshot) results.get(2));
                    callback.onComplete();
                })
                .addOnFailureListener(callback::onError);
    }

    private void mergePantry(QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            PantryEntity remote = pantryEntity(document);
            PantryEntity local = database.pantryDao().getAll().stream()
                    .filter(item -> item.id.equals(remote.id)).findFirst().orElse(null);
            if (local == null || remote.updatedAt > local.updatedAt
                    || "SYNCED".equals(local.syncState)) {
                database.pantryDao().upsert(remote);
            }
        }
    }

    private void mergeLists(QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ShoppingListEntity remote = listEntity(document);
            ShoppingListEntity local = database.shoppingDao().findList(remote.id);
            if (local == null || remote.updatedAt > local.updatedAt
                    || "SYNCED".equals(local.syncState)) {
                database.shoppingDao().upsertList(remote);
            }
        }
    }

    private void mergeItems(QuerySnapshot snapshot) {
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ShoppingListItemEntity remote = itemEntity(document);
            ShoppingListItemEntity local = database.shoppingDao().findItem(remote.id);
            if (local == null || remote.updatedAt > local.updatedAt
                    || "SYNCED".equals(local.syncState)) {
                database.shoppingDao().upsertItem(remote);
            }
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