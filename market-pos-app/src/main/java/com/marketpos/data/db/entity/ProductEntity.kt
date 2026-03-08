package com.marketpos.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index("name"), Index("updatedAt")]
)
data class ProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val groupName: String?,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val stockQty: Int,
    val minStockQty: Int,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)
