package com.marketpos.domain.model

data class SaleItem(
    val saleItemId: Long,
    val saleId: Long,
    val productBarcode: String,
    val productNameSnapshot: String,
    val unitBaseSalePriceKurusSnapshot: Long,
    val unitSalePriceKurusSnapshot: Long,
    val unitCostPriceKurusSnapshot: Long,
    val quantity: Int,
    val lineTotalKurus: Long,
    val lineCostKurus: Long
)
