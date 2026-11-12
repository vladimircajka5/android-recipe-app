package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.catalog.*;
import java.util.List;

public interface CatalogRepository {
    List<IngredientCategory> getIngredientCategories();
    List<Allergen> getAllergens();
    List<MealType> getMealTypes();
    List<DishType> getDishTypes();
    List<DietType> getDietTypes();
    void saveIngredientCategory(IngredientCategory value);
    void deleteIngredientCategory(String id);
    void saveAllergen(Allergen value);
    void deleteAllergen(String id);
    void saveMealType(MealType value);
    void deleteMealType(String id);
    void saveDishType(DishType value);
    void deleteDishType(String id);
    void saveDietType(DietType value);
    void deleteDietType(String id);
}
