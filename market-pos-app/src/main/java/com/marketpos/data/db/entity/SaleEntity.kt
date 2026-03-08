package com.marketpos.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [Index("createdAt")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val saleId: Long = 0L,
    val createdAt: Long,
    val totalAmountKurus: Long,
    val totalCostKurus: Long,
    val profitKurus: Long,
    val itemCount: Int,
    val status: Int
)

object SaleStatus {
    const val COMPLETED = 0
    const val VOID = 1
}
