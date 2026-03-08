package com.marketpos.domain.usecase

import com.marketpos.core.cart.CartManager
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject

class ResetProductCatalogUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartManager: CartManager
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        productRepository.resetCatalog()
        cartManager.clear()
    }
}
