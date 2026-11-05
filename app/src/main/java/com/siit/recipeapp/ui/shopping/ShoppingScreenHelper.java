package com.siit.recipeapp.ui.shopping;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.ui.preview.IngredientPreviewCatalog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ShoppingScreenHelper {
    static final String LIST_ID = "shopping_list_id";
    static final String ITEM_ID = "shopping_item_id";
    private ShoppingScreenHelper() { }

    static String date(LocalDateTime value) {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()));
    }

    static void open(Fragment source, Fragment destination, String listId, String itemId) {
        Bundle args = new Bundle();
        args.putString(LIST_ID, listId);
        args.putString(ITEM_ID, itemId);
        destination.setArguments(args);
        source.getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, destination).addToBackStack("shopping").commit();
    }

    static String ingredientName(String id) {
        for (Ingredient ingredient : IngredientPreviewCatalog.INGREDIENTS) {
            if (ingredient.getId().equals(id)) return ingredient.getName();
        }
        return id;
    }

    static String amount(ShoppingListItem item) {
        String label = item.getUnitId();
        for (MeasurementUnit unit : IngredientPreviewCatalog.UNITS) {
            if (unit.getId().equals(item.getUnitId())) label = unit.getAbbreviation();
        }
        return item.getQuantity().toPlainString() + " " + label;
    }
}
