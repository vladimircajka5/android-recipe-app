package com.siit.recipeapp.ui.pantry;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.siit.recipeapp.data.local.LocalDataSeeder;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.repository.room.RoomIngredientRepository;
import com.siit.recipeapp.data.repository.room.RoomPantryRepository;
import com.siit.recipeapp.data.repository.room.RoomProductRepository;
import com.siit.recipeapp.domain.service.IngredientService;
import com.siit.recipeapp.domain.service.PantryService;
import com.siit.recipeapp.domain.service.ProductService;

public final class PantryViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public PantryViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == PantryViewModel.class) {
            RecipeDatabase database = RecipeDatabase.getInstance(context);
            LocalDataSeeder.seed(database);


            PantryViewModel viewModel = getViewModel(database);
            return modelClass.cast(viewModel);
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }

    @NonNull
    private static PantryViewModel getViewModel(RecipeDatabase database) {
        RoomPantryRepository pantryRepository = new RoomPantryRepository(database);
        RoomIngredientRepository ingredientRepository = new RoomIngredientRepository(database);
        RoomProductRepository productRepository = new RoomProductRepository(database);
        return new PantryViewModel(
                new PantryService(pantryRepository),
                new IngredientService(ingredientRepository),
                new ProductService(productRepository));
    }
}
