package com.siit.recipeapp.domain.validation;

import com.siit.recipeapp.data.model.location.Location;

public final class LocationValidator {

    private LocationValidator() {

    }

    public static boolean isValid(Location location) {

        if (location == null) {
            return false;
        }

        // Provera da li ima uneto bilo sta od: drzava, region, grad
        boolean hasName =
                !isBlank(location.getCountry()) ||
                !isBlank(location.getRegion()) ||
                !isBlank(location.getCity());

        if (!hasName) {
            return false;
        }

        return areCoordinatesValid(location.getLatitude(), location.getLongitude());
    }

    // Vraca true ako su unete obe u dozvoljenim opsezima ili nisu uopste unete.
    // Vraca false za bilo sta drugo.
    public static boolean areCoordinatesValid(
            Double latitude, Double longitude) {

        if (latitude == null && longitude == null) {
            return true;
        }

        if (latitude == null || longitude == null) {
            return false;
        }

        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
