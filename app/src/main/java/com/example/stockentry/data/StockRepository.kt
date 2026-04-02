package com.example.stockentry.data

import com.example.stockentry.network.SheetsService

class StockRepository(
    private val dao: StockEntryDao,
    private val sheetsService: SheetsService
) {
    suspend fun addEntry(productType: String, rate: Double, quantity: Int) {
        dao.insert(
            StockEntry(
                productType = productType,
                rate = rate,
                quantity = quantity
            )
        )
    }

    suspend fun deleteEntry(entry: StockEntry) = dao.delete(entry)

    suspend fun last10Entries(): List<StockEntry> = dao.getLast10Entries()

    suspend fun syncUnsyncedEntries(): Result<Int> {
        val unsynced = dao.getUnsyncedEntries()
        if (unsynced.isEmpty()) return Result.success(0)

        return if (sheetsService.upload(unsynced)) {
            dao.markSynced(unsynced.map { it.id })
            Result.success(unsynced.size)
        } else {
            Result.failure(IllegalStateException("Upload failed"))
        }
    }
}
