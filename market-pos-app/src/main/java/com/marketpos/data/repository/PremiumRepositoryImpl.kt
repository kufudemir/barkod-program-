package com.marketpos.data.repository

import com.marketpos.core.premium.DeviceCodeProvider
import com.marketpos.core.premium.LicenseVerifier
import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.domain.model.AppTier
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.PremiumSource
import com.marketpos.domain.model.PremiumState
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.PremiumRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val licenseVerifier: LicenseVerifier,
    private val deviceCodeProvider: DeviceCodeProvider,
    private val accountSessionRepository: AccountSessionRepository,
    private val apiClient: BarkodSpaceApiClient
) : PremiumRepository {

    override fun observeState(): Flow<PremiumState> {
        return combine(
            appSettingDao.observe(SettingKeys.PREMIUM_TIER),
            appSettingDao.observe(SettingKeys.PREMIUM_SOURCE),
            appSettingDao.observe(SettingKeys.PREMIUM_LICENSE_CODE),
            appSettingDao.observe(SettingKeys.PREMIUM_ACTIVATED_AT),
            appSettingDao.observe(SettingKeys.PREMIUM_EXPIRES_AT),
            appSettingDao.observe(SettingKeys.PREMIUM_TRIAL_USED)
        ) { values ->
            val tier = values[0]
            val source = values[1]
            val license = values[2]
            val activatedAt = values[3]
            val expiresAt = values[4]
            val trialUsed = values[5]
            PremiumState(
                tier = (tier as AppSettingEntity?)?.value.toTier(),
                source = (source as AppSettingEntity?)?.value.toSource(),
                deviceCode = deviceCodeProvider.getDeviceCode(),
                activatedAt = (activatedAt as AppSettingEntity?)?.value?.toLongOrNull(),
                expiresAt = (expiresAt as AppSettingEntity?)?.value?.toLongOrNull(),
                licenseCodeMasked = (license as AppSettingEntity?)?.value?.maskLicenseCode(),
                trialUsed = (trialUsed as AppSettingEntity?)?.value?.toBooleanStrictOrNull() ?: false
            )
        }.map(::deriveRuntimeState)
    }

    override suspend fun getState(): PremiumState = observeState().first()

    override suspend fun hasAccess(feature: PremiumFeature): Boolean {
        val state = getState()
        return when (feature) {
            PremiumFeature.SERIAL_SCAN,
            PremiumFeature.WEB_NAME_SUGGESTION,
            PremiumFeature.OCR_NAME_SCAN,
            PremiumFeature.WEB_BARCODE_SEARCH,
            PremiumFeature.BARKOD_BANKASI_IMPORT,
            PremiumFeature.BULK_PRICE_UPDATE,
            PremiumFeature.LINE_PRICE_OVERRIDE,
            PremiumFeature.REPORTS,
            PremiumFeature.STOCK_TRACKING -> state.isPro
        }
    }

    override suspend fun startTrial(days: Int): Result<PremiumState> {
        return runCatching {
            require(days == 3 || days == 7) { "Deneme süresi 3 veya 7 gün olabilir" }
            val current = getState()
            require(!current.trialUsed) { "Deneme sürümü daha önce kullanıldı" }
            require(!current.isPro) { "Premium zaten aktif" }

            val now = DateUtils.now()
            val expiresAt = now + days.toLong() * 24L * 60L * 60L * 1000L
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TIER, AppTier.PRO.name))
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_SOURCE, PremiumSource.TRIAL.name))
            appSettingDao.delete(SettingKeys.PREMIUM_LICENSE_CODE)
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_ACTIVATED_AT, now.toString()))
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_EXPIRES_AT, expiresAt.toString()))
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TRIAL_USED, "true"))
            val state = PremiumState(
                tier = AppTier.PRO,
                source = PremiumSource.TRIAL,
                deviceCode = deviceCodeProvider.getDeviceCode(),
                activatedAt = now,
                expiresAt = expiresAt,
                licenseCodeMasked = null,
                trialUsed = true
            )
            syncToAccountIfPossible(state)
            state
        }
    }

    override suspend fun activateWithLicenseCode(licenseCode: String): Result<PremiumState> {
        return licenseVerifier.verify(licenseCode)
            .mapCatching { payload ->
                appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TIER, payload.tier.name))
                appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_SOURCE, PremiumSource.LICENSE_CODE.name))
                appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_LICENSE_CODE, licenseCode.trim()))
                appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_ACTIVATED_AT, payload.issuedAt.toString()))
                if (payload.expiresAt != null) {
                    appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_EXPIRES_AT, payload.expiresAt.toString()))
                } else {
                    appSettingDao.delete(SettingKeys.PREMIUM_EXPIRES_AT)
                }
                val state = PremiumState(
                    tier = payload.tier,
                    source = PremiumSource.LICENSE_CODE,
                    deviceCode = deviceCodeProvider.getDeviceCode(),
                    activatedAt = payload.issuedAt,
                    expiresAt = payload.expiresAt,
                    licenseCodeMasked = licenseCode.maskLicenseCode(),
                    trialUsed = getState().trialUsed
                )
                syncToAccountIfPossible(state)
                state
            }
    }

    override suspend fun clearLicense(): Result<Unit> {
        return runCatching {
            clearStoredPremium(keepTrialUsed = true)
            syncToAccountIfPossible(
                PremiumState(
                    tier = AppTier.FREE,
                    source = PremiumSource.NONE,
                    deviceCode = deviceCodeProvider.getDeviceCode()
                )
            )
        }
    }

    private suspend fun clearStoredPremium(keepTrialUsed: Boolean) {
        appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TIER, AppTier.FREE.name))
        appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_SOURCE, PremiumSource.NONE.name))
        appSettingDao.delete(SettingKeys.PREMIUM_LICENSE_CODE)
        appSettingDao.delete(SettingKeys.PREMIUM_ACTIVATED_AT)
        appSettingDao.delete(SettingKeys.PREMIUM_EXPIRES_AT)
        if (!keepTrialUsed) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TRIAL_USED, "false"))
        }
    }

    private fun String?.toTier(): AppTier = runCatching { AppTier.valueOf(this.orEmpty()) }.getOrDefault(AppTier.FREE)

    private fun String?.toSource(): PremiumSource =
        runCatching { PremiumSource.valueOf(this.orEmpty()) }.getOrDefault(PremiumSource.NONE)

    private fun String.maskLicenseCode(): String {
        if (length <= 10) return this
        return take(6) + "..." + takeLast(4)
    }

    private fun deriveRuntimeState(state: PremiumState): PremiumState {
        if (state.tier != AppTier.PRO) return state
        if (state.expiresAt != null && state.expiresAt < DateUtils.now()) {
            return state.copy(
                tier = AppTier.FREE,
                source = PremiumSource.NONE,
                activatedAt = null,
                expiresAt = null,
                licenseCodeMasked = null
            )
        }
        if (state.source == PremiumSource.LICENSE_CODE && state.licenseCodeMasked.isNullOrBlank()) {
            return state.copy(
                tier = AppTier.FREE,
                source = PremiumSource.NONE,
                activatedAt = null,
                expiresAt = null,
                licenseCodeMasked = null
            )
        }
        return state
    }

    private suspend fun syncToAccountIfPossible(state: PremiumState) {
        val accessToken = accountSessionRepository.getAccessToken() ?: return
        runCatching {
            apiClient.syncMobileUserPremium(
                accessToken = accessToken,
                tier = state.tier.name,
                source = state.source.name,
                activatedAt = state.activatedAt,
                expiresAt = state.expiresAt,
                licenseCodeMasked = state.licenseCodeMasked
            )
        }
    }
}
