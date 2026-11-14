package com.siit.recipeapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.siit.recipeapp.data.local.dao.CatalogDao;
import com.siit.recipeapp.data.local.dao.PantryDao;
import com.siit.recipeapp.data.local.dao.ShoppingDao;
import com.siit.recipeapp.data.local.entity.IngredientEntity;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.ProductEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;

@Database(entities = { PantryEntity.class, ShoppingListEntity.class, ShoppingListItemEntity.class,
        IngredientEntity.class, ProductEntity.class }, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract PantryDao pantryDao();
    public abstract ShoppingDao shoppingDao();
    public abstract CatalogDao catalogDao();

    private static volatile RecipeDatabase instance;

    public static RecipeDatabase getInstance(Context context) {
        RecipeDatabase result = instance;
        if (result == null) {
            synchronized (RecipeDatabase.class) {
                result = instance;
                if (result == null) {
                    instance = result = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RecipeDatabase.class,
                                    "recipeapp.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return result;
    }
}