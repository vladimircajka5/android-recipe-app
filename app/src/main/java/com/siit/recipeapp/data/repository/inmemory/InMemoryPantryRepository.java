package com.siit.recipeapp.data.repository.inmemory;

import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.repository.PantryRepository;
import java.util.ArrayList;
import java.util.List;

// In-memory pantry catalog, will be replaced with persistant version later
public final class InMemoryPantryRepository implements PantryRepository {
    private final List<PantryItem> items;

    public InMemoryPantryRepository(List<PantryItem> initialItems) { items = new ArrayList<>(initialItems); }
    @Override public List<PantryItem> getItems() { return List.copyOf(items); }
    @Override public PantryItem findItemByProductId(String productId) {
        for (PantryItem item : items) if (productId != null && productId.equals(item.getProductId())) return item;
        return null;
    }
    @Override public void saveItem(PantryItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) { items.set(i, item); return; }
        }
        items.add(item);
    }
    @Override public void removeItem(String itemId) { items.removeIf(item -> item.getId().equals(itemId)); }
}
