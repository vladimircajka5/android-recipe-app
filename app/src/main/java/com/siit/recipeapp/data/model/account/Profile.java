package com.siit.recipeapp.data.model.account;

import com.siit.recipeapp.data.model.location.Location;

public class Profile {
    private String biography;
    private Location location;

    // Constructor
    public Profile(String biography, Location location) {
        this.biography = biography;
        this.location = location;
    }

    // Getters
    public String getBiography() {
        return biography;
    }

    public Location getLocation() {
        return location;
    }

    // Setters
    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
