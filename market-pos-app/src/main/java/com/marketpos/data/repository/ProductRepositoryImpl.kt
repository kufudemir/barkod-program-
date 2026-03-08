package com.marketpos.data.repository

import androidx.room.withTransaction
import com.marketpos.data.db.AppDatabase
import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.dao.ProductDao
import com.marketpos.data.db.mapper.toDomain
import com.marketpos.data.db.mapper.toEntity
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.CatalogSyncRepository
import com.marketpos.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val productDao: ProductDao,
    private val catalogSyncRepository: CatalogSyncRepository
) : ProductRepository {

    override fun observeAllActive(): Flow<List<Product>> {
        return productDao.listAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun observeSearch(query: String): Flow<List<Product>> {
        return productDao.searchByName(query.trim()).map { list -> list.map { it.toDomain() } }
    }

    override fun observeByBarcode(barcode: String): Flow<Product?> {
        return productDao.observeByBarcode(barcode).map { it?.toDomain() }
    }

    override suspend fun getByBarcode(barcode: String): Product? = productDao.getByBarcode(barcode)?.toDomain()

    override suspend fun upsert(product: Product) {
        productDao.upsert(product.toEntity())
        catalogSyncRepository.queueProductUpsert(product)
        flushSyncBestEffort()
    }

    override suspend fun upsertAll(products: List<Product>) {
        if (products.isEmpty()) return
        database.withTransaction {
            productDao.upsertAll(products.map { it.toEntity() })
        }
        products.forEach { product ->
            catalogSyncRepository.queueProductUpsert(product)
        }
        flushSyncBestEffort()
    }

    override suspend fun upsertReplacingBarcode(previousBarcode: String, product: Product) {
        val previousProduct = productDao.getByBarcode(previousBarcode)?.toDomain()
        database.withTransaction {
            productDao.upsert(product.toEntity())
            productDao.deactivate(previousBarcode, DateUtils.now())
        }
        catalogSyncRepository.queueProductUpsert(product)
        if (previousBarcode != product.barcode) {
            catalogSyncRepository.queueProductDeactivate(previousBarcode, previousProduct)
        }
        flushSyncBestEffort()
    }

    override suspend fun deactivate(barcode: String) {
        val existing = productDao.getByBarcode(barcode)?.toDomain()
        productDao.deactivate(barcode, DateUtils.now())
        catalogSyncRepository.queueProductDeactivate(barcode, existing)
        flushSyncBestEffort()
    }

    override suspend fun deactivateAll() {
        val existingProducts = productDao.listAllActiveOnce().map { it.toDomain() }
        productDao.deactivateAll(DateUtils.now())
        existingProducts.forEach { product ->
            catalogSyncRepository.queueProductDeactivate(product.barcode, product)
        }
        flushSyncBestEffort()
    }

    override suspend fun resetCatalog() {
        val existingProducts = productDao.listAllActiveOnce().map { it.toDomain() }
        database.withTransaction {
            productDao.deactivateAll(DateUtils.now())
            productDao.deleteWithoutSaleReferences()
        }
        existingProducts.forEach { product ->
            catalogSyncRepository.queueProductDeactivate(product.barcode, product)
        }
        flushSyncBestEffort()
    }

    override suspend fun updateStock(barcode: String, newQty: Int) {
        productDao.updateStock(barcode, newQty, DateUtils.now())
    }

    override suspend fun updateSalePrice(barcode: String, newSalePriceKurus: Long) {
        productDao.updateSalePrice(barcode, newSalePriceKurus, DateUtils.now())
        productDao.getByBarcode(barcode)?.toDomain()?.let { updated ->
            catalogSyncRepository.queueProductUpsert(updated)
            flushSyncBestEffort()
        }
    }

    override suspend fun listByBarcodes(barcodes: List<String>): List<Product> {
        if (barcodes.isEmpty()) return emptyList()
        return productDao.listByBarcodes(barcodes).map { it.toDomain() }
    }

    override suspend fun listAllActiveOnce(): List<Product> {
        return productDao.listAllActiveOnce().map { it.toDomain() }
    }

    override suspend fun countAllProducts(): Int {
        return productDao.countAllProducts()
    }

    override suspend fun countActiveProducts(): Int {
        return productDao.countActiveProducts()
    }

    override suspend fun restoreCloudCatalog(products: List<Product>, replaceExisting: Boolean) {
        if (products.isEmpty() && !replaceExisting) return

        database.withTransaction {
            if (replaceExisting) {
                productDao.deactivateAll(DateUtils.now())
            }
            if (products.isNotEmpty()) {
                productDao.upsertAll(products.map { it.toEntity() })
            }
        }
    }

    private suspend fun flushSyncBestEffort() {
        runCatching { catalogSyncRepository.flushPending() }
    }
}
