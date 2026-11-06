package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.repository.PantryRepository;
import java.util.List;

public final class PantryService {
    private final PantryRepository repository;

    public PantryService(PantryRepository repository) { this.repository = repository; }

    public List<PantryItem> getItems() { return repository.getItems(); }
    public void saveItem(PantryItem item) { repository.saveItem(item); }
    public void removeItem(String itemId) { repository.removeItem(itemId); }
}
