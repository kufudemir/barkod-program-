package com.marketpos.domain.repository

import com.marketpos.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAllActive(): Flow<List<Product>>
    fun observeSearch(query: String): Flow<List<Product>>
    fun observeByBarcode(barcode: String): Flow<Product?>
    suspend fun getByBarcode(barcode: String): Product?
    suspend fun upsert(product: Product)
    suspend fun upsertAll(products: List<Product>)
    suspend fun upsertReplacingBarcode(previousBarcode: String, product: Product)
    suspend fun deactivate(barcode: String)
    suspend fun deactivateAll()
    suspend fun resetCatalog()
    suspend fun updateStock(barcode: String, newQty: Int)
    suspend fun updateSalePrice(barcode: String, newSalePriceKurus: Long)
    suspend fun listByBarcodes(barcodes: List<String>): List<Product>
    suspend fun listAllActiveOnce(): List<Product>
    suspend fun countAllProducts(): Int
    suspend fun countActiveProducts(): Int
    suspend fun restoreCloudCatalog(products: List<Product>, replaceExisting: Boolean)
}
