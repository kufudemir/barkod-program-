package com.marketpos.domain.usecase

import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class ApplyBulkStockChangeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(targets: List<Pair<String, Int>>): Result<Unit> = runCatching {
        targets.forEach { (barcode, newStock) ->
            require(newStock >= 0) { "Stok negatif olamaz" }
            productRepository.updateStock(barcode, newStock)
        }
    }
}
