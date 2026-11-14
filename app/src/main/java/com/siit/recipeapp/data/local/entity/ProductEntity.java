package com.siit.recipeapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public final class ProductEntity {
    @PrimaryKey
    public String id;
    public String barcode;
    public String brandName;
    public Double packageQuantity;
    public String unitId;
    public String displayColor;
    public String ingredientId;

    public ProductEntity() {}
}