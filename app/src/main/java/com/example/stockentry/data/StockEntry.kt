package com.example.stockentry.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_entries")
data class StockEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productType: String,
    val rate: Double,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
