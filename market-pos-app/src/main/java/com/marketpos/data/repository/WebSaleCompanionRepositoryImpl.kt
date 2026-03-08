package com.marketpos.data.repository

import android.content.Context
import com.marketpos.core.sync.CatalogSyncWorker
import com.marketpos.data.network.ActiveWebSaleSessionResponse
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.domain.model.ActiveWebPosSessionState
import com.marketpos.domain.model.CompanionCartItem
import com.marketpos.domain.model.CompanionPrintPayload
import com.marketpos.domain.model.CompanionRecentSale
import com.marketpos.domain.model.CompanionSaleReceipt
import com.marketpos.domain.model.CompanionSaleSummary
import com.marketpos.domain.model.MobilePosSaleSyncPayload
import com.marketpos.domain.repository.SyncOutboxRepository
import com.marketpos.domain.repository.WebSaleCompanionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class WebSaleCompanionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: BarkodSpaceApiClient,
    private val syncOutboxRepository: SyncOutboxRepository
) : WebSaleCompanionRepository {

    override suspend fun getActiveSession(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.fetchActiveWebSaleSession(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName
        ).toDomain()
    }

    override suspend fun scanBarcode(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.scanWebSale(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode
        ).toDomain()
    }

    override suspend fun incrementItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.incrementWebSaleItem(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode
        ).toDomain()
    }

    override suspend fun decrementItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.decrementWebSaleItem(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode
        ).toDomain()
    }

    override suspend fun removeItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.removeWebSaleItem(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode
        ).toDomain()
    }

    override suspend fun setCustomPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        salePriceKurus: Long
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.setWebSaleCustomPrice(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode,
            salePriceKurus = salePriceKurus
        ).toDomain()
    }

    override suspend fun applyPercentDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        percent: Double
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.applyWebSalePercentDiscount(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode,
            percent = percent
        ).toDomain()
    }

    override suspend fun applyFixedDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        discountKurus: Long
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.applyWebSaleFixedDiscount(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode,
            discountKurus = discountKurus
        ).toDomain()
    }

    override suspend fun resetPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.resetWebSaleItemPrice(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName,
            barcode = barcode
        ).toDomain()
    }

    override suspend fun completeSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<ActiveWebPosSessionState> = runCatching {
        apiClient.completeWebSale(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName
        ).toDomain()
    }

    override suspend fun triggerPrint(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<CompanionPrintPayload> = runCatching {
        val response = apiClient.triggerWebSalePrint(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName
        )

        CompanionPrintPayload(
            printReady = response.printReady,
            message = response.message,
            printUrl = response.printUrl,
            previewUrl = response.previewUrl,
            pdfUrl = response.pdfUrl,
            saleId = response.saleId
        )
    }

    override suspend fun publishMobilePosSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        payload: MobilePosSaleSyncPayload
    ): Result<Unit> {
        return runCatching {
            apiClient.publishMobilePosSale(
                accessToken = accessToken,
                companyCode = companyCode,
                deviceUid = deviceUid,
                deviceName = deviceName,
                payload = payload
            )
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { networkError ->
                runCatching {
                    val payloadJson = JSONObject()
                        .put("companyCode", companyCode)
                        .put("deviceUid", deviceUid)
                        .put("deviceName", deviceName)
                        .put("localSaleId", payload.localSaleId)
                        .put("createdAt", payload.createdAt)
                        .put("totalItems", payload.totalItems)
                        .put("totalAmountKurus", payload.totalAmountKurus)
                        .put("totalCostKurus", payload.totalCostKurus)
                        .put("profitKurus", payload.profitKurus)
                        .put("paymentMethod", payload.paymentMethod)
                        .put(
                            "items",
                            JSONArray().apply {
                                payload.items.forEach { item ->
                                    put(
                                        JSONObject()
                                            .put("barcode", item.barcode)
                                            .put("productName", item.productName)
                                            .put("quantity", item.quantity)
                                            .put("unitSalePriceKurus", item.unitSalePriceKurus)
                                            .put("unitCostPriceKurus", item.unitCostPriceKurus)
                                            .put("lineTotalKurus", item.lineTotalKurus)
                                            .put("lineProfitKurus", item.lineProfitKurus)
                                    )
                                }
                            }
                        )
                        .toString()

                    syncOutboxRepository.enqueue(
                        eventType = "LOCAL_SALE_PUBLISH",
                        payloadJson = payloadJson
                    ).getOrThrow()

                    CatalogSyncWorker.enqueue(context)
                }.fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = {
                        Result.failure(
                            IllegalStateException(
                                "Satis buluta gonderilemedi ve kuyruga da eklenemedi: ${networkError.message ?: "bilinmeyen hata"}"
                            )
                        )
                    }
                )
            }
        )
    }

    private fun ActiveWebSaleSessionResponse.toDomain(): ActiveWebPosSessionState {
        return ActiveWebPosSessionState(
            hasActiveSession = hasActiveSession,
            companyCode = companyCode,
            companyName = companyName,
            branchName = branchName,
            registerName = registerName,
            posSessionId = posSessionId,
            saleSessionId = saleSessionId,
            saleSessionLabel = saleSessionLabel,
            summary = CompanionSaleSummary(
                itemCount = summary.itemCount,
                totalAmountKurus = summary.totalAmountKurus,
                canCheckout = summary.canCheckout
            ),
            cartItems = cartItems.map { item ->
                CompanionCartItem(
                    barcode = item.barcode,
                    productName = item.productName,
                    quantity = item.quantity,
                    baseSalePriceKurus = item.baseSalePriceKurus,
                    salePriceKurus = item.salePriceKurus,
                    lineTotalKurus = item.lineTotalKurus,
                    hasCustomPrice = item.hasCustomPrice
                )
            },
            lastSale = lastSale?.let { sale ->
                CompanionSaleReceipt(
                    saleId = sale.saleId,
                    totalAmountKurus = sale.totalAmountKurus,
                    totalItems = sale.totalItems,
                    completedAtEpochMs = sale.completedAtEpochMs
                )
            },
            recentSales = recentSales.map { sale ->
                CompanionRecentSale(
                    saleId = sale.saleId,
                    totalAmountKurus = sale.totalAmountKurus,
                    totalItems = sale.totalItems,
                    completedAtEpochMs = sale.completedAtEpochMs,
                    completedAtLabel = sale.completedAtLabel,
                    registerName = sale.registerName,
                    paymentMethod = sale.paymentMethod
                )
            },
            message = message
        )
    }
}
