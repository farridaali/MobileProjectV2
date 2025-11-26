package com.example.mobileprojectv2

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.*

@Dao
interface GroceryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT * FROM  GROCERY_ITEMS ORDER BY id DESC")
    suspend fun getAllItems(): List<ItemEntity>

    @Query("UPDATE GROCERY_ITEMS SET isBought = :state WHERE id = :itemID")
    suspend fun setBoughtStatus(itemID: Int, state: Boolean)

    @Query("SELECT SUM(quantity*price) FROM grocery_items")
    suspend fun getTotalCost(): Double?

    @Query("SELECT * FROM grocery_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int):ItemEntity
}