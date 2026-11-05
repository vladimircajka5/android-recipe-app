package com.siit.recipeapp.ui.preview;

import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.util.ArrayList;
import java.util.List;

/** Activity-scoped temporary state; fragments refresh explicitly after edits. */
public final class PantryPreviewViewModel extends ViewModel {
    private final List<PantryItem> items = new ArrayList<>(PantryPreviewData.initialItems());

    public List<PantryItem> getItems() { return List.copyOf(items); }

    public void previewItem(PantryItem item) {
        items.removeIf(current -> current.getId().equals(item.getId()));
        items.add(item);
    }

    public void removeItem(String itemId) {
        items.removeIf(current -> current.getId().equals(itemId));
    }
}
