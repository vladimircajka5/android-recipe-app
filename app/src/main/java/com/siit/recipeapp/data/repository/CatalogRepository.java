package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.catalog.*;
import java.util.List;

public interface CatalogRepository {

    interface Callback<T> {
        void onSuccess(T value);
        void onError(Exception error);
    }

    final class EntryInUseException extends IllegalStateException {
        public EntryInUseException() {
            super("Catalog entry is in use.");
        }
    }

    void getIngredientCategories(
            Callback<List<IngredientCategory>> callback);

    void getAllergens(Callback<List<Allergen>> callback);

    void getMealTypes(Callback<List<MealType>> callback);

    void getDishTypes(Callback<List<DishType>> callback);

    void getDietTypes(Callback<List<DietType>> callback);

    void saveIngredientCategory(
            IngredientCategory value, Callback<Void> callback);

    void saveAllergen(Allergen value, Callback<Void> callback);

    void saveMealType(MealType value, Callback<Void> callback);

    void saveDishType(DishType value, Callback<Void> callback);

    void saveDietType(DietType value, Callback<Void> callback);

    void deleteIngredientCategory(String id, Callback<Void> callback);

    void deleteAllergen(String id, Callback<Void> callback);

    void deleteMealType(String id, Callback<Void> callback);

    void deleteDishType(String id, Callback<Void> callback);

    void deleteDietType(String id, Callback<Void> callback);
}
