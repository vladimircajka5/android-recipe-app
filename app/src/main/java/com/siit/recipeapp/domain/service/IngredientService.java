package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.repository.IngredientRepository;
import java.util.List;

// Ingredient catalog use cases shared by pantry and shopping flows
public final class IngredientService {
    private final IngredientRepository repository;
    public IngredientService(IngredientRepository repository) { this.repository = repository; }
    public List<Ingredient> getIngredients() { return repository.getIngredients(); }
    public Ingredient findById(String id) { return repository.findById(id); }
    public void saveIngredient(Ingredient ingredient) { repository.saveIngredient(ingredient); }
}
