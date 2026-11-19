package com.siit.recipeapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "sync_tombstones")
public final class SyncTombstoneEntity {
    @PrimaryKey
    @NonNull
    public String key;
    public String ownerId;
    public String collection;
    public String documentId;
    public long updatedAt;
    @NonNull
    @ColumnInfo(defaultValue = "''")
    public String localRevision = UUID.randomUUID().toString();

    public SyncTombstoneEntity() {}

    public SyncTombstoneEntity(String ownerId, String collection, String documentId, long updatedAt) {
        this.key = collection + "/" + documentId;
        this.ownerId = ownerId;
        this.collection = collection;
        this.documentId = documentId;
        this.updatedAt = updatedAt;
    }
}