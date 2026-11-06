package com.siit.recipeapp.ui.shopping;

import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.domain.service.ShoppingService;
import java.util.List;

public final class ShoppingViewModel extends ViewModel {
    private final ShoppingService service;
    public ShoppingViewModel(ShoppingService service) { this.service = service; }
    public List<ShoppingList> getLists() { return service.getLists(); }
    public List<ShoppingListItem> getItems() { return service.getItems(); }
    public ShoppingList findList(String id) { return service.findList(id); }
    public ShoppingListItem findItem(String id) { return service.findItem(id); }
    public List<ShoppingListItem> itemsFor(String listId) { return service.itemsFor(listId); }
    public void saveList(ShoppingList list) { service.saveList(list); }
    public void removeList(String id) { service.removeList(id); }
    public void saveItem(ShoppingListItem item) { service.saveItem(item); }
    public void removeItem(String id) { service.removeItem(id); }
    public void setPurchased(String id, boolean purchased) { service.setPurchased(id, purchased); }
    public void markAllPurchased(String listId) { service.markAllPurchased(listId); }
    public void archiveList(String listId) { service.archiveList(listId); }
    public void reactivateList(String listId) { service.reactivateList(listId); }
    public ShoppingList copyList(String sourceId) { return service.copyList(sourceId); }
    public void archiveCompletedListIfNeeded(String listId) { service.archiveCompletedListIfNeeded(listId); }
}
