package com.siit.recipeapp.data.model.location;

public class Location {
    private String country;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;

    // Constructor

    public Location(String country, String region, String city, Double latitude, Double longitude) {
        this.country = country;
        this.region = region;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    // Getters
    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


    // Setters
    public void setCountry(String country) {
        this.country = country;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Helpers
    public boolean hasValidRegion() {
        return region != null && !region.trim().isEmpty();
    }
}
