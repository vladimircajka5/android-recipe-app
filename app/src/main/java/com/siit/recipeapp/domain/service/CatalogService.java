package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.catalog.*;
import com.siit.recipeapp.data.repository.CatalogRepository;
import java.util.UUID;

public final class CatalogService {
    private final CatalogRepository repository;
    public CatalogService(CatalogRepository repository) {
        if (repository == null) throw new IllegalArgumentException("Repository is required");
        this.repository = repository;
    }
    public void addIngredientCategory(String name, String description) {
        ensureUnique(name,
                repository.getIngredientCategories().stream().map(IngredientCategory::getName).toArray(String[]::new));
        repository.saveIngredientCategory(new IngredientCategory(UUID.randomUUID().toString(), name, description));
    }
    public void addAllergen(String name, String description) {
        ensureUnique(name,
                repository.getAllergens().stream().map(Allergen::getName).toArray(String[]::new));
        repository.saveAllergen(new Allergen(UUID.randomUUID().toString(), name, description));
    }
    public void addMealType(String name, String description) {
        ensureUnique(name,
                repository.getMealTypes().stream().map(MealType::getName).toArray(String[]::new));
        repository.saveMealType(new MealType(UUID.randomUUID().toString(), name, description));
    }
    public void addDishType(String name, String description) {
        ensureUnique(name,
                repository.getDishTypes().stream().map(DishType::getName).toArray(String[]::new));
        repository.saveDishType(new DishType(UUID.randomUUID().toString(), name, description));
    }
    public void addDietType(String name, String description) {
        ensureUnique(name,
                repository.getDietTypes().stream().map(DietType::getName).toArray(String[]::new));
        repository.saveDietType(new DietType(UUID.randomUUID().toString(), name, description));
    }

    public void updateIngredientCategory(String id, String name, String description) {
        ensureUniqueExcept(name,
                repository.getIngredientCategories().stream().filter(x -> !x.getId().equals(id)).map(IngredientCategory::getName).toArray(String[]::new));
        repository.saveIngredientCategory(new IngredientCategory(id, name, description));
    }
    public void updateAllergen(String id, String name, String description) {
        ensureUniqueExcept(name,
                repository.getAllergens().stream().filter(x -> !x.getId().equals(id)).map(Allergen::getName).toArray(String[]::new));
        repository.saveAllergen(new Allergen(id, name, description));
    }
    public void updateMealType(String id, String name, String description) {
        ensureUniqueExcept(name,
                repository.getMealTypes().stream().filter(x -> !x.getId().equals(id)).map(MealType::getName).toArray(String[]::new));
        repository.saveMealType(new MealType(id, name, description));
    }
    public void updateDishType(String id, String name, String description) {
        ensureUniqueExcept(name,
                repository.getDishTypes().stream().filter(x -> !x.getId().equals(id)).map(DishType::getName).toArray(String[]::new));
        repository.saveDishType(new DishType(id, name, description));
    }
    public void updateDietType(String id, String name, String description) {
        ensureUniqueExcept(name,
                repository.getDietTypes().stream().filter(x -> !x.getId().equals(id)).map(DietType::getName).toArray(String[]::new));
        repository.saveDietType(new DietType(id, name, description));
    }
    private void ensureUniqueExcept(String name, String[] existing) { ensureUnique(name, existing); }
    private void ensureUnique(String name, String[] existing) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required");
        for (String item : existing) if (item.equalsIgnoreCase(name.trim())) throw new IllegalArgumentException("Item already exists");
    }
}