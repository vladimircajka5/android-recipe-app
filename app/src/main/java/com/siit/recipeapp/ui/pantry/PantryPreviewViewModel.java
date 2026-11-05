package com.siit.recipeapp.ui.pantry;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Preview class only for showing dummy pantry item data
public final class PantryPreviewViewModel extends ViewModel {
    private final MutableLiveData<List<PantryItem>> items =
            new MutableLiveData<>(PantryPreviewData.initialItems());

    public LiveData<List<PantryItem>> getItems() { return items; }

    @MainThread public void previewItem(PantryItem item) {
        List<PantryItem> updated = new ArrayList<>(items.getValue());
        updated.removeIf(current -> current.getId().equals(item.getId()));
        updated.add(item);
        items.setValue(Collections.unmodifiableList(updated));
    }

    @MainThread public void removeItem(String itemId) {
        List<PantryItem> updated = new ArrayList<>(items.getValue());
        updated.removeIf(current -> current.getId().equals(itemId));
        items.setValue(Collections.unmodifiableList(updated));
    }
}
