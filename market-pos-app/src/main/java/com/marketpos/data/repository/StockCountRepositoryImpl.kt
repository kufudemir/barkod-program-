package com.marketpos.data.repository

import androidx.room.withTransaction
import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.AppDatabase
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.dao.ProductDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.ProductEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.domain.model.StockCountItem
import com.marketpos.domain.model.StockCountScanResult
import com.marketpos.domain.model.StockCountSession
import com.marketpos.domain.repository.StockCountRepository
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class StockCountRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val appSettingDao: AppSettingDao,
    private val productDao: ProductDao
) : StockCountRepository {

    override fun observeSession(): Flow<StockCountSession> {
        return combine(
            appSettingDao.observe(SettingKeys.STOCK_COUNT_SESSION),
            appSettingDao.observe(SettingKeys.STOCK_COUNT_STARTED_AT),
            productDao.listAllActive()
        ) { sessionEntity, startedAtEntity, products ->
            val counts = parseSession(sessionEntity?.value)
            val productMap = products.associateBy { it.barcode }
            StockCountSession(
                startedAt = startedAtEntity?.value?.toLongOrNull(),
                items = counts.values
                    .map { entry ->
                        val product = productMap[entry.barcode]
                        if (product != null) {
                            product.toStockCountItem(entry.countedQty)
                        } else {
                            StockCountItem(
                                barcode = entry.barcode,
                                name = "${entry.nameSnapshot} (Arşivlendi)",
                                expectedQty = entry.expectedQtySnapshot,
                                countedQty = entry.countedQty,
                                differenceQty = entry.countedQty - entry.expectedQtySnapshot
                            )
                        }
                    }
                    .sortedWith(compareByDescending<StockCountItem> { kotlin.math.abs(it.differenceQty) }.thenBy { it.name.lowercase() })
            )
        }
    }

    override suspend fun addScan(barcode: String): Result<StockCountScanResult> {
        return runCatching {
            val product = productDao.getByBarcode(barcode)
                ?: throw IllegalArgumentException("Barkod sistemde aktif ürün olarak bulunamadı")
            val counts = readCurrentSession().toMutableMap()
            val newCount = (counts[barcode]?.countedQty ?: 0) + 1
            counts[barcode] = StoredStockCountEntry(
                barcode = barcode,
                countedQty = newCount,
                nameSnapshot = product.name,
                expectedQtySnapshot = product.stockQty
            )
            persistSession(counts)
            StockCountScanResult(
                item = product.toStockCountItem(newCount),
                newCount = newCount
            )
        }
    }

    override suspend fun updateCount(barcode: String, countedQty: Int): Result<Unit> {
        return runCatching {
            require(countedQty >= 0) { "Sayım adedi negatif olamaz" }
            val counts = readCurrentSession().toMutableMap()
            val existing = counts[barcode]
            val product = productDao.getByBarcode(barcode)
            if (countedQty == 0) {
                counts.remove(barcode)
            } else {
                counts[barcode] = StoredStockCountEntry(
                    barcode = barcode,
                    countedQty = countedQty,
                    nameSnapshot = product?.name ?: existing?.nameSnapshot
                        ?: throw IllegalArgumentException("Ürün bulunamadı"),
                    expectedQtySnapshot = product?.stockQty ?: existing?.expectedQtySnapshot
                        ?: throw IllegalArgumentException("Ürün bulunamadı")
                )
            }
            persistSession(counts)
        }
    }

    override suspend fun removeItem(barcode: String): Result<Unit> {
        return runCatching {
            val counts = readCurrentSession().toMutableMap()
            counts.remove(barcode)
            persistSession(counts)
        }
    }

    override suspend fun clearSession(): Result<Unit> {
        return runCatching {
            appSettingDao.delete(SettingKeys.STOCK_COUNT_SESSION)
            appSettingDao.delete(SettingKeys.STOCK_COUNT_STARTED_AT)
        }
    }

    override suspend fun applyCountResult(): Result<Int> {
        return runCatching {
            val counts = readCurrentSession()
            require(counts.isNotEmpty()) { "Uygulanacak sayım sonucu yok" }

            database.withTransaction {
                counts.values.forEach { entry ->
                    productDao.updateStock(entry.barcode, entry.countedQty, DateUtils.now())
                }
                appSettingDao.delete(SettingKeys.STOCK_COUNT_SESSION)
                appSettingDao.delete(SettingKeys.STOCK_COUNT_STARTED_AT)
            }
            counts.size
        }
    }

    private suspend fun readCurrentSession(): Map<String, StoredStockCountEntry> {
        return parseSession(appSettingDao.get(SettingKeys.STOCK_COUNT_SESSION)?.value)
    }

    private suspend fun persistSession(counts: Map<String, StoredStockCountEntry>) {
        if (counts.isEmpty()) {
            appSettingDao.delete(SettingKeys.STOCK_COUNT_SESSION)
            appSettingDao.delete(SettingKeys.STOCK_COUNT_STARTED_AT)
            return
        }
        if (appSettingDao.get(SettingKeys.STOCK_COUNT_STARTED_AT) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.STOCK_COUNT_STARTED_AT, DateUtils.now().toString()))
        }
        val encoded = counts.entries
            .sortedBy { it.key }
            .joinToString(separator = ";") {
                val entry = it.value
                val encodedName = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(entry.nameSnapshot.toByteArray())
                "${entry.barcode}|${entry.countedQty}|${entry.expectedQtySnapshot}|$encodedName"
            }
        appSettingDao.set(AppSettingEntity(SettingKeys.STOCK_COUNT_SESSION, encoded))
    }

    private fun parseSession(raw: String?): Map<String, StoredStockCountEntry> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(";")
            .mapNotNull { token ->
                parseSessionToken(token)
            }
            .associateBy { it.barcode }
    }

    private fun parseSessionToken(token: String): StoredStockCountEntry? {
        if (token.contains("|")) {
            val parts = token.split("|")
            val barcode = parts.getOrNull(0)?.trim().orEmpty()
            val countedQty = parts.getOrNull(1)?.trim()?.toIntOrNull()
            val expectedQty = parts.getOrNull(2)?.trim()?.toIntOrNull()
            val nameSnapshot = parts.getOrNull(3)?.takeIf { it.isNotBlank() }?.let {
                runCatching { String(Base64.getUrlDecoder().decode(it)) }.getOrNull()
            }
            if (barcode.isBlank() || countedQty == null || countedQty < 0 || expectedQty == null || expectedQty < 0 || nameSnapshot.isNullOrBlank()) {
                return null
            }
            return StoredStockCountEntry(
                barcode = barcode,
                countedQty = countedQty,
                nameSnapshot = nameSnapshot,
                expectedQtySnapshot = expectedQty
            )
        }

        val legacyParts = token.split("=")
        val barcode = legacyParts.getOrNull(0)?.trim().orEmpty()
        val count = legacyParts.getOrNull(1)?.trim()?.toIntOrNull()
        if (barcode.isBlank() || count == null || count < 0) return null
        return StoredStockCountEntry(
            barcode = barcode,
            countedQty = count,
            nameSnapshot = barcode,
            expectedQtySnapshot = 0
        )
    }

    private fun ProductEntity.toStockCountItem(countedQty: Int): StockCountItem {
        return StockCountItem(
            barcode = barcode,
            name = name,
            expectedQty = stockQty,
            countedQty = countedQty,
            differenceQty = countedQty - stockQty
        )
    }

    private data class StoredStockCountEntry(
        val barcode: String,
        val countedQty: Int,
        val nameSnapshot: String,
        val expectedQtySnapshot: Int
    )
}
