package com.siit.recipeapp.ui.shopping;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.siit.recipeapp.data.local.LocalDataSeeder;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.repository.inmemory.InMemoryShoppingRepository;
import com.siit.recipeapp.data.repository.room.RoomShoppingRepository;
import com.siit.recipeapp.domain.service.ShoppingService;
import com.siit.recipeapp.ui.preview.ShoppingPreviewData;

public final class ShoppingViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ShoppingViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == ShoppingViewModel.class) {
            RecipeDatabase database = RecipeDatabase.getInstance(context);
            LocalDataSeeder.seed(database);
            ShoppingService service = new ShoppingService(new RoomShoppingRepository(database));
            return modelClass.cast(new ShoppingViewModel(service));
        }
        throw new IllegalArgumentException("Unsupported ViewModel: " + modelClass.getName());
    }
}
