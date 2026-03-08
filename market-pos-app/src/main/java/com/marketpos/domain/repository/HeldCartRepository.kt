package com.marketpos.domain.repository

import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.HeldCart
import kotlinx.coroutines.flow.Flow

interface HeldCartRepository {
    fun observeHeldCarts(): Flow<List<HeldCart>>
    suspend fun getHeldCart(cartId: String): HeldCart?
    suspend fun saveHeldCart(label: String?, items: List<CartItem>): Result<HeldCart>
    suspend fun deleteHeldCart(cartId: String): Result<Unit>
}
