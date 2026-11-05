package com.siit.recipeapp.ui.preview;

import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ShoppingPreviewData {
    public static final String OWNER_ID = "shopping-preview";
    private ShoppingPreviewData() { }

    public static List<ShoppingList> initialLists() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                new ShoppingList("weekly", OWNER_ID, "Weekly groceries", now, null, true, ShoppingListStatus.ACTIVE),
                new ShoppingList("dinner", OWNER_ID, "Dinner ingredients", now, now.plusDays(1), true, ShoppingListStatus.ACTIVE));
    }

    public static List<ShoppingListItem> initialItems() {
        return List.of(
                new ShoppingListItem("weekly-milk", "weekly", "milk", BigDecimal.ONE, "l", false),
                new ShoppingListItem("weekly-eggs", "weekly", "egg", new BigDecimal("6"), "piece", true),
                new ShoppingListItem("dinner-pasta", "dinner", "pasta", new BigDecimal("500"), "g", false));
    }
}
