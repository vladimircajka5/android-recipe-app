package com.siit.recipeapp.ui.shopping;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.siit.recipeapp.data.repository.inmemory.InMemoryShoppingRepository;
import com.siit.recipeapp.domain.service.ShoppingService;
import com.siit.recipeapp.ui.preview.ShoppingPreviewData;

public final class ShoppingViewModelFactory implements ViewModelProvider.Factory {
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == ShoppingViewModel.class) {
            ShoppingService service = new ShoppingService(new InMemoryShoppingRepository(
                    ShoppingPreviewData.initialLists(), ShoppingPreviewData.initialItems()));
            return modelClass.cast(new ShoppingViewModel(service));
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }
}
