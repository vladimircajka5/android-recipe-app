package com.siit.recipeapp.data.repository.firebase;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import com.siit.recipeapp.data.model.catalog.Allergen;
import com.siit.recipeapp.data.model.catalog.DietType;
import com.siit.recipeapp.data.model.catalog.DishType;
import com.siit.recipeapp.data.model.catalog.IngredientCategory;
import com.siit.recipeapp.data.model.catalog.MealType;
import com.siit.recipeapp.data.repository.CatalogRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FirebaseCatalogRepository implements CatalogRepository {

    private final FirebaseFirestore firestore;

    public FirebaseCatalogRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    private interface EntryMapper<T> {
        T create(String id, String name, String description);
    }

    @Override
    public void getIngredientCategories(
            Callback<List<IngredientCategory>> callback) {

        read("ingredientCategories", IngredientCategory::new, callback);
    }

    @Override
    public void getAllergens(Callback<List<Allergen>> callback) {
        read("allergens", Allergen::new, callback);
    }

    @Override
    public void getMealTypes(Callback<List<MealType>> callback) {
        read("mealTypes", MealType::new, callback);
    }

    @Override
    public void getDishTypes(Callback<List<DishType>> callback) {
        read("dishTypes", DishType::new, callback);
    }

    @Override
    public void getDietTypes(Callback<List<DietType>> callback) {
        read("dietTypes", DietType::new, callback);
    }

    @Override
    public void saveIngredientCategory(
            IngredientCategory value, Callback<Void> callback) {

        save(
                "ingredientCategories",
                value.getId(),
                value.getName(),
                value.getDescription(),
                callback
        );
    }

    @Override
    public void saveAllergen(Allergen value, Callback<Void> callback) {
        save(
                "allergens",
                value.getId(),
                value.getName(),
                value.getDescription(),
                callback
        );
    }

    @Override
    public void saveMealType(MealType value, Callback<Void> callback) {
        save(
                "mealTypes",
                value.getId(),
                value.getName(),
                value.getDescription(),
                callback
        );
    }

    @Override
    public void saveDishType(DishType value, Callback<Void> callback) {
        save(
                "dishTypes",
                value.getId(),
                value.getName(),
                value.getDescription(),
                callback
        );
    }

    @Override
    public void saveDietType(DietType value, Callback<Void> callback) {
        save(
                "dietTypes",
                value.getId(),
                value.getName(),
                value.getDescription(),
                callback
        );
    }

    @Override
    public void deleteIngredientCategory(String id, Callback<Void> callback) {
        delete("ingredientCategories", id, callback);
    }

    @Override
    public void deleteAllergen(String id, Callback<Void> callback) {
        delete("allergens", id, callback);
    }

    @Override
    public void deleteMealType(String id, Callback<Void> callback) {
        delete("mealTypes", id, callback);
    }

    @Override
    public void deleteDishType(String id, Callback<Void> callback) {
        delete("dishTypes", id, callback);
    }

    @Override
    public void deleteDietType(String id, Callback<Void> callback) {
        delete("dietTypes", id, callback);
    }

    private <T> void read(
            String collection,
            EntryMapper<T> mapper,
            Callback<List<T>> callback) {

        firestore.collection(collection)
                .orderBy("name")
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    List<T> entries = new ArrayList<>();

                    try {
                        for (DocumentSnapshot document :
                                snapshot.getDocuments()) {

                            entries.add(mapper.create(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("description")
                            ));
                        }
                    } catch (RuntimeException error) {
                        callback.onError(error);
                        return;
                    }

                    callback.onSuccess(entries);
                })
                .addOnFailureListener(callback::onError);
    }

    private void save(
            String collection,
            String id,
            String name,
            String description,
            Callback<Void> callback) {

        if (!validId(id)) {
            callback.onError(new IllegalArgumentException(
                    "Invalid catalog entry ID."));
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException(
                    "Name is required."));
            return;
        }

        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name.trim());
        values.put(
                "description",
                description == null ? "" : description.trim()
        );

        firestore.collection(collection)
                .document(id)
                .set(values)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    private void delete(
            String collection,
            String id,
            Callback<Void> callback) {

        if (!validId(id)) {
            callback.onError(new IllegalArgumentException(
                    "Invalid catalog entry ID."));
            return;
        }

        firestore.collection(collection)
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    private boolean validId(String id) {
        return id != null
                && !id.trim().isEmpty()
                && !id.contains("/")
                && !id.equals(".")
                && !id.equals("..");
    }
}