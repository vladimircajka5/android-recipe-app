package com.siit.recipeapp.domain.validation;

import com.siit.recipeapp.data.model.account.Profile;
import com.siit.recipeapp.data.model.location.Location;

public final class ProfileValidator {

    private ProfileValidator() {
    }

    public static boolean isValid(Profile profile) {
        if (profile == null) {
            return false;
        }

        Location location = profile.getLocation();

        return LocationValidator.isValid(location) && location.hasValidRegion();
    }
}
