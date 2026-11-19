package com.siit.recipeapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "user_profiles")
public final class UserProfileEntity {

    @PrimaryKey
    @NonNull
    public String ownerId;

    @NonNull
    public String username = "";

    public String biography;
    public String country;
    public String region;
    public String city;
    public Double latitude;
    public Double longitude;

    // Putanja slike dostupne na ovom uredjaju
    public String localImageUri;

    @NonNull
    public String syncState = "SYNCED";

    public long updatedAt;

    @NonNull
    @ColumnInfo(defaultValue = "''")
    public String localRevision = UUID.randomUUID().toString();

    public UserProfileEntity() {
    }
}