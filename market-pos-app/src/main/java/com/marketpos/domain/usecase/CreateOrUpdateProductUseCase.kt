package com.marketpos.domain.usecase

import com.marketpos.core.util.DateUtils
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class CreateOrUpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product, originalBarcode: String? = null): Result<Unit> = runCatching {
        val now = DateUtils.now()
        val existing = when {
            !originalBarcode.isNullOrBlank() -> productRepository.getByBarcode(originalBarcode)
            else -> productRepository.getByBarcode(product.barcode)
        }
        val conflictingProduct = productRepository.getByBarcode(product.barcode)
        val isBarcodeChanged = !originalBarcode.isNullOrBlank() && originalBarcode != product.barcode

        if (originalBarcode.isNullOrBlank() && conflictingProduct != null) {
            throw IllegalArgumentException("Bu barkod zaten kayitli")
        }
        if (isBarcodeChanged && conflictingProduct != null) {
            throw IllegalArgumentException("Yeni barkod baska bir urunde kullaniliyor")
        }

        val normalized = product.copy(
            salePriceKurus = MoneyUtils.roundUpToWholeTL(product.salePriceKurus),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            isActive = true
        )

        if (isBarcodeChanged) {
            productRepository.upsertReplacingBarcode(originalBarcode!!, normalized)
        } else {
            productRepository.upsert(normalized)
        }
    }
}
