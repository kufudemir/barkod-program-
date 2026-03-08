package com.marketpos.data.db.query

data class ProductAggregateRow(
    val productBarcode: String,
    val productName: String,
    val totalQuantity: Int,
    val totalProfitKurus: Long
)
