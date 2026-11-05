package com.siit.recipeapp.data.model.shopping;

import java.math.BigDecimal;

/** A shopping line requires an explicit positive quantity and a unit. */
public final class ShoppingListItem {
    private final String id;
    private final String shoppingListId;
    private final String ingredientId;
    private final BigDecimal quantity;
    private final String unitId;
    private final boolean purchased;

    public ShoppingListItem(String id, String shoppingListId, String ingredientId,
                            BigDecimal quantity, String unitId, boolean purchased) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitId == null || unitId.trim().isEmpty()) {
            throw new IllegalArgumentException("Choose a measurement unit");
        }
        this.id = id;
        this.shoppingListId = shoppingListId;
        this.ingredientId = ingredientId;
        this.quantity = quantity.stripTrailingZeros();
        this.unitId = unitId;
        this.purchased = purchased;
    }

    public String getId() { return id; }
    public String getShoppingListId() { return shoppingListId; }
    public String getIngredientId() { return ingredientId; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnitId() { return unitId; }
    public boolean isPurchased() { return purchased; }
}
