package com.siit.recipeapp.data.repository.room;

import android.content.Context;

import com.siit.recipeapp.data.local.EntityMapper;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.SyncState;
import com.siit.recipeapp.data.local.entity.PantryEntity;
import com.siit.recipeapp.data.local.entity.SyncTombstoneEntity;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.PantryRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RoomPantryRepository implements PantryRepository {

    private final RecipeDatabase database;
    private final Context context;
    private final String ownerId;

    public RoomPantryRepository(RecipeDatabase database, Context context){
        this.database = database;
        this.context = context;
        this.ownerId = UserDataSession.currentOwnerId(this.context);
    }

    private boolean isActiveOwner() {
        return ownerId != null
                && ownerId.equals(UserDataSession.currentOwnerId(context));
    }

    private void requireActiveOwner() {
        if (!isActiveOwner()) {
            throw new IllegalStateException(
                    "Sign in again before changing pantry data.");
        }
    }

    @Override
    public List<PantryItem> getItems() {
        if (!isActiveOwner()) {
            return Collections.emptyList();
        }
        return mapItems(database.pantryDao().getForOwner(ownerId));
    }

    @Override
    public LiveData<List<PantryItem>> observeItems() {
        return Transformations.map(
                database.pantryDao().observeForOwner(ownerId),
                entities -> {
                    if (!isActiveOwner()) {
                        return Collections.<PantryItem>emptyList();
                    }

                    return mapItems(entities);
                }
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
        if (!isActiveOwner()) {
            return null;
        }
        PantryEntity entity = database.pantryDao()
                .findByProductIdForOwner(productId, ownerId);

        return entity == null ? null : EntityMapper.pantry(entity);
    }

    @Override
    public void saveItem(PantryItem item) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            PantryEntity existing = database.pantryDao()
                    .findById(item.getId());

            if (existing != null && !ownerId.equals(existing.ownerId)) {
                throw new IllegalStateException(
                        "Cannot replace another owner's pantry item.");
            }

            PantryEntity entity = EntityMapper.pantry(
                    item, SyncState.PENDING);

            entity.ownerId = ownerId;

            database.syncDao().deleteTombstone(
                    "pantry/" + entity.id);

            database.pantryDao().upsert(entity);
        });
    }

    @Override
    public void removeItem(String itemId) {
        database.runInTransaction(() -> {
            requireActiveOwner();

            PantryEntity entity = database.pantryDao()
                    .findByIdForOwner(itemId, ownerId);

            if (entity == null) {
                return;
            }

            database.syncDao().upsertTombstone(
                    new SyncTombstoneEntity(
                            ownerId,
                            "pantry",
                            entity.id,
                            System.currentTimeMillis()
                    )
            );

            database.pantryDao().delete(entity);
        });
    }
}