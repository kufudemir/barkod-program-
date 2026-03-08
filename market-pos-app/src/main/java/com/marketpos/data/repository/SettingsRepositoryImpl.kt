package com.marketpos.data.repository

import com.marketpos.core.util.SecurityUtils
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.AppSaleMode
import com.marketpos.domain.model.AppThemeMode
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
import com.marketpos.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao
) : SettingsRepository {

    override fun observeMode(): Flow<AppMode> {
        return appSettingDao.observe(SettingKeys.APP_MODE)
            .map { entity -> entity?.value?.toModeOrDefault() ?: AppMode.CASHIER }
    }

    override fun observeSaleMode(): Flow<AppSaleMode> {
        return appSettingDao.observe(SettingKeys.APP_SALE_MODE)
            .map { entity -> AppSaleMode.fromStoredValue(entity?.value) }
    }

    override fun observeThemeMode(): Flow<AppThemeMode> {
        return appSettingDao.observe(SettingKeys.THEME_MODE)
            .map { entity -> AppThemeMode.fromStoredValue(entity?.value) }
    }

    override fun observeSerialScanCooldown(): Flow<SerialScanCooldownOption> {
        return appSettingDao.observe(SettingKeys.SERIAL_SCAN_COOLDOWN)
            .map { entity -> SerialScanCooldownOption.fromStoredValue(entity?.value) }
    }

    override fun observeScanBoxSize(): Flow<ScanBoxSizeOption> {
        return appSettingDao.observe(SettingKeys.SCAN_BOX_SIZE)
            .map { entity -> ScanBoxSizeOption.fromStoredValue(entity?.value) }
    }

    override suspend fun getMode(): AppMode {
        return appSettingDao.get(SettingKeys.APP_MODE)?.value?.toModeOrDefault() ?: AppMode.CASHIER
    }

    override suspend fun getSaleMode(): AppSaleMode {
        return AppSaleMode.fromStoredValue(appSettingDao.get(SettingKeys.APP_SALE_MODE)?.value)
    }

    override suspend fun getThemeMode(): AppThemeMode {
        return AppThemeMode.fromStoredValue(appSettingDao.get(SettingKeys.THEME_MODE)?.value)
    }

    override suspend fun setMode(mode: AppMode) {
        appSettingDao.set(AppSettingEntity(SettingKeys.APP_MODE, mode.name))
    }

    override suspend fun setSaleMode(mode: AppSaleMode) {
        appSettingDao.set(AppSettingEntity(SettingKeys.APP_SALE_MODE, mode.name))
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        appSettingDao.set(AppSettingEntity(SettingKeys.THEME_MODE, mode.name))
    }

    override suspend fun getCameraEnabled(mode: AppMode): Boolean {
        val key = if (mode == AppMode.ADMIN) {
            SettingKeys.CAMERA_ENABLED_ADMIN
        } else {
            SettingKeys.CAMERA_ENABLED_CASHIER
        }
        return appSettingDao.get(key)?.value?.toBooleanStrictOrNull() ?: true
    }

    override suspend fun setCameraEnabled(mode: AppMode, enabled: Boolean) {
        val key = if (mode == AppMode.ADMIN) {
            SettingKeys.CAMERA_ENABLED_ADMIN
        } else {
            SettingKeys.CAMERA_ENABLED_CASHIER
        }
        appSettingDao.set(AppSettingEntity(key, enabled.toString()))
    }

    override fun observeStockCountCameraEnabled(): Flow<Boolean> {
        return appSettingDao.observe(SettingKeys.CAMERA_ENABLED_STOCK_COUNT)
            .map { entity -> entity?.value?.toBooleanStrictOrNull() ?: true }
    }

    override suspend fun getStockCountCameraEnabled(): Boolean {
        return appSettingDao.get(SettingKeys.CAMERA_ENABLED_STOCK_COUNT)?.value?.toBooleanStrictOrNull() ?: true
    }

    override suspend fun setStockCountCameraEnabled(enabled: Boolean) {
        appSettingDao.set(AppSettingEntity(SettingKeys.CAMERA_ENABLED_STOCK_COUNT, enabled.toString()))
    }

    override suspend fun getSerialScanCooldown(): SerialScanCooldownOption {
        return SerialScanCooldownOption.fromStoredValue(appSettingDao.get(SettingKeys.SERIAL_SCAN_COOLDOWN)?.value)
    }

    override suspend fun setSerialScanCooldown(option: SerialScanCooldownOption) {
        appSettingDao.set(AppSettingEntity(SettingKeys.SERIAL_SCAN_COOLDOWN, option.name))
    }

    override suspend fun getScanBoxSize(): ScanBoxSizeOption {
        return ScanBoxSizeOption.fromStoredValue(appSettingDao.get(SettingKeys.SCAN_BOX_SIZE)?.value)
    }

    override suspend fun setScanBoxSize(option: ScanBoxSizeOption) {
        appSettingDao.set(AppSettingEntity(SettingKeys.SCAN_BOX_SIZE, option.name))
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val salt = appSettingDao.get(SettingKeys.PIN_SALT)?.value ?: return false
        val hash = appSettingDao.get(SettingKeys.PIN_HASH)?.value ?: return false
        return SecurityUtils.hashPin(pin, salt) == hash
    }

    override suspend fun setPin(pin: String): Result<Unit> {
        if (!pin.matches(Regex("^\\d{4}$"))) {
            return Result.failure(IllegalArgumentException("PIN 4 haneli olmalıdır"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPin(pin, salt)
        appSettingDao.set(AppSettingEntity(SettingKeys.PIN_SALT, salt))
        appSettingDao.set(AppSettingEntity(SettingKeys.PIN_HASH, hash))
        return Result.success(Unit)
    }

    override suspend fun initializeDefaultsIfNeeded() {
        if (appSettingDao.get(SettingKeys.APP_MODE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.APP_MODE, AppMode.CASHIER.name))
        }
        if (appSettingDao.get(SettingKeys.APP_SALE_MODE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.APP_SALE_MODE, AppSaleMode.MOBILE_SALES.name))
        }
        if (appSettingDao.get(SettingKeys.PIN_HASH) == null || appSettingDao.get(SettingKeys.PIN_SALT) == null) {
            setPin("1234")
        }
        if (appSettingDao.get(SettingKeys.THEME_MODE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.THEME_MODE, AppThemeMode.LIGHT.name))
        }
        if (appSettingDao.get(SettingKeys.ROUNDING_MODE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.ROUNDING_MODE, "CEIL_TO_INT"))
        }
        if (appSettingDao.get(SettingKeys.DEFAULT_PERCENT_STEP) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.DEFAULT_PERCENT_STEP, "10"))
        }
        if (appSettingDao.get(SettingKeys.CAMERA_ENABLED_ADMIN) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.CAMERA_ENABLED_ADMIN, "true"))
        }
        if (appSettingDao.get(SettingKeys.CAMERA_ENABLED_CASHIER) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.CAMERA_ENABLED_CASHIER, "true"))
        }
        if (appSettingDao.get(SettingKeys.CAMERA_ENABLED_STOCK_COUNT) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.CAMERA_ENABLED_STOCK_COUNT, "true"))
        }
        if (appSettingDao.get(SettingKeys.SERIAL_SCAN_COOLDOWN) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.SERIAL_SCAN_COOLDOWN, SerialScanCooldownOption.SAFE.name))
        }
        if (appSettingDao.get(SettingKeys.SCAN_BOX_SIZE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.SCAN_BOX_SIZE, ScanBoxSizeOption.MEDIUM.name))
        }
        if (appSettingDao.get(SettingKeys.PREMIUM_TIER) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TIER, "FREE"))
        }
        if (appSettingDao.get(SettingKeys.PREMIUM_SOURCE) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_SOURCE, "NONE"))
        }
        if (appSettingDao.get(SettingKeys.PREMIUM_TRIAL_USED) == null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TRIAL_USED, "false"))
        }
    }

    private fun String.toModeOrDefault(): AppMode {
        return runCatching { AppMode.valueOf(this) }.getOrDefault(AppMode.CASHIER)
    }
}
