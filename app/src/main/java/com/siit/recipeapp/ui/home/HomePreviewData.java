package com.siit.recipeapp.ui.home;

import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Amount;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class HomePreviewData {

    static final int POPULAR = 0;
    static final int NEWEST = 1;
    static final int VIEWED = 2;
    static final int COOKED = 3;
    static final int CAN_MAKE = 4;
    static final int MODE_COUNT = 5;

    private static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;
    private static final long PREVIEW_NOW = System.currentTimeMillis();

    static final class Entry {
        final Recipe recipe;
        final double monthlyAverage;
        final int monthlyRatingCount;
        final long publishedAt;
        final long viewedAt;
        final long cookingStartedAt;

        Entry(
                Recipe recipe,
                double monthlyAverage,
                int monthlyRatingCount,
                long publishedAt,
                long viewedAt,
                long cookingStartedAt) {

            this.recipe = recipe;
            this.monthlyAverage = monthlyAverage;
            this.monthlyRatingCount = monthlyRatingCount;
            this.publishedAt = publishedAt;
            this.viewedAt = viewedAt;
            this.cookingStartedAt = cookingStartedAt;
        }
    }

    private static final List<Entry> ENTRIES = createEntries();

    private HomePreviewData() {
    }

    static boolean isPersonalMode(int mode) {
        return mode == VIEWED || mode == COOKED || mode == CAN_MAKE;
    }

    static List<Entry> select(int mode) {
        List<Entry> result = new ArrayList<>(ENTRIES);

        switch (mode) {
            case NEWEST:
                result.sort(
                        Comparator.comparingLong(
                                        (Entry entry) -> entry.publishedAt)
                                .reversed());
                break;

            case VIEWED:
                long cutoff = PREVIEW_NOW - 7L * DAY;
                result.removeIf(entry ->
                        entry.viewedAt == 0L || entry.viewedAt < cutoff);
                result.sort(
                        Comparator.comparingLong(
                                        (Entry entry) -> entry.viewedAt)
                                .reversed());
                break;

            case COOKED:
                result.removeIf(entry -> entry.cookingStartedAt == 0L);
                result.sort(
                        Comparator.comparingLong(
                                        (Entry entry) -> entry.cookingStartedAt)
                                .reversed());
                break;

            case CAN_MAKE:
                // UI demonstration using existing sample data only.
                // This does not read the signed-in user's pantry.
                Set<String> available =
                        RecipeSearchPreviewData.pantryIngredientIds();

                result.removeIf(entry ->
                        !hasSampleIngredients(entry.recipe, available));
                break;

            case POPULAR:
            default:
                result.sort(
                        Comparator.comparingDouble(
                                        (Entry entry) -> entry.monthlyAverage)
                                .reversed());
                break;
        }

        return result;
    }

    private static boolean hasSampleIngredients(
            Recipe recipe,
            Set<String> available) {

        if (recipe.ingredients.isEmpty()) {
            return false;
        }

        for (Amount amount : recipe.ingredients) {
            if (!available.contains(amount.ingredientId)) {
                return false;
            }
        }

        return true;
    }

    private static List<Entry> createEntries() {
        List<Entry> result = new ArrayList<>();

        add(result, "pancakes", 4.7, 21,
                PREVIEW_NOW - DAY,
                PREVIEW_NOW - 2L * HOUR,
                PREVIEW_NOW - 3L * DAY);

        add(result, "pasta", 4.9, 34,
                PREVIEW_NOW - 4L * DAY,
                PREVIEW_NOW - 3L * DAY,
                0L);

        add(result, "rice", 4.5, 8,
                PREVIEW_NOW - 2L * DAY,
                PREVIEW_NOW - 9L * DAY,
                PREVIEW_NOW - DAY);

        return result;
    }

    private static void add(
            List<Entry> result,
            String recipeId,
            double monthlyAverage,
            int monthlyRatingCount,
            long publishedAt,
            long viewedAt,
            long cookingStartedAt) {

        Recipe recipe = RecipeSearchPreviewData.findRecipe(recipeId);

        if (recipe != null) {
            result.add(new Entry(
                    recipe,
                    monthlyAverage,
                    monthlyRatingCount,
                    publishedAt,
                    viewedAt,
                    cookingStartedAt));
        }
    }
}