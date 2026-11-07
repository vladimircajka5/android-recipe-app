package com.siit.recipeapp.data.repository.inmemory;

import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;

// In-memory barcode product catalog, will be replaced with persistant version later
public final class InMemoryProductRepository implements ProductRepository {
    private final List<Product> products = new ArrayList<>();

    @Override public List<Product> getProducts() { return List.copyOf(products); }
    @Override public Product findProductByBarcode(String barcode) {
        if (barcode == null) return null;
        for (Product product : products) if (product.getBarcode().equals(barcode)) return product;
        return null;
    }
    @Override public void saveProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            Product current = products.get(i);
            if (current.getId().equals(product.getId()) || current.getBarcode().equals(product.getBarcode())) {
                products.set(i, product); return;
            }
        }
        products.add(product);
    }
}
