package com.siit.recipeapp.ui.preview;

import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.time.LocalDate;
import java.util.List;

// Preview class for creating dummy pantry data
public final class PantryPreviewData {
    public static final String OWNER_ID = "pantry-preview";

    public static final List<MeasurementUnit> UNITS = IngredientPreviewCatalog.UNITS;
    public static final List<Ingredient> INGREDIENTS = IngredientPreviewCatalog.INGREDIENTS;

    private PantryPreviewData() { }

    public static List<PantryItem> initialItems() {
        return List.of(
                new PantryItem("preview-flour", OWNER_ID, "flour", null,
                        500.0, "g", null),
                new PantryItem("preview-milk", OWNER_ID, "milk", "Sample dairy",
                        1.0, "l", LocalDate.now().plusDays(3)),
                new PantryItem("preview-pepper", OWNER_ID, "pepper", null,
                        null, null, null));
    }

}
