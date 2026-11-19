package com.siit.recipeapp.data.repository.room;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.entity.ProductEntity;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public final class RoomProductRepository implements ProductRepository {
    private final RecipeDatabase database;

    public RoomProductRepository(RecipeDatabase database) {
        this.database = database;
    }

    @Override
    public List<Product> getProducts() {
        List<Product> result = new ArrayList<>();
        for (ProductEntity product : database.catalogDao().getProducts()) {
            result.add(EntityMapper.product(product));
        }
        return result;
    }

    @Override
    public Product findProductByBarcode(String barcode) {
        ProductEntity product = database.catalogDao().findProductByBarcode(barcode);
        return product == null ? null : EntityMapper.product(product);
    }

    @Override
    public void saveProduct(Product product) {
        database.catalogDao().upsertProduct(EntityMapper.product(product));
    }
}