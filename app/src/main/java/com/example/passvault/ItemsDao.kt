package com.example.passvault

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ItemsDao {
    @Query("SELECT * FROM item")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE id=(:id)")
    suspend fun getItem(id: UUID): Item

    @Update
    suspend fun updateItem(item: Item)

    @Insert
    suspend fun addItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)
}