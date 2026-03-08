package com.marketpos.domain.model

data class HeldCart(
    val cartId: String,
    val label: String,
    val createdAt: Long,
    val items: List<CartItem>
) {
    val totalAmountKurus: Long get() = items.sumOf { it.lineTotalKurus }
    val totalItemCount: Int get() = items.sumOf { it.quantity }
}
