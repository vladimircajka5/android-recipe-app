package com.siit.recipeapp.ui.pantry;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.siit.recipeapp.data.repository.inmemory.InMemoryPantryRepository;
import com.siit.recipeapp.domain.service.PantryService;
import com.siit.recipeapp.ui.preview.PantryPreviewData;

public final class PantryViewModelFactory implements ViewModelProvider.Factory {
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == PantryViewModel.class) {
            PantryService service = new PantryService(new InMemoryPantryRepository(PantryPreviewData.initialItems()));
            return modelClass.cast(new PantryViewModel(service));
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }
}
