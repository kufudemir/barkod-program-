package com.marketpos.domain.repository

import com.marketpos.domain.model.Product
import com.marketpos.domain.model.SyncFlushResult

interface CatalogSyncRepository {
    suspend fun queueProductUpsert(product: Product): Result<Unit>
    suspend fun queueProductDeactivate(barcode: String, lastKnownProduct: Product?): Result<Unit>
    suspend fun flushPending(): Result<SyncFlushResult>
}
