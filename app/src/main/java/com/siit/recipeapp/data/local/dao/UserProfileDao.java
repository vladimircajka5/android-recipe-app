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
}