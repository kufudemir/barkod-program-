package com.marketpos.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["saleId"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["barcode"],
            childColumns = ["productBarcode"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("saleId"), Index("productBarcode")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val saleItemId: Long = 0L,
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
