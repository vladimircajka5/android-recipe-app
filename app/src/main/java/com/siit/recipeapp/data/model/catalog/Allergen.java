package com.siit.recipeapp.data.model.catalog;

import java.util.Objects;

public final class Allergen {
    private final String id;
    private final String name;
    private final String description;

    public Allergen(String id, String name, String description) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id is required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name is required");
        this.id = id.trim();
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public boolean equals(Object value) {
        return value instanceof Allergen && id.equals(((Allergen)value).id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
