package com.siit.recipeapp.ui.map;

import android.os.Bundle;

import com.siit.recipeapp.data.model.location.Location;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class RecipeMapPreviewData {

    static final int ORIGIN = 0;
    static final int AUTHOR = 1;

    static final class Entry {
        final Recipe recipe;
        final String author;
        final Location origin;
        final Location authorRegion;
        final Double rating;

        Entry(
                String recipeId,
                Location origin,
                Location authorRegion,
                Double rating) {

            recipe = RecipeSearchPreviewData.findRecipe(recipeId);

            if (recipe == null) {
                throw new IllegalArgumentException(
                        "Unknown preview recipe: " + recipeId);
            }

            author = "Alex";
            this.origin = origin;
            this.authorRegion = authorRegion;
            this.rating = rating;
        }

        Location location(int source) {
            return source == AUTHOR ? authorRegion : origin;
        }
    }

    static final class Place {
        final String country;
        final String region;

        Place(String country, String region) {
            this.country = country;
            this.region = region;
        }

        String label() {
            return region.isEmpty()
                    ? country
                    : region + " · " + country;
        }
    }

    static final class Filters {
        int source = ORIGIN;
        String country = "";
        String region = "";
        int dish;
        int meal;
        int maxMinutes;
        double minRating;

        Filters() {
        }

        Filters(Bundle values) {
            if (values == null) {
                return;
            }

            source = values.getInt("source") == AUTHOR ? AUTHOR : ORIGIN;
            country = values.getString("country", "");
            region = values.getString("region", "");
            dish = values.getInt("dish");
            meal = values.getInt("meal");
            maxMinutes = values.getInt("max_minutes");
            minRating = values.getDouble("min_rating");
        }

        Bundle save() {
            Bundle values = new Bundle();
            values.putInt("source", source);
            values.putString("country", country);
            values.putString("region", region);
            values.putInt("dish", dish);
            values.putInt("meal", meal);
            values.putInt("max_minutes", maxMinutes);
            values.putDouble("min_rating", minRating);
            return values;
        }

        int count() {
            return (country.isEmpty() ? 0 : 1)
                    + (region.isEmpty() ? 0 : 1)
                    + (dish == 0 ? 0 : 1)
                    + (meal == 0 ? 0 : 1)
                    + (maxMinutes == 0 ? 0 : 1)
                    + (minRating == 0 ? 0 : 1);
        }
    }

    private static final Location AUTHOR_LOCATION =
            new Location(
                    "Serbia", "Vojvodina", "Novi Sad",
                    45.2671, 19.8335);

    private static final List<Entry> ENTRIES = List.of(
            new Entry(
                    "pancakes",
                    new Location(
                            "Serbia", "Vojvodina", "Novi Sad",
                            45.2671, 19.8335),
                    AUTHOR_LOCATION,
                    4.7),

            new Entry(
                    "pasta",
                    new Location(
                            "Italy", "Sicily", "Palermo",
                            38.1157, 13.3615),
                    AUTHOR_LOCATION,
                    4.9),

            new Entry(
                    "rice",
                    new Location(
                            "Italy", "Lazio", "Rome",
                            41.9028, 12.4964),
                    AUTHOR_LOCATION,
                    4.5)
    );

    private RecipeMapPreviewData() {
    }

    static List<Entry> select(Filters filters) {
        List<Entry> result = new ArrayList<>();

        for (Entry entry : ENTRIES) {
            Location location = entry.location(filters.source);

            if (location == null) {
                continue;
            }

            if (!filters.country.isEmpty()
                    && !filters.country.equals(location.getCountry())) {
                continue;
            }

            if (!filters.region.isEmpty()
                    && !filters.region.equals(location.getRegion())) {
                continue;
            }

            if (filters.dish != 0
                    && filters.dish != entry.recipe.dish) {
                continue;
            }

            if (filters.meal != 0
                    && filters.meal != entry.recipe.meal) {
                continue;
            }

            if (filters.maxMinutes > 0
                    && entry.recipe.minutes > filters.maxMinutes) {
                continue;
            }

            if (filters.minRating > 0
                    && (entry.rating == null
                    || entry.rating < filters.minRating)) {
                continue;
            }

            result.add(entry);
        }

        return result;
    }

    static Map<String, Integer> groups(
            List<Entry> entries,
            Filters filters) {

        Map<String, Integer> groups = new TreeMap<>();

        for (Entry entry : entries) {
            Location location = entry.location(filters.source);

            String label = filters.country.isEmpty()
                    ? location.getCountry()
                    : location.getRegion();

            groups.put(label, groups.getOrDefault(label, 0) + 1);
        }

        return groups;
    }

    static List<Place> places(int source) {
        Map<String, Place> unique = new TreeMap<>();

        for (Entry entry : ENTRIES) {
            Location location = entry.location(source);

            if (location == null) {
                continue;
            }

            String country = location.getCountry();
            String region = location.getRegion();

            unique.put(
                    "country/" + country,
                    new Place(country, ""));

            if (region != null && !region.isEmpty()) {
                unique.put(
                        "region/" + country + "/" + region,
                        new Place(country, region));
            }
        }

        List<Place> result = new ArrayList<>(unique.values());
        result.sort(Comparator.comparing(Place::label));
        return result;
    }

    static List<String> countries(int source) {
        Set<String> countries = new TreeSet<>();

        for (Place place : places(source)) {
            countries.add(place.country);
        }

        return new ArrayList<>(countries);
    }

    static List<Place> regions(int source, String country) {
        List<Place> regions = new ArrayList<>();

        for (Place place : places(source)) {
            if (!place.region.isEmpty()
                    && (country.isEmpty()
                    || country.equals(place.country))) {
                regions.add(place);
            }
        }

        return regions;
    }

    static List<Integer> types(boolean dish) {
        Set<Integer> types = new LinkedHashSet<>();
        types.add(0);

        for (Entry entry : ENTRIES) {
            types.add(dish ? entry.recipe.dish : entry.recipe.meal);
        }

        return new ArrayList<>(types);
    }

    static Place findPlace(int source, String query) {
        String text = query.trim();
        Place regionMatch = null;

        for (Place place : places(source)) {
            if (place.label().equalsIgnoreCase(text)) {
                return place;
            }

            if (!place.region.isEmpty()
                    && place.region.equalsIgnoreCase(text)) {
                if (regionMatch != null) {
                    return null;
                }

                regionMatch = place;
            }
        }

        return regionMatch;
    }
}