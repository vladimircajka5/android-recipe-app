package com.siit.recipeapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.siit.recipeapp.data.local.SyncState;

@Entity(tableName = "shopping_list_items")
public final class ShoppingListItemEntity {
    @PrimaryKey
    public String id;
    public String shoppingListId;
    public String ingredientId;
    public double quantity;
    public String unitId;
    public boolean purchased;
    public String syncState;
    public long updatedAt;

    public ShoppingListItemEntity() {}

    public ShoppingListItemEntity(String id, String shoppingListId, String ingredientId,
                                  double quantity, String unitId, boolean purchased,
                                  SyncState syncState, long updatedAt) {
        this.id = id;
        this.shoppingListId = shoppingListId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitId = unitId;
        this.purchased = purchased;
        this.syncState = syncState.name();
        this.updatedAt = updatedAt;
    }
}