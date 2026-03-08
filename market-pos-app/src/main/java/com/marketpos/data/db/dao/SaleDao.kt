package com.marketpos.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketpos.data.db.entity.SaleEntity
import com.marketpos.data.db.query.SummaryRow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :from AND :to AND status = 0 ORDER BY createdAt DESC")
    suspend fun listByDateRange(from: Long, to: Long): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE saleId = :saleId LIMIT 1")
    suspend fun getById(saleId: Long): SaleEntity?

    @Query(
        """
        SELECT SUM(totalAmountKurus) AS totalAmountKurus,
               SUM(profitKurus) AS totalProfitKurus,
               COUNT(saleId) AS saleCount
        FROM sales
        WHERE createdAt BETWEEN :from AND :to AND status = 0
        """
    )
    suspend fun sumTotalsByDateRange(from: Long, to: Long): SummaryRow?

    @Query("SELECT COUNT(saleId) FROM sales WHERE status = 0")
    suspend fun countCompletedSales(): Int
}
