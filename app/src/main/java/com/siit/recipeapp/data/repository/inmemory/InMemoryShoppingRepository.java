package com.siit.recipeapp.data.repository.inmemory;

import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.repository.ShoppingRepository;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryShoppingRepository implements ShoppingRepository {
    private final List<ShoppingList> lists;
    private final List<ShoppingListItem> items;

    public InMemoryShoppingRepository(List<ShoppingList> initialLists, List<ShoppingListItem> initialItems) {
        lists = new ArrayList<>(initialLists);
        items = new ArrayList<>(initialItems);
    }

    @Override public List<ShoppingList> getLists() { return List.copyOf(lists); }
    @Override public List<ShoppingListItem> getItems() { return List.copyOf(items); }

    @Override public ShoppingList findList(String id) {
        for (ShoppingList list : lists) if (list.getId().equals(id)) return list;
        return null;
    }

    @Override public ShoppingListItem findItem(String id) {
        for (ShoppingListItem item : items) if (item.getId().equals(id)) return item;
        return null;
    }

    @Override public List<ShoppingListItem> itemsFor(String listId) {
        List<ShoppingListItem> result = new ArrayList<>();
        for (ShoppingListItem item : items) {
            if (item.getShoppingListId().equals(listId)) result.add(item);
        }
        return List.copyOf(result);
    }

    @Override public void saveList(ShoppingList list) {
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getId().equals(list.getId())) {
                lists.set(i, list);
                return;
            }
        }
        lists.add(list);
    }

    @Override public void removeList(String id) {
        lists.removeIf(list -> list.getId().equals(id));
        items.removeIf(item -> item.getShoppingListId().equals(id));
    }

    @Override public void saveItem(ShoppingListItem item) {
        if (findList(item.getShoppingListId()) == null) return;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                return;
            }
        }
        items.add(item);
    }

    @Override public void removeItem(String id) {
        items.removeIf(item -> item.getId().equals(id));
    }
}
