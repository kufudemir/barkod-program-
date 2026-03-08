package com.marketpos.domain.repository

import com.marketpos.domain.model.ActiveWebPosSessionState
import com.marketpos.domain.model.CompanionPrintPayload
import com.marketpos.domain.model.MobilePosSaleSyncPayload

interface WebSaleCompanionRepository {
    suspend fun getActiveSession(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<ActiveWebPosSessionState>

    suspend fun scanBarcode(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState>

    suspend fun incrementItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState>

    suspend fun decrementItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState>

    suspend fun removeItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState>

    suspend fun setCustomPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        salePriceKurus: Long
    ): Result<ActiveWebPosSessionState>

    suspend fun applyPercentDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        percent: Double
    ): Result<ActiveWebPosSessionState>

    suspend fun applyFixedDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        discountKurus: Long
    ): Result<ActiveWebPosSessionState>

    suspend fun resetPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): Result<ActiveWebPosSessionState>

    suspend fun completeSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<ActiveWebPosSessionState>

    suspend fun triggerPrint(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): Result<CompanionPrintPayload>

    suspend fun publishMobilePosSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        payload: MobilePosSaleSyncPayload
    ): Result<Unit>
}
