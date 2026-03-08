package com.marketpos.domain.repository

import com.marketpos.domain.model.StockCountScanResult
import com.marketpos.domain.model.StockCountSession
import kotlinx.coroutines.flow.Flow

interface StockCountRepository {
    fun observeSession(): Flow<StockCountSession>
    suspend fun addScan(barcode: String): Result<StockCountScanResult>
    suspend fun updateCount(barcode: String, countedQty: Int): Result<Unit>
    suspend fun removeItem(barcode: String): Result<Unit>
    suspend fun clearSession(): Result<Unit>
    suspend fun applyCountResult(): Result<Int>
}
