package com.marketpos.domain.model

data class Sale(
    val saleId: Long,
    val createdAt: Long,
    val totalAmountKurus: Long,
    val totalCostKurus: Long,
    val profitKurus: Long,
    val itemCount: Int,
    val status: Int
)
