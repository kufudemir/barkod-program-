package com.marketpos.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketpos.data.db.entity.SaleItemEntity
import com.marketpos.data.db.query.ProductAggregateRow

@Dao
interface SaleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SaleItemEntity>)

    @Query(
        """
        SELECT si.productBarcode AS productBarcode,
               MAX(si.productNameSnapshot) AS productName,
               SUM(si.quantity) AS totalQuantity,
               SUM(si.lineTotalKurus - si.lineCostKurus) AS totalProfitKurus
        FROM sale_items si
        INNER JOIN sales s ON s.saleId = si.saleId
        WHERE s.createdAt BETWEEN :from AND :to AND s.status = 0
        GROUP BY si.productBarcode
        ORDER BY totalQuantity DESC
        LIMIT :limit
        """
    )
    suspend fun topSellingByDateRange(from: Long, to: Long, limit: Int): List<ProductAggregateRow>

    @Query(
        """
        SELECT si.productBarcode AS productBarcode,
               MAX(si.productNameSnapshot) AS productName,
               SUM(si.quantity) AS totalQuantity,
               SUM(si.lineTotalKurus - si.lineCostKurus) AS totalProfitKurus
        FROM sale_items si
        INNER JOIN sales s ON s.saleId = si.saleId
        WHERE s.createdAt BETWEEN :from AND :to AND s.status = 0
        GROUP BY si.productBarcode
        ORDER BY totalProfitKurus DESC
        LIMIT :limit
        """
    )
    suspend fun topProfitByDateRange(from: Long, to: Long, limit: Int): List<ProductAggregateRow>

    @Query(
        """
        SELECT COALESCE(SUM(si.quantity), 0)
        FROM sale_items si
        INNER JOIN sales s ON s.saleId = si.saleId
        WHERE si.productBarcode = :productBarcode
          AND s.createdAt BETWEEN :from AND :to
          AND s.status = 0
        """
    )
    suspend fun soldQuantityByProductAndDateRange(productBarcode: String, from: Long, to: Long): Int

    @Query(
        """
        SELECT si.*
        FROM sale_items si
        INNER JOIN sales s ON s.saleId = si.saleId
        WHERE s.createdAt BETWEEN :from AND :to
          AND s.status = 0
        ORDER BY s.createdAt DESC, si.saleItemId DESC
        """
    )
    suspend fun listByDateRange(from: Long, to: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY saleItemId ASC")
    suspend fun listBySaleId(saleId: Long): List<SaleItemEntity>
}
