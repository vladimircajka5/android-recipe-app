package com.siit.recipeapp.data.repository.room;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.repository.PantryRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

public final class RoomPantryRepository implements PantryRepository {

    private final RecipeDatabase database;

    public RoomPantryRepository(RecipeDatabase database){
        this.database = database;
    }

    @Override
    public List<PantryItem> getItems() {
        return mapItems(database.pantryDao().getAll());
    }

    @Override
    public LiveData<List<PantryItem>> observeItems() {
        return Transformations.map(
                database.pantryDao().observeAll(),
                RoomPantryRepository::mapItems
        );
    }

    private static List<PantryItem> mapItems(List<PantryEntity> entities) {
        List<PantryItem> result = new ArrayList<>(entities.size());
        for (PantryEntity entity : entities) {
            result.add(EntityMapper.pantry(entity));
        }
        return result;
    }

    @Override
    public PantryItem findItemByProductId(String productId) {
        PantryEntity item = database.pantryDao().findByProductId(productId);
        return item == null ? null : EntityMapper.pantry(item);
    }

    @Override
    public void saveItem(PantryItem item) {
        database.pantryDao().upsert(EntityMapper.pantry(item, SyncState.PENDING));
    }

    @Override
    public void removeItem(String itemId) {
        PantryEntity item = database.pantryDao().getAll().stream()
                .filter(value -> value.id.equals(itemId))
                .findFirst().orElse(null);

        if (item != null) {
            database.pantryDao().delete(item);
        }
    }
}
