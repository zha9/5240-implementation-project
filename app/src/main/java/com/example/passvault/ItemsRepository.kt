package com.example.passvault

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory
import java.util.UUID

class ItemsRepository private constructor(
    private var context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {
    private lateinit var database: ItemsDatabase

    fun initializeDatabase(databaseName: String, passPhrase: String) {
        database = Room
            .databaseBuilder(
                context.applicationContext,
                ItemsDatabase::class.java,
                databaseName
            )
            .openHelperFactory(SupportFactory(SQLCipherUtils.getBytes(passPhrase)))
            .build()
    }

    fun getItems(): Flow<List<Item>> = database.itemsDao().getItems()

    suspend fun getItem(id: UUID): Item = database.itemsDao().getItem(id)

    fun updateItem(item: Item) {
        coroutineScope.launch {
            database.itemsDao().updateItem(item)
        }
    }

    suspend fun addItem(item: Item) {
        database.itemsDao().addItem(item)
    }

    fun deleteItem(item: Item) {
        coroutineScope.launch {
            database.itemsDao().deleteItem(item)
        }
    }

    companion object {
        private var INSTANCE: ItemsRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ItemsRepository(context)
            }
        }

        fun get(): ItemsRepository {
            return INSTANCE
                ?: throw IllegalStateException("Repository not initialized!")
        }
    }
}