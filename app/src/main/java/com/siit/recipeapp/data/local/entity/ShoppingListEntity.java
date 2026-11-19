package com.siit.recipeapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(tableName = "shopping_lists")
public final class ShoppingListEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String ownerId;
    public String name;
    public LocalDateTime createdAt;
    public LocalDateTime plannedPurchaseAt;
    public boolean permanent;
    public String status;
    public String syncState;
    public long updatedAt;
    @NonNull
    @ColumnInfo(defaultValue = "''")
    public String localRevision = UUID.randomUUID().toString();

    public ShoppingListEntity() {}

    public ShoppingListEntity(String id, String ownerId, String name, LocalDateTime createdAt,
                              LocalDateTime plannedPurchaseAt, boolean permanent,
                              ShoppingListStatus status, SyncState syncState, long updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = createdAt;
        this.plannedPurchaseAt = plannedPurchaseAt;
        this.permanent = permanent;
        this.status = status.name();
        this.syncState = syncState.name();
        this.updatedAt = updatedAt;
    }
}