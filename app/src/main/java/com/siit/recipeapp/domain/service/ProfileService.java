
package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.account.Profile;
import com.siit.recipeapp.data.model.location.Location;
import com.siit.recipeapp.domain.validation.ProfileValidator;

public class ProfileService {

    public Profile prepareUpdate(Profile currentProfile, String biography, Location location) {

        if (currentProfile == null) {
            throw new IllegalArgumentException("Current profile cannot be null");
        }

        Profile updatedProfile = new Profile(biography, location);

        if (!ProfileValidator.isValid(updatedProfile)) {
            throw new IllegalArgumentException("Invalid profile data");
        }

        return updatedProfile;
    }
}
