package com.siit.recipeapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import java.util.List;

@Dao
public interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY expirationDate IS NULL, expirationDate")
    List<PantryEntity> getAll();

    @Query("SELECT * FROM pantry_items WHERE productId = :productId LIMIT 1")
    PantryEntity findByProductId(String productId);

    @Query("SELECT * FROM pantry_items WHERE syncState = 'PENDING'")
    List<PantryEntity> getPending();

    @Query("UPDATE pantry_items SET syncState = 'SYNCED' WHERE id = :id")
    void markSynced(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PantryEntity item);

    @Delete
    void delete(PantryEntity item);
}
