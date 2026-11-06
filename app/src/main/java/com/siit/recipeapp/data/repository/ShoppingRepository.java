package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import java.util.List;

public interface ShoppingRepository {
    List<ShoppingList> getLists();
    List<ShoppingListItem> getItems();
    ShoppingList findList(String id);
    ShoppingListItem findItem(String id);
    List<ShoppingListItem> itemsFor(String listId);
    void saveList(ShoppingList list);
    void removeList(String id);
    void saveItem(ShoppingListItem item);
    void removeItem(String id);
}
