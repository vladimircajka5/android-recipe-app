
package com.siit.recipeapp.domain.validation;

import com.siit.recipeapp.data.model.account.Profile;
import com.siit.recipeapp.data.model.location.Location;

public final class ProfileValidator {

    private ProfileValidator() {
    }

    public static boolean isValid(Profile profile) {

        if (profile == null || profile.getLocation() == null) {
            return false;
        }

        Location location = profile.getLocation();

        // Sva tekstualna polja lokacije su opciona
        // Koordinate, ako postoje, moraju biti ispravne
        return LocationValidator.areCoordinatesValid(
                location.getLatitude(),
                location.getLongitude()
        );
    }
}
