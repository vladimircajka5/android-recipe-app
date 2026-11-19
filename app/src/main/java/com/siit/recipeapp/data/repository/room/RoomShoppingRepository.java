package com.siit.recipeapp.data.repository.room;

import android.content.Context;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.local.entity.SyncTombstoneEntity;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.ShoppingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RoomShoppingRepository implements ShoppingRepository {

    private final RecipeDatabase database;
    private final Context context;
    private final String ownerId;

    public RoomShoppingRepository(RecipeDatabase database, Context context) {
        this.database = database;
        this.context = context.getApplicationContext();
        this.ownerId = UserDataSession.currentOwnerId(this.context);
    }

    private boolean isActiveOwner() {
        return ownerId != null
                && ownerId.equals(UserDataSession.currentOwnerId(context));
    }

    private void requireActiveOwner() {
        if (!isActiveOwner()) {
            throw new IllegalStateException(
                    "Sign in again before changing shopping data.");
        }
    }

    @Override
    public List<ShoppingList> getLists() {
        if (!isActiveOwner()) {
            return Collections.emptyList();
        }

        List<ShoppingList> result = new ArrayList<>();

        for (ShoppingListEntity entity :
                database.shoppingDao().getListsForOwner(ownerId)) {
            result.add(EntityMapper.list(entity));
        }

        return result;
    }

    @Override
    public List<ShoppingListItem> getItems() {
        if (!isActiveOwner()) {
            return Collections.emptyList();
        }

        return mapItems(
                database.shoppingDao().getItemsForOwner(ownerId));
    }

    @Override
    public ShoppingList findList(String id) {
        if (!isActiveOwner()) {
            return null;
        }

        ShoppingListEntity entity = database.shoppingDao()
                .findListForOwner(id, ownerId);

        return entity == null ? null : EntityMapper.list(entity);
    }

    @Override
    public ShoppingListItem findItem(String id) {
        if (!isActiveOwner()) {
            return null;
        }

        ShoppingListItemEntity entity = database.shoppingDao()
                .findItemForOwner(id, ownerId);

        return entity == null ? null : EntityMapper.item(entity);
    }

    @Override
    public List<ShoppingListItem> itemsFor(String listId) {
        if (!isActiveOwner()) {
            return Collections.emptyList();
        }

        return mapItems(
                database.shoppingDao().itemsForOwner(listId, ownerId));
    }

    private static List<ShoppingListItem> mapItems(
            List<ShoppingListItemEntity> entities) {

        List<ShoppingListItem> result = new ArrayList<>(entities.size());

        for (ShoppingListItemEntity entity : entities) {
            result.add(EntityMapper.item(entity));
        }

        return result;
    }

    @Override
    public void saveList(ShoppingList list) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            ShoppingListEntity existing = database.shoppingDao()
                    .findList(list.getId());

            if (existing != null && !ownerId.equals(existing.ownerId)) {
                throw new IllegalStateException(
                        "Cannot replace another owner's shopping list.");
            }

            ShoppingListEntity entity = EntityMapper.list(
                    list, SyncState.PENDING);

            entity.ownerId = ownerId;

            database.syncDao().deleteTombstone(
                    "shoppingLists/" + entity.id);

            database.shoppingDao().upsertList(entity);
        });
    }

    @Override
    public void removeList(String id) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            ShoppingListEntity list = database.shoppingDao()
                    .findListForOwner(id, ownerId);

            if (list == null) {
                return;
            }

            long deletedAt = System.currentTimeMillis();

            database.syncDao().upsertTombstone(
                    new SyncTombstoneEntity(
                            ownerId, "shoppingLists", id, deletedAt));

            for (ShoppingListItemEntity item :
                    database.shoppingDao().itemsForOwner(id, ownerId)) {

                database.syncDao().upsertTombstone(
                        new SyncTombstoneEntity(
                                ownerId,
                                "shoppingItems",
                                item.id,
                                deletedAt
                        )
                );
            }

            database.shoppingDao().deleteItemsForList(id);
            database.shoppingDao().deleteList(id);
        });
    }

    @Override
    public void saveItem(ShoppingListItem item) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            ShoppingListEntity parent = database.shoppingDao()
                    .findListForOwner(item.getShoppingListId(), ownerId);

            if (parent == null) {
                throw new IllegalStateException(
                        "Shopping list is missing or belongs to another owner.");
            }

            ShoppingListItemEntity existing = database.shoppingDao()
                    .findItem(item.getId());

            if (existing != null
                    && database.shoppingDao()
                    .findItemForOwner(item.getId(), ownerId) == null) {

                throw new IllegalStateException(
                        "Cannot replace another owner's shopping item.");
            }

            database.syncDao().deleteTombstone(
                    "shoppingItems/" + item.getId());

            database.shoppingDao().upsertItem(
                    EntityMapper.item(item, SyncState.PENDING));
        });
    }

    @Override
    public void removeItem(String id) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            ShoppingListItemEntity item = database.shoppingDao()
                    .findItemForOwner(id, ownerId);

            if (item == null) {
                return;
            }

            database.syncDao().upsertTombstone(
                    new SyncTombstoneEntity(
                            ownerId,
                            "shoppingItems",
                            id,
                            System.currentTimeMillis()
                    )
            );

            database.shoppingDao().deleteItem(id);
        });
    }
}