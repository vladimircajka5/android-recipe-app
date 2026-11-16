package com.siit.recipeapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.siit.recipeapp.data.local.dao.CatalogDao;
import com.siit.recipeapp.data.local.dao.PantryDao;
import com.siit.recipeapp.data.local.dao.ShoppingDao;
import com.siit.recipeapp.data.local.dao.SyncDao;
import com.siit.recipeapp.data.local.entity.IngredientEntity;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.ProductEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import com.siit.recipeapp.data.local.entity.SyncTombstoneEntity;

@Database(entities = { PantryEntity.class, ShoppingListEntity.class, ShoppingListItemEntity.class,
        IngredientEntity.class, ProductEntity.class, SyncTombstoneEntity.class },
        version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract PantryDao pantryDao();
    public abstract ShoppingDao shoppingDao();
    public abstract CatalogDao catalogDao();
    public abstract SyncDao syncDao();
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

    private static final androidx.room.migration.Migration MIGRATION_1_2 =
            new androidx.room.migration.Migration(1, 2) {
                @Override
                public void migrate(@androidx.annotation.NonNull
                                    androidx.sqlite.db.SupportSQLiteDatabase database) {
                    database.execSQL("CREATE TABLE IF NOT EXISTS sync_tombstones (" +
                            "`key` TEXT NOT NULL, ownerId TEXT, collection TEXT NOT NULL, " +
                            "documentId TEXT NOT NULL, updatedAt INTEGER NOT NULL, " +
                            "PRIMARY KEY(`key`))");
                }
            };
}