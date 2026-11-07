package com.siit.recipeapp.ui.pantry;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.siit.recipeapp.data.repository.inmemory.InMemoryIngredientRepository;
import com.siit.recipeapp.data.repository.inmemory.InMemoryPantryRepository;
import com.siit.recipeapp.data.repository.inmemory.InMemoryProductRepository;
import com.siit.recipeapp.domain.service.IngredientService;
import com.siit.recipeapp.domain.service.PantryService;
import com.siit.recipeapp.domain.service.ProductService;
import com.siit.recipeapp.ui.preview.PantryPreviewData;

public final class PantryViewModelFactory implements ViewModelProvider.Factory {
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == PantryViewModel.class) {
            InMemoryPantryRepository pantryRepository = new InMemoryPantryRepository(PantryPreviewData.initialItems());
            InMemoryIngredientRepository ingredientRepository = new InMemoryIngredientRepository(PantryPreviewData.INGREDIENTS);
            InMemoryProductRepository productRepository = new InMemoryProductRepository();
            PantryViewModel viewModel = new PantryViewModel(
                    new PantryService(pantryRepository),
                    new IngredientService(ingredientRepository),
                    new ProductService(productRepository));
            return modelClass.cast(viewModel);
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }
}
