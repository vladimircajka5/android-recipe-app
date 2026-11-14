package com.siit.recipeapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.siit.recipeapp.data.local.SyncState;
import java.time.LocalDate;

@Entity(tableName = "pantry_items")
public final class PantryEntity {
    @PrimaryKey
    public String id;
    public String ownerId;
    public String ingredientId;
    public String productId;
    public String brand;
    public Double quantity;
    public String unitId;
    public LocalDate expirationDate;
    public String syncState;
    public long updatedAt;

    public PantryEntity() {}

    public PantryEntity(String id, String ownerId, String ingredientId, String productId,
                        String brand, Double quantity, String unitId, LocalDate expirationDate,
                        SyncState syncState, long updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.ingredientId = ingredientId;
        this.productId = productId;
        this.brand = brand;
        this.quantity = quantity;
        this.unitId = unitId;
        this.expirationDate = expirationDate;
        this.syncState = syncState.name();
        this.updatedAt = updatedAt;
    }
}