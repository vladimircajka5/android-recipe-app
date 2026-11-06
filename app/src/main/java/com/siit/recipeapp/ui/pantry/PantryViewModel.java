package com.siit.recipeapp.ui.pantry;

import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.domain.service.PantryService;
import java.util.List;

public final class PantryViewModel extends ViewModel {
    private final PantryService service;
    public PantryViewModel(PantryService service) { this.service = service; }
    public List<PantryItem> getItems() { return service.getItems(); }
    public void saveItem(PantryItem item) { service.saveItem(item); }
    public void removeItem(String itemId) { service.removeItem(itemId); }
}
