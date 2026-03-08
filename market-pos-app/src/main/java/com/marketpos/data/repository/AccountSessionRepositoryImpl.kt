package com.marketpos.data.repository

import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.data.network.AuthSessionResponse
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.core.device.DeviceIdentityProvider
import com.marketpos.domain.model.AppTier
import com.marketpos.domain.model.PremiumSource
import com.marketpos.domain.model.AccountSessionState
import com.marketpos.domain.model.AccountSessionType
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.LegalConsentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AccountSessionRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val apiClient: BarkodSpaceApiClient,
    private val deviceIdentityProvider: DeviceIdentityProvider,
    private val legalConsentRepository: LegalConsentRepository
) : AccountSessionRepository {

    override fun observeState(): Flow<AccountSessionState> {
        return appSettingDao.observeAll().map { entities ->
            val values = entities.associateBy { it.key }.mapValues { it.value.value }
            buildState(values)
        }
    }

    override suspend fun getState(): AccountSessionState {
        val values = appSettingDao.getMany(
            listOf(
                SettingKeys.SESSION_TYPE,
                SettingKeys.SESSION_USER_ID,
                SettingKeys.SESSION_USER_NAME,
                SettingKeys.SESSION_USER_EMAIL,
                SettingKeys.SESSION_AUTH_TOKEN
            )
        ).associateBy { it.key }.mapValues { it.value.value }

        return buildState(values)
    }

    override suspend fun continueAsGuest(): Result<AccountSessionState> = runCatching {
        persistGuestSession()
        getState()
    }

    override suspend fun register(name: String, email: String, password: String): Result<AccountSessionState> = runCatching {
        val response = apiClient.registerMobileUser(
            name = name.trim(),
            email = email.trim(),
            password = password,
            deviceUid = getDeviceUid(),
            deviceName = getDeviceName()
        )
        persistRegisteredSession(response)
        syncLegalConsentAfterAuthenticatedSession(response.accessToken)
        syncPremiumAfterAuthenticatedSession(response.accessToken)
        getState()
    }

    override suspend fun login(email: String, password: String): Result<AccountSessionState> = runCatching {
        val response = apiClient.loginMobileUser(
            email = email.trim(),
            password = password,
            deviceUid = getDeviceUid(),
            deviceName = getDeviceName()
        )
        persistRegisteredSession(response)
        syncLegalConsentAfterAuthenticatedSession(response.accessToken)
        syncPremiumAfterAuthenticatedSession(response.accessToken)
        getState()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val accessToken = getAccessToken() ?: error("Kayıtlı oturum bulunamadı")
        apiClient.updateMobileUserPassword(
            accessToken = accessToken,
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }

    override suspend fun requestPasswordReset(email: String): Result<Long?> = runCatching {
        apiClient.requestMobileUserPasswordReset(email.trim()).expiresAt
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> = runCatching {
        apiClient.resetMobileUserPassword(
            email = email.trim(),
            code = code.trim(),
            newPassword = newPassword
        )
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        getAccessToken()?.let { token ->
            runCatching { apiClient.logoutMobileUser(token) }
        }
        clearLocalSession()
    }

    override suspend fun clearLocalSession() {
        listOf(
            SettingKeys.SESSION_TYPE,
            SettingKeys.SESSION_USER_ID,
            SettingKeys.SESSION_USER_NAME,
            SettingKeys.SESSION_USER_EMAIL,
            SettingKeys.SESSION_AUTH_TOKEN
        ).forEach { appSettingDao.delete(it) }
    }

    override suspend fun getAccessToken(): String? {
        return appSettingDao.get(SettingKeys.SESSION_AUTH_TOKEN)?.value
    }

    private suspend fun getDeviceUid(): String {
        val existing = appSettingDao.get(SettingKeys.SYNC_DEVICE_UID)?.value
        if (!existing.isNullOrBlank()) return existing
        val generated = deviceIdentityProvider.getDeviceUid()
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_UID, generated))
        return generated
    }

    private suspend fun getDeviceName(): String {
        val existing = appSettingDao.get(SettingKeys.SYNC_DEVICE_NAME)?.value
        if (!existing.isNullOrBlank()) return existing
        val generated = deviceIdentityProvider.getDeviceName()
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_NAME, generated))
        return generated
    }

    private suspend fun persistGuestSession() {
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_TYPE, AccountSessionType.GUEST.name))
        appSettingDao.delete(SettingKeys.SESSION_USER_ID)
        appSettingDao.delete(SettingKeys.SESSION_USER_NAME)
        appSettingDao.delete(SettingKeys.SESSION_USER_EMAIL)
        appSettingDao.delete(SettingKeys.SESSION_AUTH_TOKEN)
    }

    private suspend fun persistRegisteredSession(response: AuthSessionResponse) {
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_TYPE, AccountSessionType.REGISTERED.name))
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_USER_ID, response.user.id.toString()))
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_USER_NAME, response.user.name))
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_USER_EMAIL, response.user.email))
        appSettingDao.set(AppSettingEntity(SettingKeys.SESSION_AUTH_TOKEN, response.accessToken))
    }

    private suspend fun refreshPremiumFromServer(accessToken: String) {
        val premium = apiClient.fetchMobileUserPremium(accessToken)
        appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_TIER, premium.tier.ifBlank { AppTier.FREE.name }))
        appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_SOURCE, premium.source.ifBlank { PremiumSource.NONE.name }))
        if (premium.activatedAt != null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_ACTIVATED_AT, premium.activatedAt.toString()))
        } else {
            appSettingDao.delete(SettingKeys.PREMIUM_ACTIVATED_AT)
        }
        if (premium.expiresAt != null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_EXPIRES_AT, premium.expiresAt.toString()))
        } else {
            appSettingDao.delete(SettingKeys.PREMIUM_EXPIRES_AT)
        }
        if (!premium.licenseCodeMasked.isNullOrBlank()) {
            appSettingDao.set(AppSettingEntity(SettingKeys.PREMIUM_LICENSE_CODE, premium.licenseCodeMasked))
        } else {
            appSettingDao.delete(SettingKeys.PREMIUM_LICENSE_CODE)
        }
    }

    private suspend fun syncLegalConsentAfterAuthenticatedSession(accessToken: String) {
        val consentState = legalConsentRepository.getState()
        if (!consentState.isAccepted || consentState.acceptedAt == null) return
        runCatching {
            apiClient.syncMobileUserConsent(
                accessToken = accessToken,
                version = consentState.currentVersion,
                acceptedAt = consentState.acceptedAt
            )
        }
    }

    private suspend fun syncPremiumAfterAuthenticatedSession(accessToken: String) {
        val localPremium = readLocalPremiumSnapshot()
        if (localPremium.tier == AppTier.PRO) {
            runCatching {
                apiClient.syncMobileUserPremium(
                    accessToken = accessToken,
                    tier = localPremium.tier.name,
                    source = localPremium.source.name,
                    activatedAt = localPremium.activatedAt,
                    expiresAt = localPremium.expiresAt,
                    licenseCodeMasked = localPremium.licenseCodeMasked
                )
            }
        }
        refreshPremiumFromServer(accessToken)
    }

    private suspend fun readLocalPremiumSnapshot(): LocalPremiumSnapshot {
        val values = appSettingDao.getMany(
            listOf(
                SettingKeys.PREMIUM_TIER,
                SettingKeys.PREMIUM_SOURCE,
                SettingKeys.PREMIUM_LICENSE_CODE,
                SettingKeys.PREMIUM_ACTIVATED_AT,
                SettingKeys.PREMIUM_EXPIRES_AT
            )
        ).associateBy { it.key }.mapValues { it.value.value }

        val rawLicense = values[SettingKeys.PREMIUM_LICENSE_CODE]
        return LocalPremiumSnapshot(
            tier = runCatching { AppTier.valueOf(values[SettingKeys.PREMIUM_TIER].orEmpty()) }.getOrDefault(AppTier.FREE),
            source = runCatching { PremiumSource.valueOf(values[SettingKeys.PREMIUM_SOURCE].orEmpty()) }.getOrDefault(PremiumSource.NONE),
            activatedAt = values[SettingKeys.PREMIUM_ACTIVATED_AT]?.toLongOrNull(),
            expiresAt = values[SettingKeys.PREMIUM_EXPIRES_AT]?.toLongOrNull(),
            licenseCodeMasked = rawLicense?.maskLicenseCode()
        )
    }

    private fun String.maskLicenseCode(): String {
        if (length <= 10) return this
        return take(6) + "..." + takeLast(4)
    }

    private fun buildState(values: Map<String, String>): AccountSessionState {
        val type = runCatching { AccountSessionType.valueOf(values[SettingKeys.SESSION_TYPE].orEmpty()) }
            .getOrDefault(AccountSessionType.NONE)

        return AccountSessionState(
            type = type,
            userId = values[SettingKeys.SESSION_USER_ID]?.toLongOrNull(),
            userName = values[SettingKeys.SESSION_USER_NAME],
            userEmail = values[SettingKeys.SESSION_USER_EMAIL],
            authToken = values[SettingKeys.SESSION_AUTH_TOKEN]
        )
    }

    private data class LocalPremiumSnapshot(
        val tier: AppTier,
        val source: PremiumSource,
        val activatedAt: Long?,
        val expiresAt: Long?,
        val licenseCodeMasked: String?
    )
}


