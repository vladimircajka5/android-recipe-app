package com.siit.recipeapp.data.repository.room;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.repository.ShoppingRepository;

import java.util.ArrayList;
import java.util.List;

public final class RoomShoppingRepository implements ShoppingRepository {
    private final RecipeDatabase database;

    public RoomShoppingRepository(RecipeDatabase database) {
        this.database = database;
    }

    @Override
    public List<ShoppingList> getLists() {
        List<ShoppingList> result = new ArrayList<>();
        for (ShoppingListEntity list : database.shoppingDao().getLists()) {
            result.add(EntityMapper.list(list));
        }
        return result;
    }

    @Override
    public List<ShoppingListItem> getItems() {
        List<ShoppingListItem> result = new ArrayList<>();
        for (ShoppingListItemEntity item : database.shoppingDao().getItems()) {
            result.add(EntityMapper.item(item));
        }
        return result;
    }

    @Override
    public ShoppingList findList(String id) {
        ShoppingListEntity list = database.shoppingDao().findList(id);
        return list == null ? null : EntityMapper.list(list);
    }

    @Override
    public ShoppingListItem findItem(String id) {
        ShoppingListItemEntity item = database.shoppingDao().findItem(id);
        return item == null ? null : EntityMapper.item(item);
    }

    @Override
    public List<ShoppingListItem> itemsFor(String listId) {
        List<ShoppingListItem> result = new ArrayList<>();
        for (ShoppingListItemEntity item : database.shoppingDao().itemsFor(listId)) {
            result.add(EntityMapper.item(item));
        }
        return result;
    }

    @Override
    public void saveList(ShoppingList list) {
        database.shoppingDao().upsertList(EntityMapper.list(list, SyncState.PENDING));
    }

    @Override
    public void removeList(String id) {
        database.shoppingDao().deleteItemsForList(id);
        database.shoppingDao().deleteList(id);
    }

    @Override
    public void saveItem(ShoppingListItem item) {
        if (findList(item.getShoppingListId()) != null) {
            database.shoppingDao().upsertItem(EntityMapper.item(item, SyncState.PENDING));
        }
    }

    @Override
    public void removeItem(String id) {
        database.shoppingDao().deleteItem(id);
    }
}