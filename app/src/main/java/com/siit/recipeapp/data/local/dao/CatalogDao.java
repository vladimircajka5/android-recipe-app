package com.siit.recipeapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.siit.recipeapp.data.local.entity.IngredientEntity;
import com.siit.recipeapp.data.local.entity.ProductEntity;
import java.util.List;

@Dao
public interface CatalogDao {
    @Query("SELECT * FROM ingredients")
    List<IngredientEntity> getIngredients();

    @Query("SELECT * FROM ingredients WHERE id = :id LIMIT 1")
    IngredientEntity findIngredient(String id);

    @Query("SELECT * FROM products")
    List<ProductEntity> getProducts();

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    ProductEntity findProductByBarcode(String barcode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertIngredient(IngredientEntity ingredient);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertProduct(ProductEntity product);
}