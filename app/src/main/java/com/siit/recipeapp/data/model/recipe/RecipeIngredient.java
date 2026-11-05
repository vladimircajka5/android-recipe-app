package com.siit.recipeapp.data.model.recipe;

public class RecipeIngredient {

    // Graf
    private Double quantity;
    private int position;
    // Veze
    private String ingredientId;
    private String unitId;

    // Constructor
    public RecipeIngredient(Double quantity, int position, String ingredientId, String unitId) {
        this.quantity = quantity;
        this.position = position;
        this.ingredientId = ingredientId;
        this.unitId = unitId;
    }

    // Getters
    public Double getQuantity() {
        return quantity;
    }

    public int getPosition() {
        return position;
    }

    public String getingredientId() {
        return ingredientId;
    }

    public String getUnitId() {
        return unitId;
    }

    // Setters

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setingredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }
}
