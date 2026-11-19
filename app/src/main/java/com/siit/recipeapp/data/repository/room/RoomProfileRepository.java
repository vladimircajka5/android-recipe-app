package com.siit.recipeapp.data.repository.room;

import android.content.Context;

import com.siit.recipeapp.RecipeApplication;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.UserProfileEntity;
import com.siit.recipeapp.data.model.account.Profile;
import com.siit.recipeapp.data.model.location.Location;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.ProfileRepository;
import com.siit.recipeapp.domain.validation.ProfileValidator;

import java.util.UUID;

public final class RoomProfileRepository implements ProfileRepository {

    private final RecipeDatabase database;
    private final Context context;
    private final String ownerId;

    public RoomProfileRepository(RecipeDatabase database, Context context) {
        this.database = database;
        this.context = context.getApplicationContext();
        this.ownerId = UserDataSession.currentOwnerId(this.context);
    }

    private boolean isActiveOwner() {
        return ownerId != null
                && ownerId.equals(UserDataSession.currentOwnerId(context));
    }

    @Override
    public Profile getProfile(String username) {
        if (!isActiveOwner()) {
            return null;
        }

        UserProfileEntity entity = database.userProfileDao()
                .getProfile(ownerId);

        if (entity == null || !entity.username.equals(username)) {
            return null;
        }

        Location location = new Location(
                entity.country,
                entity.region,
                entity.city,
                entity.latitude,
                entity.longitude
        );

        return new Profile(entity.biography, location);
    }

    @Override
    public void updateProfile(String username, Profile profile) {
        if (!ProfileValidator.isValid(profile)) {
            throw new IllegalArgumentException("Invalid profile data.");
        }

        database.runInTransaction(() -> {
            if (!isActiveOwner()) {
                throw new IllegalStateException(
                        "Sign in again before changing profile data.");
            }

            UserProfileEntity entity = database.userProfileDao()
                    .getProfile(ownerId);

            if (entity == null || !entity.username.equals(username)) {
                throw new IllegalStateException(
                        "Own profile must be downloaded before editing.");
            }

            Location location = profile.getLocation();

            entity.biography = profile.getBiography();
            entity.country = location.getCountry();
            entity.region = location.getRegion();
            entity.city = location.getCity();
            entity.latitude = location.getLatitude();
            entity.longitude = location.getLongitude();

            entity.syncState = SyncState.PENDING.name();
            entity.updatedAt = System.currentTimeMillis();
            entity.localRevision = UUID.randomUUID().toString();

            database.userProfileDao().upsert(entity);
        });

        ((RecipeApplication) context).syncSignedInUser();
    }
}