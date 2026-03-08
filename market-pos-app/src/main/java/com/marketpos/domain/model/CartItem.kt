package com.marketpos.domain.model

data class CartItem(
    val barcode: String,
    val name: String,
    val baseSalePriceKurus: Long,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val quantity: Int,
    val stockQty: Int
) {
    val lineTotalKurus: Long get() = salePriceKurus * quantity
    val lineCostKurus: Long get() = costPriceKurus * quantity
    val hasCustomPrice: Boolean get() = salePriceKurus != baseSalePriceKurus
}
