package com.siit.recipeapp.data.repository.inmemory;

import com.siit.recipeapp.data.model.catalog.*;
import com.siit.recipeapp.data.repository.CatalogRepository;
import java.util.ArrayList;
import java.util.List;


// In-memory pantry catalog, will be replaced with persistent version later
public final class InMemoryCatalogRepository implements CatalogRepository {
    private final List<IngredientCategory> categories = new ArrayList<>();
    private final List<Allergen> allergens = new ArrayList<>();
    private final List<MealType> meals = new ArrayList<>();
    private final List<DishType> dishes = new ArrayList<>();
    private final List<DietType> diets = new ArrayList<>();

    public InMemoryCatalogRepository() {
        saveIngredientCategory(new IngredientCategory("cat-vegetables", "Povrće", "Sveže i termički obrađeno povrće"));
        saveIngredientCategory(new IngredientCategory("cat-fruit", "Voće", ""));
        saveIngredientCategory(new IngredientCategory("cat-meat", "Meso", ""));
        saveIngredientCategory(new IngredientCategory("cat-fish", "Riba i morski plodovi", ""));
        saveIngredientCategory(new IngredientCategory("cat-dairy", "Mlečni proizvodi", ""));
        saveIngredientCategory(new IngredientCategory("cat-grains", "Žitarice", ""));
        saveIngredientCategory(new IngredientCategory("cat-spices", "Začini", ""));
        saveIngredientCategory(new IngredientCategory("cat-nuts", "Orašasti plodovi", ""));
        saveAllergen(new Allergen("allergen-gluten", "Gluten", ""));
        saveAllergen(new Allergen("allergen-milk", "Mleko", ""));
        saveAllergen(new Allergen("allergen-eggs", "Jaja", ""));
        saveAllergen(new Allergen("allergen-peanuts", "Kikiriki", ""));
        saveMealType(new MealType("meal-breakfast", "Doručak", ""));
        saveMealType(new MealType("meal-lunch", "Ručak", ""));
        saveMealType(new MealType("meal-dinner", "Večera", ""));
        saveDishType(new DishType("dish-main", "Glavno jelo", ""));
        saveDishType(new DishType("dish-dessert", "Dezert", ""));
        saveDishType(new DishType("dish-soup", "Supa", ""));
        saveDietType(new DietType("diet-vegetarian", "Vegetarijansko", ""));
        saveDietType(new DietType("diet-vegan", "Vegansko", ""));
        saveDietType(new DietType("diet-fasting", "Posno", ""));
        saveDietType(new DietType("diet-pescetarian", "Peskatarijansko", ""));
    }
    private <T> List<T> copy(List<T> source) { return new ArrayList<>(source); }
    private <T> void save(List<T> list, T value, java.util.function.Function<T,String> id) {
        for (int i=0;i<list.size();i++) if (id.apply(list.get(i)).equals(id.apply(value))) { list.set(i,value); return; }
        list.add(value);
    }
    private <T> void delete(List<T> list, String id, java.util.function.Function<T,String> key) { list.removeIf(v -> key.apply(v).equals(id)); }
    @Override public List<IngredientCategory> getIngredientCategories() { return copy(categories); }
    @Override public List<Allergen> getAllergens() { return copy(allergens); }
    @Override public List<MealType> getMealTypes() { return copy(meals); }
    @Override public List<DishType> getDishTypes() { return copy(dishes); }
    @Override public List<DietType> getDietTypes() { return copy(diets); }
    @Override public void saveIngredientCategory(IngredientCategory v) { save(categories,v,IngredientCategory::getId); }
    @Override public void deleteIngredientCategory(String id) { delete(categories,id,IngredientCategory::getId); }
    @Override public void saveAllergen(Allergen v) { save(allergens,v,Allergen::getId); }
    @Override public void deleteAllergen(String id) { delete(allergens,id,Allergen::getId); }
    @Override public void saveMealType(MealType v) { save(meals,v,MealType::getId); }
    @Override public void deleteMealType(String id) { delete(meals,id,MealType::getId); }
    @Override public void saveDishType(DishType v) { save(dishes,v,DishType::getId); }
    @Override public void deleteDishType(String id) { delete(dishes,id,DishType::getId); }
    @Override public void saveDietType(DietType v) { save(diets,v,DietType::getId); }
    @Override public void deleteDietType(String id) { delete(diets,id,DietType::getId); }
}
