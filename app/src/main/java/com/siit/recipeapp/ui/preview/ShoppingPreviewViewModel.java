package com.siit.recipeapp.ui.preview;

import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import java.util.ArrayList;
import java.util.List;

/** Activity-scoped demo state. Screens read snapshots and refresh explicitly. */
public final class ShoppingPreviewViewModel extends ViewModel {
    private final List<ShoppingList> lists = new ArrayList<>(ShoppingPreviewData.initialLists());
    private final List<ShoppingListItem> items = new ArrayList<>(ShoppingPreviewData.initialItems());

    public List<ShoppingList> getLists() { return List.copyOf(lists); }
    public List<ShoppingListItem> getItems() { return List.copyOf(items); }

    public ShoppingList findList(String id) {
        for (ShoppingList list : lists) if (list.getId().equals(id)) return list;
        return null;
    }

    public ShoppingListItem findItem(String id) {
        for (ShoppingListItem item : items) if (item.getId().equals(id)) return item;
        return null;
    }

    public List<ShoppingListItem> itemsFor(String listId) {
        List<ShoppingListItem> result = new ArrayList<>();
        for (ShoppingListItem item : items) {
            if (item.getShoppingListId().equals(listId)) result.add(item);
        }
        return List.copyOf(result);
    }

    public void saveList(ShoppingList list) {
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getId().equals(list.getId())) {
                lists.set(i, list);
                return;
            }
        }
        lists.add(list);
    }

    public void removeList(String id) {
        lists.removeIf(list -> list.getId().equals(id));
        items.removeIf(item -> item.getShoppingListId().equals(id));
    }

    public void saveItem(ShoppingListItem item) {
        if (findList(item.getShoppingListId()) == null) return;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                return;
            }
        }
        items.add(item);
    }

    public void removeItem(String id) {
        items.removeIf(item -> item.getId().equals(id));
    }

    public void setPurchased(String id, boolean purchased) {
        ShoppingListItem item = findItem(id);
        if (item != null) saveItem(new ShoppingListItem(item.getId(), item.getShoppingListId(),
                item.getIngredientId(), item.getQuantity(), item.getUnitId(), purchased));
    }

    public void markAllPurchased(String listId) {
        for (int i = 0; i < items.size(); i++) {
            ShoppingListItem item = items.get(i);
            if (item.getShoppingListId().equals(listId)) {
                items.set(i, new ShoppingListItem(item.getId(), listId, item.getIngredientId(),
                        item.getQuantity(), item.getUnitId(), true));
            }
        }
    }
}
