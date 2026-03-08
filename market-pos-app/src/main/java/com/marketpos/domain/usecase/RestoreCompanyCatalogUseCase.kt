package com.marketpos.domain.usecase

import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class RestoreCompanyCatalogUseCase @Inject constructor(
    private val activationRepository: ActivationRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(companyCode: String, replaceExisting: Boolean): Result<Int> = runCatching {
        val cloudProducts = activationRepository.fetchCompanyCatalog(companyCode).getOrThrow()
        val mappedProducts = cloudProducts.map { remote ->
            Product(
                barcode = remote.barcode,
                name = remote.name,
                groupName = remote.groupName,
                salePriceKurus = remote.salePriceKurus,
                costPriceKurus = remote.costPriceKurus,
                stockQty = 0,
                minStockQty = 0,
                note = remote.note,
                createdAt = remote.updatedAt,
                updatedAt = remote.updatedAt,
                isActive = true
            )
        }
        productRepository.restoreCloudCatalog(mappedProducts, replaceExisting)
        mappedProducts.size
    }
}
