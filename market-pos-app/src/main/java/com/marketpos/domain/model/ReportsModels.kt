package com.marketpos.domain.model

data class HourlySalesReport(
    val hour: Int,
    val saleCount: Int,
    val totalAmountKurus: Long
)

data class DiscountedProductReport(
    val productBarcode: String,
    val productName: String,
    val totalQuantity: Int,
    val estimatedDiscountKurus: Long
)
