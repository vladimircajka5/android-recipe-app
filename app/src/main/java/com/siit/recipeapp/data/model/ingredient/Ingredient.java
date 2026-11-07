package com.siit.recipeapp.data.model.ingredient;

import java.util.Objects;


public final class Ingredient {
    private final String id;
    private final String name;
    private final String categoryId;
    private final String defaultUnitId;
    private final IngredientKind kind;

    public Ingredient(String id, String name, String categoryId, String defaultUnitId,
                      IngredientKind kind) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.defaultUnitId = defaultUnitId;
        this.kind = Objects.requireNonNull(kind, "Ingredient kind");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategoryId() { return categoryId; }
    public String getDefaultUnitId() { return defaultUnitId; }
    public IngredientKind getKind() { return kind; }
    @Override public String toString() { return name; }
}
