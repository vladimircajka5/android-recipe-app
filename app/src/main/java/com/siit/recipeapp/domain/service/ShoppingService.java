package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import com.siit.recipeapp.data.repository.ShoppingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ShoppingService {
    private final ShoppingRepository repository;

    public ShoppingService(ShoppingRepository repository) { this.repository = repository; }

    public List<ShoppingList> getLists() { return repository.getLists(); }
    public List<ShoppingListItem> getItems() { return repository.getItems(); }
    public ShoppingList findList(String id) { return repository.findList(id); }
    public ShoppingListItem findItem(String id) { return repository.findItem(id); }
    public List<ShoppingListItem> itemsFor(String listId) { return repository.itemsFor(listId); }
    public void saveList(ShoppingList list) { repository.saveList(list); }
    public void removeList(String id) { repository.removeList(id); }
    public void saveItem(ShoppingListItem item) { repository.saveItem(item); }
    public void removeItem(String id) {
        ShoppingListItem item = repository.findItem(id);
        if (item == null) return;
        repository.removeItem(id);
        archiveWhenComplete(item.getShoppingListId());
    }

    public void setPurchased(String id, boolean purchased) {
        ShoppingListItem item = repository.findItem(id);
        if (item == null) return;
        repository.saveItem(new ShoppingListItem(item.getId(), item.getShoppingListId(),
                item.getIngredientId(), item.getQuantity(), item.getUnitId(), purchased));
        archiveWhenComplete(item.getShoppingListId());
    }

    public void markAllPurchased(String listId) {
        for (ShoppingListItem item : repository.itemsFor(listId)) {
            repository.saveItem(new ShoppingListItem(item.getId(), listId, item.getIngredientId(),
                    item.getQuantity(), item.getUnitId(), true));
        }
        archiveWhenComplete(listId);
    }

    public void archiveList(String listId) { updateStatus(listId, true); }

    public void reactivateList(String listId) {
        for (ShoppingListItem item : repository.itemsFor(listId)) {
            if (item.isPurchased()) {
                repository.saveItem(new ShoppingListItem(item.getId(), listId, item.getIngredientId(),
                        item.getQuantity(), item.getUnitId(), false));
            }
        }
        updateStatus(listId, false);
    }

    public ShoppingList copyList(String sourceId) {
        ShoppingList source = repository.findList(sourceId);
        if (source == null) return null;
        String copyId = UUID.randomUUID().toString();
        ShoppingList copy = new ShoppingList(copyId, source.getOwnerId(), source.getName() + " (copy)",
                LocalDateTime.now(), null, source.isPermanent(),
                ShoppingListStatus.ACTIVE);
        repository.saveList(copy);
        for (ShoppingListItem item : repository.itemsFor(sourceId)) {
            repository.saveItem(new ShoppingListItem(UUID.randomUUID().toString(), copyId,
                    item.getIngredientId(), item.getQuantity(), item.getUnitId(), false));
        }
        return copy;
    }

    public void archiveCompletedListIfNeeded(String listId) { archiveWhenComplete(listId); }

    private void archiveWhenComplete(String listId) {
        ShoppingList list = repository.findList(listId);
        List<ShoppingListItem> entries = repository.itemsFor(listId);
        if (list == null || list.isPermanent()
                || list.getStatus() != ShoppingListStatus.ACTIVE
                || entries.isEmpty()) return;
        for (ShoppingListItem item : entries) if (!item.isPurchased()) return;
        updateStatus(listId, true);
    }

    private void updateStatus(String listId, boolean archived) {
        ShoppingList list = repository.findList(listId);
        if (list == null) return;
        repository.saveList(new ShoppingList(list.getId(), list.getOwnerId(), list.getName(),
                list.getCreatedAt(), list.getPlannedPurchaseAt(), list.isPermanent(),
                archived ? ShoppingListStatus.ARCHIVED
                        : ShoppingListStatus.ACTIVE));
    }
}
