package com.marketpos.domain.usecase

import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.CatalogSyncRepository
import javax.inject.Inject

class QueueProductSyncUseCase @Inject constructor(
    private val catalogSyncRepository: CatalogSyncRepository
) {
    suspend fun upsert(product: Product): Result<Unit> = catalogSyncRepository.queueProductUpsert(product)
    suspend fun deactivate(barcode: String, product: Product?): Result<Unit> = catalogSyncRepository.queueProductDeactivate(barcode, product)
}
