package com.marketpos.domain.model

data class Product(
    val barcode: String,
    val name: String,
    val groupName: String? = null,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val stockQty: Int,
    val minStockQty: Int,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true
)
