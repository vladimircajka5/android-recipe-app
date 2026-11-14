package com.siit.recipeapp.data.local;

import com.siit.recipeapp.data.local.entity.IngredientEntity;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.ProductEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.IngredientKind;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;

public final class EntityMapper {
    private EntityMapper() {}

    public static PantryItem pantry(PantryEntity value) {
        return new PantryItem(value.id, value.ownerId, value.ingredientId, value.productId,
                value.brand, value.quantity, value.unitId, value.expirationDate);
    }

    public static PantryEntity pantry(PantryItem value, SyncState state) {
        return new PantryEntity(value.getId(), value.getOwnerId(), value.getIngredientId(),
                value.getProductId(), value.getBrand(), value.getQuantity(), value.getUnitId(),
                value.getExpirationDate(), state, System.currentTimeMillis());
    }

    public static ShoppingList list(ShoppingListEntity value) {
        return new ShoppingList(value.id, value.ownerId, value.name, value.createdAt,
                value.plannedPurchaseAt, value.permanent,
                ShoppingListStatus.valueOf(value.status));
    }

    public static ShoppingListEntity list(ShoppingList value, SyncState state) {
        return new ShoppingListEntity(value.getId(), value.getOwnerId(), value.getName(),
                value.getCreatedAt(), value.getPlannedPurchaseAt(), value.isPermanent(),
                value.getStatus(), state, System.currentTimeMillis());
    }

    public static ShoppingListItem item(ShoppingListItemEntity value) {
        return new ShoppingListItem(value.id, value.shoppingListId, value.ingredientId,
                value.quantity, value.unitId, value.purchased);
    }

    public static ShoppingListItemEntity item(ShoppingListItem value, SyncState state) {
        return new ShoppingListItemEntity(value.getId(), value.getShoppingListId(),
                value.getIngredientId(), value.getQuantity(), value.getUnitId(),
                value.isPurchased(), state, System.currentTimeMillis());
    }

    public static Ingredient ingredient(IngredientEntity value) {
        return new Ingredient(value.id, value.name, value.categoryId, value.defaultUnitId,
                IngredientKind.valueOf(value.kind));
    }

    public static IngredientEntity ingredient(Ingredient value) {
        IngredientEntity result = new IngredientEntity();
        result.id = value.getId();
        result.name = value.getName();
        result.categoryId = value.getCategoryId();
        result.defaultUnitId = value.getDefaultUnitId();
        result.kind = value.getKind().name();
        return result;
    }

    public static Product product(ProductEntity value) {
        return new Product(value.id, value.barcode, value.brandName, value.packageQuantity,
                value.unitId, value.displayColor, value.ingredientId);
    }

    public static ProductEntity product(Product value) {
        ProductEntity result = new ProductEntity();
        result.id = value.getId();
        result.barcode = value.getBarcode();
        result.brandName = value.getBrandName();
        result.packageQuantity = value.getPackageQuantity();
        result.unitId = value.getUnitId();
        result.displayColor = value.getDisplayColor();
        result.ingredientId = value.getIngredientId();
        return result;
    }
}