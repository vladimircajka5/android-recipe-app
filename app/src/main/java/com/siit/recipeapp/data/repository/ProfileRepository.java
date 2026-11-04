package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.account.Profile;

public interface ProfileRepository {
    Profile getProfile(String username);
    void updateProfile(String username, Profile profile);

}
