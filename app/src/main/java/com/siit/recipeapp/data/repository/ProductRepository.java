package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.ingredient.Product;
import java.util.List;

public interface ProductRepository {
    List<Product> getProducts();
    Product findProductByBarcode(String barcode);
    void saveProduct(Product product);
}
