package com.siit.recipeapp.ui.pantry;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.IngredientKind;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Preview class for creating dummy pantry data
public final class PantryPreviewData {
    public static final String OWNER_ID = "pantry-preview";

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

    private PantryPreviewData() { }

    public static List<PantryItem> initialItems() {
        return List.of(
                new PantryItem("preview-flour", OWNER_ID, "flour", null,
                        new BigDecimal("500"), "g", null),
                new PantryItem("preview-milk", OWNER_ID, "milk", "Sample dairy",
                        BigDecimal.ONE, "l", LocalDate.now().plusDays(3)),
                new PantryItem("preview-pepper", OWNER_ID, "pepper", null,
                        null, null, null));
    }

    private static Ingredient ingredient(String id, String name, String category, String unit) {
        return new Ingredient(id, name, category, unit, IngredientKind.GENERIC);
    }
}
