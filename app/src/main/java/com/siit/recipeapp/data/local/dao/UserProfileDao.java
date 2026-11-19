package com.siit.recipeapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.siit.recipeapp.data.local.entity.UserProfileEntity;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE ownerId = :ownerId LIMIT 1")
    UserProfileEntity getProfile(String ownerId);

    @Query("SELECT * FROM user_profiles WHERE ownerId = :ownerId LIMIT 1")
    LiveData<UserProfileEntity> observeProfile(String ownerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfileEntity profile);

    @Query("SELECT * FROM user_profiles " +
            "WHERE ownerId = :ownerId AND syncState = 'PENDING' LIMIT 1")
    UserProfileEntity getPending(String ownerId);

    @Query("UPDATE user_profiles SET syncState = 'SYNCED' " +
            "WHERE ownerId = :ownerId AND localRevision = :revision " +
            "AND syncState = 'PENDING'")
    int markSynced(String ownerId, String revision);
}