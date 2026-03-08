package com.marketpos.domain.usecase

import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Product? {
        return productRepository.getByBarcode(barcode)
    }
}
