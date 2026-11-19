package com.siit.recipeapp.ui.pantry;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.domain.service.IngredientService;
import com.siit.recipeapp.domain.service.PantryService;
import com.siit.recipeapp.domain.service.ProductService;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PantryViewModel extends ViewModel {
    private final PantryService pantryService;
    private final IngredientService ingredientService;
    private final ProductService productService;
    private final LiveData<List<PantryItem>> observableItems;

    public PantryViewModel(PantryService pantryService, IngredientService ingredientService, ProductService productService) {
        this.pantryService = pantryService;
        this.ingredientService = ingredientService;
        this.productService = productService;
        this.observableItems = pantryService.observeItems();
    }
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<SaveResult> saveResult = new MutableLiveData<>();

    enum SaveStatus {
        SAVING,
        SAVED,
        FAILED
    }

    static final class SaveResult {
        final String draftId;
        final SaveStatus status;

        SaveResult(String draftId, SaveStatus status) {
            this.draftId = draftId;
            this.status = status;
        }
    }
    LiveData<SaveResult> observeSaveResult() {
        return saveResult;
    }

    void saveManualItem(String draftId, PantryItem item) {
        SaveResult current = saveResult.getValue();
        if (current != null && current.status == SaveStatus.SAVING) {
            return;
        }

        saveResult.setValue(new SaveResult(draftId, SaveStatus.SAVING));

        saveExecutor.execute(() -> {
            SaveStatus status;
            try {
                pantryService.saveItem(item);
                status = SaveStatus.SAVED;
            } catch (RuntimeException exception) {
                Log.e("PantryViewModel", "Could not save pantry item", exception);
                status = SaveStatus.FAILED;
            }

            SaveResult result = new SaveResult(draftId, status);
            mainHandler.post(() -> saveResult.setValue(result));
        });
    }

    void clearSaveResult(String draftId) {
        SaveResult current = saveResult.getValue();
        if (current != null
                && draftId.equals(current.draftId)
                && current.status != SaveStatus.SAVING) {
            saveResult.setValue(null);
        }
    }

    @Override
    protected void onCleared() {
        saveExecutor.shutdown();
        super.onCleared();
    }

    public List<PantryItem> getItems() { return pantryService.getItems(); }

    public LiveData<List<PantryItem>> observeItems() {
        return observableItems;
    }

    public List<Ingredient> getIngredients() { return ingredientService.getIngredients(); }

    public List<Product> getProducts() { return productService.getProducts(); }

    public PantryItem findItemByProductId(String id) { return pantryService.findItemByProductId(id); }

    public Product findProductByBarcode(String barcode) { return productService.findProductByBarcode(barcode); }

    public void saveItem(PantryItem item) { pantryService.saveItem(item); }

    public void saveIngredient(Ingredient ingredient) { ingredientService.saveIngredient(ingredient); }

    public void saveProduct(Product product) { productService.saveProduct(product); }

    public void removeItem(String itemId) { pantryService.removeItem(itemId); }
}
