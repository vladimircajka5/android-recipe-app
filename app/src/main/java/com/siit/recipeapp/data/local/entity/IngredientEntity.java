package com.siit.recipeapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.xml.transform.sax.SAXResult;

@Entity(tableName = "ingredients")
public class IngredientEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String categoryId;
    public String defaultUnitId;
    public String kind;

    public IngredientEntity() {}

}
