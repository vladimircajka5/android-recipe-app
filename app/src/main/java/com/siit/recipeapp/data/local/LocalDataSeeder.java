package com.siit.recipeapp.data.local;

import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.ui.preview.PantryPreviewData;
import com.siit.recipeapp.ui.preview.ShoppingPreviewData;

public final class LocalDataSeeder {
    private LocalDataSeeder() {}

    public static void seed(RecipeDatabase database) {
        if (database.catalogDao().getIngredients().isEmpty()) {
            for (Ingredient ingredient : PantryPreviewData.INGREDIENTS) {
                database.catalogDao().upsertIngredient(EntityMapper.ingredient(ingredient));
            }
        }
        if (database.pantryDao().getAll().isEmpty()) {
            for (PantryItem item : PantryPreviewData.initialItems()) {
                database.pantryDao().upsert(EntityMapper.pantry(item, SyncState.SYNCED));
            }
        }
        if (database.shoppingDao().getLists().isEmpty()) {
            for (ShoppingList list : ShoppingPreviewData.initialLists()) {
                database.shoppingDao().upsertList(EntityMapper.list(list, SyncState.SYNCED));
            }
            for (ShoppingListItem item : ShoppingPreviewData.initialItems()) {
                database.shoppingDao().upsertItem(EntityMapper.item(item, SyncState.SYNCED));
            }
        }
    }
}