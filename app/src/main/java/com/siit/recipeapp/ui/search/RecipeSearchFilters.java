package com.siit.recipeapp.ui.search;

import android.os.Bundle;

import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Amount;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;

import java.util.ArrayList;
import java.util.List;

final class RecipeSearchFilters {
    static final int ANY = R.string.search_any;
    static final int INGREDIENTS_OFF = 0;
    static final int INGREDIENTS_MANUAL = 1;
    static final int INGREDIENTS_PANTRY = 2;

    static final String DISH = "dish";
    static final String DIET = "diet";
    static final String MEAL = "meal";
    static final String METHOD = "method";
    static final String MIN_TIME = "min_time";
    static final String MAX_TIME = "max_time";
    static final String MIN_UNIT = "min_unit";
    static final String MAX_UNIT = "max_unit";
    static final String ALLERGENS = "allergens";
    static final String INGREDIENT_SOURCE = "ingredient_source";
    static final String MANUAL_IDS = "manual_ids";
    static final String MAX_MISSING = "max_missing";

    private final Bundle values;

    RecipeSearchFilters(Bundle values) {
        this.values = values;
    }

    boolean matches(Recipe recipe) {
        return matchesChoice(DISH, recipe.dish)
                && matchesChoice(DIET, recipe.diet)
                && matchesChoice(MEAL, recipe.meal)
                && matchesChoice(METHOD, recipe.method)
                && recipe.minutes >= minutes(MIN_TIME, MIN_UNIT, 0)
                && recipe.minutes <= minutes(MAX_TIME, MAX_UNIT, Integer.MAX_VALUE)
                && !containsExcludedAllergen(recipe);
    }

    boolean usesIngredients() {
        return values.getInt(INGREDIENT_SOURCE) != INGREDIENTS_OFF;
    }

    int maxMissing() {
        return values.getInt(MAX_MISSING);
    }

    List<String> missingIngredients(Recipe recipe) {
        List<String> missing = new ArrayList<>();
        if (!usesIngredients()) return missing;

        List<String> available = values.getStringArrayList(MANUAL_IDS);
        if (values.getInt(INGREDIENT_SOURCE) == INGREDIENTS_PANTRY) {
            available = new ArrayList<>(RecipeSearchPreviewData.pantryIngredientIds());
        }
        for (Amount amount : recipe.ingredients) {
            if (available == null || !available.contains(amount.ingredientId)) {
                missing.add(RecipeSearchPreviewData.ingredientName(amount.ingredientId));
            }
        }
        return missing;
    }

    boolean hasAdvancedFilters() {
        List<Integer> excluded = values.getIntegerArrayList(ALLERGENS);
        return values.getInt(DISH, ANY) != ANY || values.getInt(DIET, ANY) != ANY
                || values.getInt(MEAL, ANY) != ANY || values.getInt(METHOD, ANY) != ANY
                || values.containsKey(MIN_TIME) || values.containsKey(MAX_TIME)
                || (excluded != null && !excluded.isEmpty()) || usesIngredients();
    }

    private boolean matchesChoice(String key, int choice) {
        int selected = values.getInt(key, ANY);
        return selected == ANY || selected == choice;
    }

    private boolean containsExcludedAllergen(Recipe recipe) {
        List<Integer> excluded = values.getIntegerArrayList(ALLERGENS);
        if (excluded == null) return false;
        for (int allergen : recipe.allergens) {
            if (excluded.contains(allergen)) return true;
        }
        return false;
    }

    private int minutes(String time, String unit, int fallback) {
        return values.containsKey(time)
                ? toMinutes(values.getInt(time), values.getInt(unit)) : fallback;
    }

    static int toMinutes(int value, int unit) {
        return unit == 1 ? value * 60 : value;
    }
}
