package com.marketpos.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.marketpos.core.sync.CatalogSyncWorker
import com.marketpos.data.db.AppDatabase
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.dao.ProductDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.ProductEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.domain.model.CloudCatalogChange
import com.marketpos.domain.model.MobilePosSaleSyncItem
import com.marketpos.domain.model.MobilePosSaleSyncPayload
import com.marketpos.domain.model.Product
import com.marketpos.domain.model.SyncFlushResult
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.CatalogSyncRepository
import com.marketpos.domain.repository.SyncOutboxRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class CatalogSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val appSettingDao: AppSettingDao,
    private val productDao: ProductDao,
    private val activationRepository: ActivationRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val syncOutboxRepository: SyncOutboxRepository,
    private val apiClient: BarkodSpaceApiClient
) : CatalogSyncRepository {

    override suspend fun queueProductUpsert(product: Product): Result<Unit> {
        val payloadJson = JSONObject()
            .put("barcode", product.barcode)
            .put("name", product.name)
            .put("salePriceKurus", product.salePriceKurus)
            .put("costPriceKurus", product.costPriceKurus)
            .put("groupName", product.groupName)
            .put("note", product.note)
            .put("sourceUpdatedAt", product.updatedAt)
            .put("isActive", product.isActive)
            .toString()

        return syncOutboxRepository.enqueue(
            eventType = "PRODUCT_UPSERT",
            payloadJson = payloadJson
        ).onSuccess {
            CatalogSyncWorker.enqueue(context)
        }
    }

    override suspend fun queueProductDeactivate(barcode: String, lastKnownProduct: Product?): Result<Unit> {
        if (lastKnownProduct == null) {
            return Result.failure(IllegalArgumentException("Pasiflestirilecek urun verisi bulunamadi"))
        }

        return queueProductUpsert(
            lastKnownProduct.copy(
                barcode = barcode,
                isActive = false
            )
        )
    }

    override suspend fun flushPending(): Result<SyncFlushResult> = runCatching {
        val activationState = activationRepository.getActivationState()
        if (!activationState.isActivated || activationState.activationToken.isNullOrBlank()) {
            throw IllegalStateException("Firma aktivasyonu gerekli")
        }

        val pendingItems = syncOutboxRepository.listPending(limit = 50)
        val successEventUuids = mutableListOf<String>()
        val failedEventUuids = mutableListOf<String>()
        val failedMessagesByEvent = mutableMapOf<String, String>()

        val productEvents = pendingItems.filter { it.eventType == EVENT_PRODUCT_UPSERT }
        val mobileSaleEvents = pendingItems.filter { it.eventType == EVENT_LOCAL_SALE_PUBLISH }
        val unsupportedEvents = pendingItems.filter {
            it.eventType != EVENT_PRODUCT_UPSERT && it.eventType != EVENT_LOCAL_SALE_PUBLISH
        }

        if (productEvents.isNotEmpty()) {
            val events = JSONArray().apply {
                productEvents.forEach { item ->
                    put(
                        JSONObject()
                            .put("eventUuid", item.eventUuid)
                            .put("type", item.eventType)
                            .put("occurredAt", item.createdAt)
                            .put("payload", JSONObject(item.payloadJson))
                    )
                }
            }

            val response = apiClient.syncCatalogBatch(
                activationToken = activationState.activationToken,
                deviceUid = activationState.deviceUid ?: activationRepository.getDeviceUid(),
                batchUuid = UUID.randomUUID().toString(),
                eventsJsonArray = events
            )

            response.results.forEach { result ->
                when (result.status) {
                    "processed", "duplicate" -> successEventUuids += result.eventUuid
                    else -> {
                        failedEventUuids += result.eventUuid
                        failedMessagesByEvent[result.eventUuid] =
                            result.message?.takeIf { it.isNotBlank() } ?: "Sunucu urun eventini reddetti"
                    }
                }
            }
        }

        if (mobileSaleEvents.isNotEmpty()) {
            val accessToken = accountSessionRepository.getAccessToken()
                ?: throw IllegalStateException("Kayitli kullanici oturumu bulunamadi")
            val fallbackCompanyCode = activationState.companyCode?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Firma kodu bulunamadi")
            val fallbackDeviceUid = activationState.deviceUid?.takeIf { it.isNotBlank() }
                ?: activationRepository.getDeviceUid()
            val fallbackDeviceName = activationState.deviceName?.takeIf { it.isNotBlank() }
                ?: activationRepository.getDeviceName()

            mobileSaleEvents.forEach { item ->
                runCatching {
                    val payloadNode = JSONObject(item.payloadJson)
                    val syncPayload = parseMobileSalePayload(payloadNode)
                    val companyCode = payloadNode.optString("companyCode").takeIf { it.isNotBlank() } ?: fallbackCompanyCode
                    val deviceUid = payloadNode.optString("deviceUid").takeIf { it.isNotBlank() } ?: fallbackDeviceUid
                    val deviceName = payloadNode.optString("deviceName").takeIf { it.isNotBlank() } ?: fallbackDeviceName

                    apiClient.publishMobilePosSale(
                        accessToken = accessToken,
                        companyCode = companyCode,
                        deviceUid = deviceUid,
                        deviceName = deviceName,
                        payload = syncPayload
                    )
                    successEventUuids += item.eventUuid
                }.onFailure { error ->
                    failedEventUuids += item.eventUuid
                    failedMessagesByEvent[item.eventUuid] = error.message ?: "Mobil satis buluta gonderilemedi"
                }
            }
        }

        if (unsupportedEvents.isNotEmpty()) {
            unsupportedEvents.forEach { item ->
                failedEventUuids += item.eventUuid
                failedMessagesByEvent[item.eventUuid] = "Desteklenmeyen outbox event tipi: ${item.eventType}"
            }
        }

        if (successEventUuids.isNotEmpty()) {
            syncOutboxRepository.deleteByEventUuids(successEventUuids)
        }
        if (failedEventUuids.isNotEmpty()) {
            failedEventUuids
                .distinct()
                .forEach { eventUuid ->
                    val message = failedMessagesByEvent[eventUuid] ?: "Sunucu bazi eventleri reddetti"
                    syncOutboxRepository.markFailed(listOf(eventUuid), message)
                }
        }

        val remoteAppliedCount = pullRemoteCatalogChanges(activationState.companyCode)
        val pendingCount = syncOutboxRepository.countPending()
        val failedCount = failedEventUuids.distinct().size

        activationRepository.updateSyncStatus(
            lastSuccessAt = System.currentTimeMillis(),
            lastError = if (failedCount == 0) null else "Bazi senkron eventleri basarisiz"
        )

        SyncFlushResult(
            processedCount = successEventUuids.size + remoteAppliedCount,
            failedCount = failedCount,
            pendingCount = pendingCount
        )
    }.onFailure {
        activationRepository.updateSyncStatus(lastError = it.message ?: "Senkron hatasi")
    }

    private suspend fun pullRemoteCatalogChanges(companyCode: String?): Int {
        val accessToken = accountSessionRepository.getAccessToken()
        val normalizedCompanyCode = companyCode?.trim().orEmpty()
        if (accessToken.isNullOrBlank() || normalizedCompanyCode.isBlank()) {
            return 0
        }

        var cursor = readCursor(normalizedCompanyCode)
        var appliedCount = 0
        var pageCount = 0

        while (pageCount < MAX_PULL_PAGE_COUNT) {
            val page = activationRepository.fetchCompanyCatalogChanges(
                companyCode = normalizedCompanyCode,
                sinceUpdatedAt = cursor,
                limit = PULL_PAGE_LIMIT
            ).getOrThrow()

            if (page.changes.isEmpty()) {
                if (page.nextCursor > cursor) {
                    cursor = page.nextCursor
                    writeCursor(normalizedCompanyCode, cursor)
                }
                break
            }

            appliedCount += applyRemoteChanges(page.changes)
            cursor = maxOf(
                cursor,
                page.nextCursor,
                page.changes.maxOfOrNull { it.updatedAt } ?: cursor
            )
            writeCursor(normalizedCompanyCode, cursor)

            pageCount++
            if (!page.hasMore) {
                break
            }
        }

        return appliedCount
    }

    private suspend fun applyRemoteChanges(changes: List<CloudCatalogChange>): Int {
        if (changes.isEmpty()) return 0

        var appliedCount = 0
        database.withTransaction {
            changes.forEach { change ->
                val barcode = change.barcode.trim()
                val name = change.name.trim()
                if (barcode.isBlank() || name.isBlank() || change.updatedAt <= 0L) {
                    return@forEach
                }

                val existing = productDao.getAnyByBarcode(barcode)
                if (existing != null && existing.updatedAt >= change.updatedAt) {
                    return@forEach
                }

                if (!change.isActive) {
                    if (existing != null) {
                        productDao.upsert(
                            existing.copy(
                                name = name,
                                groupName = change.groupName,
                                salePriceKurus = change.salePriceKurus.coerceAtLeast(0L),
                                costPriceKurus = change.costPriceKurus.coerceAtLeast(0L),
                                note = change.note,
                                updatedAt = change.updatedAt,
                                isActive = false
                            )
                        )
                        appliedCount++
                    }
                    return@forEach
                }

                val merged = if (existing == null) {
                    ProductEntity(
                        barcode = barcode,
                        name = name,
                        groupName = change.groupName,
                        salePriceKurus = change.salePriceKurus.coerceAtLeast(0L),
                        costPriceKurus = change.costPriceKurus.coerceAtLeast(0L),
                        stockQty = 0,
                        minStockQty = 0,
                        note = change.note,
                        createdAt = change.updatedAt,
                        updatedAt = change.updatedAt,
                        isActive = true
                    )
                } else {
                    existing.copy(
                        name = name,
                        groupName = change.groupName,
                        salePriceKurus = change.salePriceKurus.coerceAtLeast(0L),
                        costPriceKurus = change.costPriceKurus.coerceAtLeast(0L),
                        note = change.note,
                        updatedAt = change.updatedAt,
                        isActive = true
                    )
                }

                productDao.upsert(merged)
                appliedCount++
            }
        }

        return appliedCount
    }

    private suspend fun readCursor(companyCode: String): Long {
        val cursorCompany = appSettingDao.get(SettingKeys.SYNC_CATALOG_CURSOR_COMPANY)?.value
        if (!cursorCompany.equals(companyCode, ignoreCase = true)) {
            writeCursor(companyCode, 0L)
            return 0L
        }

        return appSettingDao.get(SettingKeys.SYNC_CATALOG_CURSOR)?.value?.toLongOrNull() ?: 0L
    }

    private suspend fun writeCursor(companyCode: String, cursor: Long) {
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_CATALOG_CURSOR_COMPANY, companyCode))
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_CATALOG_CURSOR, cursor.coerceAtLeast(0L).toString()))
    }

    private fun parseMobileSalePayload(payloadNode: JSONObject): MobilePosSaleSyncPayload {
        val itemsNode = payloadNode.optJSONArray("items") ?: JSONArray()
        val items = buildList {
            for (index in 0 until itemsNode.length()) {
                val item = itemsNode.optJSONObject(index) ?: continue
                add(
                    MobilePosSaleSyncItem(
                        barcode = item.optString("barcode"),
                        productName = item.optString("productName"),
                        quantity = item.optInt("quantity").coerceAtLeast(1),
                        unitSalePriceKurus = item.optLong("unitSalePriceKurus").coerceAtLeast(1L),
                        unitCostPriceKurus = item.optLong("unitCostPriceKurus").coerceAtLeast(0L),
                        lineTotalKurus = item.optLong("lineTotalKurus").coerceAtLeast(1L),
                        lineProfitKurus = item.optLong("lineProfitKurus")
                    )
                )
            }
        }
        if (items.isEmpty()) {
            throw IllegalStateException("Mobil satis payload bos item iceriyor")
        }

        return MobilePosSaleSyncPayload(
            localSaleId = payloadNode.optLong("localSaleId"),
            createdAt = payloadNode.optLong("createdAt"),
            totalItems = payloadNode.optInt("totalItems"),
            totalAmountKurus = payloadNode.optLong("totalAmountKurus"),
            totalCostKurus = payloadNode.optLong("totalCostKurus"),
            profitKurus = payloadNode.optLong("profitKurus"),
            paymentMethod = payloadNode.optString("paymentMethod").ifBlank { "cash" },
            items = items
        )
    }

    private companion object {
        private const val EVENT_PRODUCT_UPSERT = "PRODUCT_UPSERT"
        private const val EVENT_LOCAL_SALE_PUBLISH = "LOCAL_SALE_PUBLISH"
        private const val PULL_PAGE_LIMIT = 200
        private const val MAX_PULL_PAGE_COUNT = 5
    }
}
