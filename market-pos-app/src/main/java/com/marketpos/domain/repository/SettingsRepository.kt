package com.marketpos.domain.repository

import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.AppSaleMode
import com.marketpos.domain.model.AppThemeMode
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeMode(): Flow<AppMode>
    fun observeSaleMode(): Flow<AppSaleMode>
    fun observeThemeMode(): Flow<AppThemeMode>
    fun observeSerialScanCooldown(): Flow<SerialScanCooldownOption>
    fun observeScanBoxSize(): Flow<ScanBoxSizeOption>
    suspend fun getMode(): AppMode
    suspend fun getSaleMode(): AppSaleMode
    suspend fun getThemeMode(): AppThemeMode
    suspend fun setMode(mode: AppMode)
    suspend fun setSaleMode(mode: AppSaleMode)
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun getCameraEnabled(mode: AppMode): Boolean
    suspend fun setCameraEnabled(mode: AppMode, enabled: Boolean)
    fun observeStockCountCameraEnabled(): Flow<Boolean>
    suspend fun getStockCountCameraEnabled(): Boolean
    suspend fun setStockCountCameraEnabled(enabled: Boolean)
    suspend fun getSerialScanCooldown(): SerialScanCooldownOption
    suspend fun setSerialScanCooldown(option: SerialScanCooldownOption)
    suspend fun getScanBoxSize(): ScanBoxSizeOption
    suspend fun setScanBoxSize(option: ScanBoxSizeOption)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun setPin(pin: String): Result<Unit>
    suspend fun initializeDefaultsIfNeeded()
}
