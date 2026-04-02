package com.example.stockentry.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StockEntryDao {
    @Insert
    suspend fun insert(entry: StockEntry)

    @Delete
    suspend fun delete(entry: StockEntry)

    @Query("SELECT * FROM stock_entries ORDER BY createdAt DESC LIMIT 10")
    suspend fun getLast10Entries(): List<StockEntry>

    @Query("SELECT * FROM stock_entries WHERE synced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedEntries(): List<StockEntry>

    @Query("UPDATE stock_entries SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)
}
