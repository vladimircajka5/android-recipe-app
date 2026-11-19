package com.siit.recipeapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.siit.recipeapp.data.local.entity.ShoppingListEntity;
import com.siit.recipeapp.data.local.entity.ShoppingListItemEntity;
import java.util.List;

@Dao
public interface ShoppingDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    List<ShoppingListEntity> getLists();

    @Query("SELECT * FROM shopping_list_items")
    List<ShoppingListItemEntity> getItems();

    @Query("SELECT * FROM shopping_lists WHERE id = :id LIMIT 1")
    ShoppingListEntity findList(String id);

    @Query("SELECT * FROM shopping_list_items WHERE id = :id LIMIT 1")
    ShoppingListItemEntity findItem(String id);

    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId")
    List<ShoppingListItemEntity> itemsFor(String listId);

    @Query("SELECT * FROM shopping_lists WHERE ownerId = :ownerId AND syncState = 'PENDING'")
    List<ShoppingListEntity> getPendingLists(String ownerId);

    @Query("SELECT item.* FROM shopping_list_items AS item " +
            "INNER JOIN shopping_lists AS list " +
            "ON item.shoppingListId = list.id " +
            "WHERE list.ownerId = :ownerId AND item.syncState = 'PENDING'")
    List<ShoppingListItemEntity> getPendingItems(String ownerId);

    @Query("UPDATE shopping_lists SET syncState = 'SYNCED' WHERE id = :id")
    void markListSynced(String id);

    @Query("UPDATE shopping_list_items SET syncState = 'SYNCED' WHERE id = :id")
    void markItemSynced(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertList(ShoppingListEntity list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertItem(ShoppingListItemEntity item);

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    void deleteList(String id);

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    void deleteItem(String id);

    @Query("DELETE FROM shopping_list_items WHERE shoppingListId = :listId")
    void deleteItemsForList(String listId);

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    void deleteListById(String id);

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    void deleteItemById(String id);
}
