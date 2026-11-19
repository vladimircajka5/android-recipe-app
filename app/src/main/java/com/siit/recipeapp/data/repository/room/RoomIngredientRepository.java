package com.siit.recipeapp.data.repository.room;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.entity.IngredientEntity;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.repository.IngredientRepository;

import java.util.ArrayList;
import java.util.List;

public final class RoomIngredientRepository implements IngredientRepository {
    private final RecipeDatabase database;

    public RoomIngredientRepository(RecipeDatabase database) {
        this.database = database;
    }

    @Override
    public List<Ingredient> getIngredients() {
        List<Ingredient> result = new ArrayList<>();
        for (IngredientEntity ingredient : database.catalogDao().getIngredients()) {
            result.add(EntityMapper.ingredient(ingredient));
        }
        return result;
    }

    @Override
    public Ingredient findById(String ingredientId) {
        IngredientEntity ingredient = database.catalogDao().findIngredient(ingredientId);
        return ingredient == null ? null : EntityMapper.ingredient(ingredient);
    }

    @Override
    public void saveIngredient(Ingredient ingredient) {
        database.catalogDao().upsertIngredient(EntityMapper.ingredient(ingredient));
    }
}