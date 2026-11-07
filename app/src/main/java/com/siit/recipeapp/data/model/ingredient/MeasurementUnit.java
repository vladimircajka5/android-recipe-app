package com.siit.recipeapp.data.model.ingredient;


public final class MeasurementUnit {
    private final String id;
    private final String name;
    private final String abbreviation;

    public MeasurementUnit(String id, String name, String abbreviation) {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAbbreviation() { return abbreviation; }
    @Override public String toString() { return name + " (" + abbreviation + ")"; }
}
