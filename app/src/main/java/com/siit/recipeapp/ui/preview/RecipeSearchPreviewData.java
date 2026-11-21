package com.siit.recipeapp.ui.preview;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.pantry.PantryItem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RecipeSearchPreviewData {
    private RecipeSearchPreviewData() {}

    public static final class Amount {
        public final String ingredientId;
        public final String quantity;

        private Amount(String ingredientId, String quantity) {
            this.ingredientId = ingredientId;
            this.quantity = quantity;
        }
    }

    public static final class Recipe {
        public final String id;
        @StringRes public final int title;
        @StringRes public final int description;
        @StringRes public final int dish;
        @StringRes public final int diet;
        @StringRes public final int meal;
        @StringRes public final int method;
        @StringRes public final int directions;
        @DrawableRes public final int photo;
        public final int minutes;
        public final int servings;
        public final List<Integer> allergens;
        public final List<Amount> ingredients;

        private Recipe(String id, int title, int description, int dish, int diet, int meal,
                       int method, int directions, int photo, int minutes, int servings,
                       List<Integer> allergens, List<Amount> ingredients) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dish = dish;
            this.diet = diet;
            this.meal = meal;
            this.method = method;
            this.directions = directions;
            this.photo = photo;
            this.minutes = minutes;
            this.servings = servings;
            this.allergens = allergens;
            this.ingredients = ingredients;
        }
    }

    public static final List<Recipe> RECIPES = List.of(
            new Recipe("pancakes", R.string.search_sample_pancakes, R.string.search_sample_pancakes_description,
                    R.string.search_dish_dessert, R.string.search_diet_vegetarian,
                    R.string.search_meal_breakfast, R.string.search_method_frying,
                    R.string.search_sample_pancakes_steps, R.drawable.recipe_sample_pancakes, 25, 4,
                    List.of(R.string.search_allergen_gluten, R.string.search_allergen_milk,
                            R.string.search_allergen_egg),
                    List.of(new Amount("flour", "200 g"), new Amount("milk", "300 ml"),
                            new Amount("egg", "2 pcs"))),
            new Recipe("pasta", R.string.search_sample_pasta, R.string.search_sample_pasta_description,
                    R.string.search_dish_main, R.string.search_diet_vegetarian,
                    R.string.search_meal_lunch, R.string.search_method_boiling,
                    R.string.search_sample_pasta_steps, 0, 35, 2,
                    List.of(R.string.search_allergen_gluten, R.string.search_allergen_milk),
                    List.of(new Amount("pasta", "250 g"), new Amount("tomato", "2 pcs"),
                            new Amount("cheese", "80 g"))),
            new Recipe("rice", R.string.search_sample_rice, R.string.search_sample_rice_description,
                    R.string.search_dish_main, R.string.search_diet_vegan,
                    R.string.search_meal_dinner, R.string.search_method_baking,
                    R.string.search_sample_rice_steps, 0, 65, 4, List.of(),
                    List.of(new Amount("rice", "300 g"), new Amount("tomato", "2 pcs"),
                            new Amount("pepper", "5 g")))
    );

    public static Recipe findRecipe(String id) {
        for (Recipe recipe : RECIPES) {
            if (recipe.id.equals(id)) return recipe;
        }
        return null;
    }

    public static Set<String> pantryIngredientIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (PantryItem item : PantryPreviewData.initialItems()) {
            ids.add(item.getIngredientId());
        }
        return ids;
    }

    public static String ingredientName(String id) {
        for (Ingredient ingredient : IngredientPreviewCatalog.INGREDIENTS) {
            if (ingredient.getId().equals(id)) return ingredient.getName();
        }
        return id;
    }
}
