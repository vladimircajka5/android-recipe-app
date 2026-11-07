package com.siit.recipeapp.data.model.ingredient;

// A branded product linked to its generic or branded catalog ingredient
public final class Product {
    private final String id;
    private final String barcode;
    private final String brandName;
    private final Double packageQuantity;
    private final String unitId;
    private final String displayColor;
    private final String ingredientId;

    public Product(String id, String barcode, String brandName, Double packageQuantity,
                   String unitId, String displayColor, String ingredientId) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new IllegalArgumentException("Barcode is required");
        }
        if (packageQuantity != null && (!Double.isFinite(packageQuantity) || packageQuantity <= 0)) {
            throw new IllegalArgumentException("Package quantity must be greater than zero");
        }
        if (packageQuantity != null && (unitId == null || unitId.trim().isEmpty())) {
            throw new IllegalArgumentException("Choose a unit for the package quantity");
        }
        if (ingredientId == null || ingredientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Choose an ingredient");
        }
        this.id = id;
        this.barcode = barcode.trim();
        this.brandName = brandName == null || brandName.trim().isEmpty() ? null : brandName.trim();
        this.packageQuantity = packageQuantity;
        this.unitId = unitId;
        this.displayColor = displayColor;
        this.ingredientId = ingredientId;
    }

    public String getId() { return id; }
    public String getBarcode() { return barcode; }
    public String getBrandName() { return brandName; }
    public Double getPackageQuantity() { return packageQuantity; }
    public String getUnitId() { return unitId; }
    public String getDisplayColor() { return displayColor; }
    public String getIngredientId() { return ingredientId; }
}
