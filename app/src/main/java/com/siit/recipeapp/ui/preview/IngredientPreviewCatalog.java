package com.siit.recipeapp.ui.preview;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.IngredientKind;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import java.util.List;

/** Shared catalog fixtures for UI milestones; no data access or persistence. */
public final class IngredientPreviewCatalog {
    public static final List<MeasurementUnit> UNITS = List.of(
            new MeasurementUnit("g", "Gram", "g"),
            new MeasurementUnit("kg", "Kilogram", "kg"),
            new MeasurementUnit("l", "Litre", "l"),
            new MeasurementUnit("ml", "Millilitre", "ml"),
            new MeasurementUnit("piece", "Piece", "pcs"));

    public static final List<Ingredient> INGREDIENTS = List.of(
            ingredient("apple", "Apple", "fruit", "piece"),
            ingredient("cheese", "Cheese", "dairy", "g"),
            ingredient("egg", "Egg", "protein", "piece"),
            ingredient("flour", "Flour", "grains", "g"),
            ingredient("milk", "Milk", "dairy", "ml"),
            ingredient("onion", "Onion", "vegetables", "piece"),
            ingredient("pasta", "Pasta", "grains", "g"),
            ingredient("pepper", "Pepper", "spices", "g"),
            ingredient("potato", "Potato", "vegetables", "kg"),
            ingredient("rice", "Rice", "grains", "g"),
            ingredient("salt", "Salt", "spices", "g"),
            ingredient("tomato", "Tomato", "vegetables", "piece"));


    private IngredientPreviewCatalog() { }
    private static Ingredient ingredient(String id, String name, String category, String unit) {
        return new Ingredient(id, name, category, unit, IngredientKind.GENERIC);
    }
}
