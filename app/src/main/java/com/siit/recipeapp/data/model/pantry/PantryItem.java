package com.siit.recipeapp.data.model.pantry;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable stock record. null quantity means unknown, never zero. */
public final class PantryItem {
    private final String id;
    private final String ownerId;
    private final String ingredientId;
    private final String brand;
    private final BigDecimal quantity;
    private final String unitId;
    private final LocalDate expirationDate;

    public PantryItem(String id, String ownerId, String ingredientId, String brand,
                      BigDecimal quantity, String unitId, LocalDate expirationDate) {
        this.id = id;
        this.ownerId = ownerId;
        this.ingredientId = ingredientId;
        this.brand = brand;
        this.unitId = unitId;
        if (quantity != null && quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (quantity != null && (this.unitId == null || this.unitId.trim().isEmpty())) {
            throw new IllegalArgumentException("Choose a unit for the quantity");
        }
        this.quantity = quantity == null ? null : quantity.stripTrailingZeros();
        this.expirationDate = expirationDate;
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getIngredientId() { return ingredientId; }
    public String getBrand() { return brand; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnitId() { return unitId; }
    public LocalDate getExpirationDate() { return expirationDate; }
}
