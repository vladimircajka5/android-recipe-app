package com.siit.recipeapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.lifecycle.LiveData;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import java.util.List;

@Dao
public interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY expirationDate IS NULL, expirationDate")
    List<PantryEntity> getAll();

    @Query("SELECT * FROM pantry_items ORDER BY expirationDate IS NULL, expirationDate")
    LiveData<List<PantryEntity>> observeAll();

    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    PantryEntity findById(String id);

    @Query("SELECT * FROM pantry_items WHERE productId = :productId LIMIT 1")
    PantryEntity findByProductId(String productId);

    @Query("SELECT * FROM pantry_items WHERE ownerId = :ownerId AND syncState = 'PENDING'")
    List<PantryEntity> getPending(String ownerId);

    @Query("UPDATE pantry_items SET syncState = 'SYNCED' " +
            "WHERE id = :id AND ownerId = :ownerId " +
            "AND localRevision = :revision AND syncState = 'PENDING'")
    int markSynced(String id, String ownerId, String revision);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PantryEntity item);

    @Query("DELETE FROM pantry_items WHERE id = :id")
    void deleteById(String id);

    @Delete
    void delete(PantryEntity item);

    @Query("SELECT * FROM pantry_items WHERE ownerId = :ownerId " +
            "ORDER BY expirationDate IS NULL, expirationDate")
    List<PantryEntity> getForOwner(String ownerId);

    @Query("SELECT * FROM pantry_items WHERE ownerId = :ownerId " +
            "ORDER BY expirationDate IS NULL, expirationDate")
    LiveData<List<PantryEntity>> observeForOwner(String ownerId);

    @Query("SELECT * FROM pantry_items " +
            "WHERE id = :id AND ownerId = :ownerId LIMIT 1")
    PantryEntity findByIdForOwner(String id, String ownerId);

    @Query("SELECT * FROM pantry_items " +
            "WHERE productId = :productId AND ownerId = :ownerId LIMIT 1")
    PantryEntity findByProductIdForOwner(String productId, String ownerId);
}
