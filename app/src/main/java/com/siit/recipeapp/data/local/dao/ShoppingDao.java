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
}
