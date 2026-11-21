package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.catalog.Allergen;
import com.siit.recipeapp.data.model.catalog.DietType;
import com.siit.recipeapp.data.model.catalog.DishType;
import com.siit.recipeapp.data.model.catalog.IngredientCategory;
import com.siit.recipeapp.data.model.catalog.MealType;
import com.siit.recipeapp.data.repository.CatalogRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class CatalogService {

    private final CatalogRepository repository;

    public CatalogService(CatalogRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository is required.");
        }

        this.repository = repository;
    }

    public void saveIngredientCategory(
            String id,
            String name,
            String description,
            CatalogRepository.Callback<Void> callback) {

        String entryId = resolveId(id);

        repository.getIngredientCategories(validateThenSave(
                entryId,
                name,
                IngredientCategory::getId,
                IngredientCategory::getName,
                () -> repository.saveIngredientCategory(
                        new IngredientCategory(entryId, name, description),
                        callback),
                callback
        ));
    }

    public void saveAllergen(
            String id,
            String name,
            String description,
            CatalogRepository.Callback<Void> callback) {

        String entryId = resolveId(id);

        repository.getAllergens(validateThenSave(
                entryId,
                name,
                Allergen::getId,
                Allergen::getName,
                () -> repository.saveAllergen(
                        new Allergen(entryId, name, description),
                        callback),
                callback
        ));
    }

    public void saveMealType(
            String id,
            String name,
            String description,
            CatalogRepository.Callback<Void> callback) {

        String entryId = resolveId(id);

        repository.getMealTypes(validateThenSave(
                entryId,
                name,
                MealType::getId,
                MealType::getName,
                () -> repository.saveMealType(
                        new MealType(entryId, name, description),
                        callback),
                callback
        ));
    }

    public void saveDishType(
            String id,
            String name,
            String description,
            CatalogRepository.Callback<Void> callback) {

        String entryId = resolveId(id);

        repository.getDishTypes(validateThenSave(
                entryId,
                name,
                DishType::getId,
                DishType::getName,
                () -> repository.saveDishType(
                        new DishType(entryId, name, description),
                        callback),
                callback
        ));
    }

    public void saveDietType(
            String id,
            String name,
            String description,
            CatalogRepository.Callback<Void> callback) {

        String entryId = resolveId(id);

        repository.getDietTypes(validateThenSave(
                entryId,
                name,
                DietType::getId,
                DietType::getName,
                () -> repository.saveDietType(
                        new DietType(entryId, name, description),
                        callback),
                callback
        ));
    }

    private String resolveId(String id) {
        return id == null ? UUID.randomUUID().toString() : id;
    }

    private <T> CatalogRepository.Callback<List<T>> validateThenSave(
            String id,
            String name,
            Function<T, String> idOf,
            Function<T, String> nameOf,
            Runnable save,
            CatalogRepository.Callback<Void> callback) {

        return new CatalogRepository.Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> entries) {
                try {
                    if (id.trim().isEmpty()) {
                        throw new IllegalArgumentException("ID is required.");
                    }

                    if (name == null || name.trim().isEmpty()) {
                        throw new IllegalArgumentException("Name is required.");
                    }

                    for (T entry : entries) {
                        boolean sameEntry = id.equals(idOf.apply(entry));
                        boolean sameName = name.trim()
                                .equalsIgnoreCase(nameOf.apply(entry));

                        if (!sameEntry && sameName) {
                            throw new IllegalArgumentException(
                                    "An entry with this name already exists.");
                        }
                    }
                } catch (IllegalArgumentException error) {
                    callback.onError(error);
                    return;
                }

                try {
                    save.run();
                } catch (IllegalArgumentException error) {
                    callback.onError(error);
                }
            }

            @Override
            public void onError(Exception error) {
                callback.onError(error);
            }
        };
    }
}