package com.siit.recipeapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.siit.recipeapp.data.local.entity.SyncTombstoneEntity;

import java.util.List;

@Dao
public interface SyncDao {
    @Query("SELECT * FROM sync_tombstones WHERE ownerId = :ownerId ORDER BY updatedAt")
    List<SyncTombstoneEntity> getTombstones(String ownerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertTombstone(SyncTombstoneEntity tombstone);

    @Query("DELETE FROM sync_tombstones WHERE `key` = :key")
    void deleteTombstone(String key);

    @Query("DELETE FROM sync_tombstones " +
            "WHERE `key` = :key AND ownerId = :ownerId " +
            "AND localRevision = :revision")
    int deleteSyncedTombstone(String key, String ownerId, String revision);
}