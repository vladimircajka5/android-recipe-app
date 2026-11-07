package com.siit.recipeapp.ui.pantry;

import androidx.lifecycle.ViewModel;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.domain.service.IngredientService;
import com.siit.recipeapp.domain.service.PantryService;
import com.siit.recipeapp.domain.service.ProductService;
import java.util.List;

public final class PantryViewModel extends ViewModel {
    private final PantryService pantryService;
    private final IngredientService ingredientService;
    private final ProductService productService;

    public PantryViewModel(PantryService pantryService, IngredientService ingredientService, ProductService productService) {
        this.pantryService = pantryService;
        this.ingredientService = ingredientService;
        this.productService = productService;
    }
    public List<PantryItem> getItems() { return pantryService.getItems(); }
    public List<Ingredient> getIngredients() { return ingredientService.getIngredients(); }
    public List<Product> getProducts() { return productService.getProducts(); }
    public PantryItem findItemByProductId(String id) { return pantryService.findItemByProductId(id); }
    public Product findProductByBarcode(String barcode) { return productService.findProductByBarcode(barcode); }
    public void saveItem(PantryItem item) { pantryService.saveItem(item); }
    public void saveIngredient(Ingredient ingredient) { ingredientService.saveIngredient(ingredient); }
    public void saveProduct(Product product) { productService.saveProduct(product); }
    public void removeItem(String itemId) { pantryService.removeItem(itemId); }
}
