package com.siit.recipeapp.data.repository.inmemory;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.repository.IngredientRepository;
import java.util.ArrayList;
import java.util.List;

// In-memory barcode ingredient catalog, will be replaced with persistant version later
public final class InMemoryIngredientRepository implements IngredientRepository {
    private final List<Ingredient> ingredients;

    public InMemoryIngredientRepository(List<Ingredient> initialIngredients) { ingredients = new ArrayList<>(initialIngredients); }
    @Override public List<Ingredient> getIngredients() { return List.copyOf(ingredients); }
    @Override public Ingredient findById(String ingredientId) {
        for (Ingredient ingredient : ingredients) if (ingredient.getId().equals(ingredientId)) return ingredient;
        return null;
    }
    @Override public void saveIngredient(Ingredient ingredient) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).getId().equals(ingredient.getId())) { ingredients.set(i, ingredient); return; }
        }
        ingredients.add(ingredient);
    }
}
