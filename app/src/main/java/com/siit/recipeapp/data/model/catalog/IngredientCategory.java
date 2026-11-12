package com.siit.recipeapp.data.model.catalog;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class IngredientCategory {
    private final String id;
    private String name;
    private final String description;

    public IngredientCategory(String id, String name, String description){
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.description = description == null ? "":description.trim();
    }

    private static String requireText(String value, String field){
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required");{
            return value.trim();
        }
    }

    public String getId(){ return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object value) {
        return value instanceof IngredientCategory && id.equals(((IngredientCategory)value).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}