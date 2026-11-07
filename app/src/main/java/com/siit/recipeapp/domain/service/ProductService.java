package com.siit.recipeapp.domain.service;

import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.repository.ProductRepository;
import java.util.List;

//Product catalog use cases
public final class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository = repository; }
    public List<Product> getProducts() { return repository.getProducts(); }
    public Product findProductByBarcode(String barcode) { return repository.findProductByBarcode(barcode); }
    public void saveProduct(Product product) { repository.saveProduct(product); }
}
