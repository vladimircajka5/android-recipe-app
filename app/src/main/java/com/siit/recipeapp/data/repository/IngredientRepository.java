package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import java.util.List;

public interface IngredientRepository {
    List<Ingredient> getIngredients();
    Ingredient findById(String ingredientId);
    void saveIngredient(Ingredient ingredient);
}
