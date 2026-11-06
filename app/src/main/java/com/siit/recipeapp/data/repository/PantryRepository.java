package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.util.List;

public interface PantryRepository {
    List<PantryItem> getItems();
    void saveItem(PantryItem item);
    void removeItem(String itemId);
}
