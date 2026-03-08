package com.marketpos.domain.usecase

import com.marketpos.core.util.DateUtils
import com.marketpos.domain.model.BarcodeBankasiImportItem
import com.marketpos.domain.model.BarcodeBankasiImportSummary
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class ImportBarcodeBankasiProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(items: List<BarcodeBankasiImportItem>): Result<BarcodeBankasiImportSummary> = runCatching {
        require(items.isNotEmpty()) { "İçe aktarılacak ürün bulunamadı" }

        val existingProducts = productRepository.listByBarcodes(items.map { it.barcode })
            .associateBy { it.barcode }
        val now = DateUtils.now()

        val mergedProducts = items.map { item ->
            val existing = existingProducts[item.barcode]
            Product(
                barcode = item.barcode,
                name = item.name,
                salePriceKurus = item.salePriceKurus,
                costPriceKurus = existing?.costPriceKurus ?: 0L,
                stockQty = existing?.stockQty ?: 0,
                minStockQty = existing?.minStockQty ?: 0,
                note = existing?.note ?: "BarkodBankası import",
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                isActive = true
            )
        }

        productRepository.upsertAll(mergedProducts)

        val updatedCount = mergedProducts.count { existingProducts.containsKey(it.barcode) }
        BarcodeBankasiImportSummary(
            importedCount = mergedProducts.size,
            updatedCount = updatedCount,
            createdCount = mergedProducts.size - updatedCount
        )
    }
}


