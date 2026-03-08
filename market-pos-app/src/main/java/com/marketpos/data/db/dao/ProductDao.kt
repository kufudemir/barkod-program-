package com.marketpos.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.marketpos.data.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getAnyByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    fun observeByBarcode(barcode: String): Flow<ProductEntity?>

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("UPDATE products SET isActive = 0, updatedAt = :updatedAt WHERE barcode = :barcode")
    suspend fun deactivate(barcode: String, updatedAt: Long)

    @Query("UPDATE products SET isActive = 0, updatedAt = :updatedAt WHERE isActive = 1")
    suspend fun deactivateAll(updatedAt: Long)

    @Query("DELETE FROM products WHERE barcode NOT IN (SELECT DISTINCT productBarcode FROM sale_items)")
    suspend fun deleteWithoutSaleReferences()

    @Query("UPDATE products SET stockQty = :newQty, updatedAt = :updatedAt WHERE barcode = :barcode")
    suspend fun updateStock(barcode: String, newQty: Int, updatedAt: Long)

    @Query("UPDATE products SET salePriceKurus = :newPriceKurus, updatedAt = :updatedAt WHERE barcode = :barcode")
    suspend fun updateSalePrice(barcode: String, newPriceKurus: Long, updatedAt: Long)

    @Query(
        """
        SELECT * FROM products
        WHERE isActive = 1
          AND (:query = '' OR name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
        """
    )
    fun searchByName(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun listAllActive(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY updatedAt DESC")
    suspend fun listAllActiveOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE barcode IN (:barcodes) AND isActive = 1")
    suspend fun listByBarcodes(barcodes: List<String>): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countAllProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun countActiveProducts(): Int
}
