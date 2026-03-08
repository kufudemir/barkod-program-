package com.marketpos.data.repository

import androidx.room.withTransaction
import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.AppDatabase
import com.marketpos.data.db.dao.ProductDao
import com.marketpos.data.db.dao.SaleDao
import com.marketpos.data.db.dao.SaleItemDao
import com.marketpos.data.db.entity.SaleEntity
import com.marketpos.data.db.entity.SaleItemEntity
import com.marketpos.data.db.entity.SaleStatus
import com.marketpos.data.db.mapper.toDomain
import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.ProductAggregateReport
import com.marketpos.domain.model.Sale
import com.marketpos.domain.model.SaleItem
import com.marketpos.domain.model.SummaryReport
import com.marketpos.domain.repository.SaleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val productDao: ProductDao
) : SaleRepository {

    override suspend fun createSale(cartItems: List<CartItem>): Result<Long> {
        if (cartItems.isEmpty()) return Result.failure(IllegalArgumentException("Sepet bos"))
        return runCatching {
            database.withTransaction {
                cartItems.forEach { item ->
                    productDao.getByBarcode(item.barcode)
                        ?: throw IllegalStateException("Ürün bulunamadı: ${item.barcode}")
                }

                val totalAmount = cartItems.sumOf { it.lineTotalKurus }
                val totalCost = cartItems.sumOf { it.lineCostKurus }
                val totalProfit = totalAmount - totalCost
                val itemCount = cartItems.sumOf { it.quantity }

                val saleId = saleDao.insertSale(
                    SaleEntity(
                        createdAt = DateUtils.now(),
                        totalAmountKurus = totalAmount,
                        totalCostKurus = totalCost,
                        profitKurus = totalProfit,
                        itemCount = itemCount,
                        status = SaleStatus.COMPLETED
                    )
                )

                val items = cartItems.map { item ->
                    SaleItemEntity(
                        saleId = saleId,
                        productBarcode = item.barcode,
                        productNameSnapshot = item.name,
                        unitBaseSalePriceKurusSnapshot = item.baseSalePriceKurus,
                        unitSalePriceKurusSnapshot = item.salePriceKurus,
                        unitCostPriceKurusSnapshot = item.costPriceKurus,
                        quantity = item.quantity,
                        lineTotalKurus = item.lineTotalKurus,
                        lineCostKurus = item.lineCostKurus
                    )
                }
                saleItemDao.insertItems(items)

                cartItems.forEach { item ->
                    val product = productDao.getByBarcode(item.barcode)
                        ?: throw IllegalStateException("Ürün bulunamadı: ${item.barcode}")
                    productDao.updateStock(
                        barcode = item.barcode,
                        newQty = product.stockQty - item.quantity,
                        updatedAt = DateUtils.now()
                    )
                }
                saleId
            }
        }
    }

    override suspend fun getSummary(fromEpochMillis: Long, toEpochMillis: Long): SummaryReport {
        val summary = saleDao.sumTotalsByDateRange(fromEpochMillis, toEpochMillis)
        return summary?.toDomain() ?: SummaryReport(0L, 0L, 0)
    }

    override suspend fun getTopSelling(
        fromEpochMillis: Long,
        toEpochMillis: Long,
        limit: Int
    ): List<ProductAggregateReport> {
        return saleItemDao.topSellingByDateRange(fromEpochMillis, toEpochMillis, limit).map { it.toDomain() }
    }

    override suspend fun getTopProfit(
        fromEpochMillis: Long,
        toEpochMillis: Long,
        limit: Int
    ): List<ProductAggregateReport> {
        return saleItemDao.topProfitByDateRange(fromEpochMillis, toEpochMillis, limit).map { it.toDomain() }
    }

    override suspend fun getSoldQuantity(
        productBarcode: String,
        fromEpochMillis: Long,
        toEpochMillis: Long
    ): Int {
        return saleItemDao.soldQuantityByProductAndDateRange(productBarcode, fromEpochMillis, toEpochMillis)
    }

    override suspend fun listSales(fromEpochMillis: Long, toEpochMillis: Long): List<Sale> {
        return saleDao.listByDateRange(fromEpochMillis, toEpochMillis).map { it.toDomain() }
    }

    override suspend fun listSaleItemsByRange(fromEpochMillis: Long, toEpochMillis: Long): List<SaleItem> {
        return saleItemDao.listByDateRange(fromEpochMillis, toEpochMillis).map { it.toDomain() }
    }

    override suspend fun getSaleById(saleId: Long): Sale? {
        return saleDao.getById(saleId)?.toDomain()
    }

    override suspend fun getSaleItems(saleId: Long): List<SaleItem> {
        return saleItemDao.listBySaleId(saleId).map { it.toDomain() }
    }

    override suspend fun getTotalSaleCount(): Int {
        return saleDao.countCompletedSales()
    }
}


