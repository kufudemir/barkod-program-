package com.marketpos.domain.usecase

import com.marketpos.domain.model.CartItem
import com.marketpos.domain.repository.SaleRepository
import javax.inject.Inject

class CreateSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(cartItems: List<CartItem>): Result<Long> {
        return saleRepository.createSale(cartItems)
    }
}
