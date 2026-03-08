package com.marketpos.domain.model

data class SummaryReport(
    val totalAmountKurus: Long,
    val totalProfitKurus: Long,
    val saleCount: Int
)

data class ProductAggregateReport(
    val productBarcode: String,
    val productName: String,
    val totalQuantity: Int,
    val totalProfitKurus: Long
)
