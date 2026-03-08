package com.marketpos.data.repository

import com.marketpos.BuildConfig
import com.marketpos.core.device.DeviceIdentityProvider
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.domain.model.CloudCatalogProduct
import com.marketpos.domain.model.CloudCatalogChange
import com.marketpos.domain.model.CloudCatalogChangePage
import com.marketpos.domain.model.CompanyActivationState
import com.marketpos.domain.model.RecoverableCompany
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ActivationRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao,
    private val apiClient: BarkodSpaceApiClient,
    private val deviceIdentityProvider: DeviceIdentityProvider,
    private val accountSessionRepository: AccountSessionRepository
) : ActivationRepository {

    override fun observeActivationState(): Flow<CompanyActivationState> {
        return appSettingDao.observeAll().map { entities ->
            val map = entities.associateBy { it.key }.mapValues { it.value.value }
            buildState(map)
        }
    }

    override suspend fun getActivationState(): CompanyActivationState {
        val entities = appSettingDao.getMany(
            listOf(
                SettingKeys.COMPANY_ID,
                SettingKeys.COMPANY_NAME,
                SettingKeys.COMPANY_CODE,
                SettingKeys.SYNC_DEVICE_UID,
                SettingKeys.SYNC_DEVICE_NAME,
                SettingKeys.SYNC_ACTIVATION_TOKEN,
                SettingKeys.SYNC_LAST_SUCCESS_AT,
                SettingKeys.SYNC_LAST_ERROR
            )
        )
        val map = entities.associateBy { it.key }.mapValues { it.value.value }
        return buildState(map)
    }

    override suspend fun activate(companyName: String): Result<CompanyActivationState> = runCatching {
        val normalizedCompanyName = companyName.trim()
        val response = apiClient.activateDevice(
            companyName = normalizedCompanyName,
            deviceUid = getDeviceUid(),
            deviceName = getDeviceName(),
            appVersion = BuildConfig.VERSION_NAME,
            accountAccessToken = accountSessionRepository.getAccessToken()
        )
        persistActivation(response)
        getActivationState()
    }

    override suspend fun activateAsNewCompany(companyName: String): Result<CompanyActivationState> = runCatching {
        val normalizedCompanyName = companyName.trim()
        val response = apiClient.activateDevice(
            companyName = normalizedCompanyName,
            forceNewCompany = true,
            deviceUid = getDeviceUid(),
            deviceName = getDeviceName(),
            appVersion = BuildConfig.VERSION_NAME,
            accountAccessToken = accountSessionRepository.getAccessToken()
        )
        persistActivation(response)
        getActivationState()
    }

    override suspend fun activateWithCompanyCode(companyCode: String): Result<CompanyActivationState> = runCatching {
        val response = apiClient.activateDevice(
            companyCode = companyCode.trim(),
            deviceUid = getDeviceUid(),
            deviceName = getDeviceName(),
            appVersion = BuildConfig.VERSION_NAME,
            accountAccessToken = accountSessionRepository.getAccessToken()
        )
        persistActivation(response)
        getActivationState()
    }

    private suspend fun persistActivation(response: com.marketpos.data.network.DeviceActivationResponse) {
        val previousCompanyCode = appSettingDao.get(SettingKeys.COMPANY_CODE)?.value
        appSettingDao.set(AppSettingEntity(SettingKeys.COMPANY_ID, response.companyId.toString()))
        appSettingDao.set(AppSettingEntity(SettingKeys.COMPANY_NAME, response.companyName))
        appSettingDao.set(AppSettingEntity(SettingKeys.COMPANY_CODE, response.companyCode))
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_UID, getDeviceUid()))
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_NAME, getDeviceName()))
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_ACTIVATION_TOKEN, response.activationToken))
        appSettingDao.delete(SettingKeys.SYNC_LAST_ERROR)
        if (!previousCompanyCode.equals(response.companyCode, ignoreCase = true)) {
            appSettingDao.delete(SettingKeys.SYNC_CATALOG_CURSOR)
            appSettingDao.delete(SettingKeys.SYNC_CATALOG_CURSOR_COMPANY)
        }
    }

    override suspend fun clearActivation(): Result<Unit> = runCatching {
        listOf(
            SettingKeys.COMPANY_ID,
            SettingKeys.COMPANY_NAME,
            SettingKeys.COMPANY_CODE,
            SettingKeys.SYNC_DEVICE_UID,
            SettingKeys.SYNC_DEVICE_NAME,
            SettingKeys.SYNC_ACTIVATION_TOKEN,
            SettingKeys.SYNC_LAST_SUCCESS_AT,
            SettingKeys.SYNC_LAST_ERROR,
            SettingKeys.SYNC_CATALOG_CURSOR,
            SettingKeys.SYNC_CATALOG_CURSOR_COMPANY
        ).forEach { appSettingDao.delete(it) }
    }

    override suspend fun updateSyncStatus(lastSuccessAt: Long?, lastError: String?) {
        if (lastSuccessAt != null) {
            appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_LAST_SUCCESS_AT, lastSuccessAt.toString()))
        }
        if (lastError.isNullOrBlank()) {
            appSettingDao.delete(SettingKeys.SYNC_LAST_ERROR)
        } else {
            appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_LAST_ERROR, lastError))
        }
    }

    override suspend fun getDeviceUid(): String {
        val existing = appSettingDao.get(SettingKeys.SYNC_DEVICE_UID)?.value
        if (!existing.isNullOrBlank()) return existing
        val generated = deviceIdentityProvider.getDeviceUid()
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_UID, generated))
        return generated
    }

    override suspend fun getDeviceName(): String {
        val existing = appSettingDao.get(SettingKeys.SYNC_DEVICE_NAME)?.value
        if (!existing.isNullOrBlank()) return existing
        val generated = deviceIdentityProvider.getDeviceName()
        appSettingDao.set(AppSettingEntity(SettingKeys.SYNC_DEVICE_NAME, generated))
        return generated
    }

    override suspend fun listOwnedCompanies(): Result<List<RecoverableCompany>> = runCatching {
        val deviceUid = getDeviceUid()
        val accessToken = accountSessionRepository.getAccessToken()
        val companies = if (accessToken.isNullOrBlank()) {
            apiClient.fetchDeviceHistoryCompanies(deviceUid)
        } else {
            apiClient.fetchOwnedCompanies(accessToken, deviceUid)
        }

        companies.map { company ->
            RecoverableCompany(
                companyId = company.companyId,
                companyName = company.companyName,
                companyCode = company.companyCode,
                createdVia = company.createdVia,
                productCount = company.productCount,
                lastSyncedAt = company.lastSyncedAt
            )
        }
    }

    override suspend fun fetchCompanyCatalog(companyCode: String): Result<List<CloudCatalogProduct>> = runCatching {
        val accessToken = accountSessionRepository.getAccessToken()
            ?: error("Kayıtlı kullanıcı oturumu bulunamadı")
        apiClient.fetchCompanyCatalog(accessToken, companyCode).map { product ->
            CloudCatalogProduct(
                barcode = product.barcode,
                name = product.name,
                groupName = product.groupName,
                salePriceKurus = product.salePriceKurus,
                costPriceKurus = product.costPriceKurus,
                note = product.note,
                updatedAt = product.updatedAt
            )
        }
    }

    override suspend fun fetchCompanyCatalogChanges(
        companyCode: String,
        sinceUpdatedAt: Long,
        limit: Int
    ): Result<CloudCatalogChangePage> = runCatching {
        val accessToken = accountSessionRepository.getAccessToken()
            ?: error("Kayıtlı kullanıcı oturumu bulunamadı")

        val response = apiClient.fetchCompanyCatalogChanges(
            accessToken = accessToken,
            companyCode = companyCode,
            sinceUpdatedAt = sinceUpdatedAt,
            limit = limit
        )

        CloudCatalogChangePage(
            nextCursor = response.nextCursor,
            hasMore = response.hasMore,
            changes = response.changes.map { change ->
                CloudCatalogChange(
                    barcode = change.barcode,
                    name = change.name,
                    groupName = change.groupName,
                    salePriceKurus = change.salePriceKurus,
                    costPriceKurus = change.costPriceKurus,
                    note = change.note,
                    isActive = change.isActive,
                    updatedAt = change.updatedAt
                )
            }
        )
    }

    private fun buildState(values: Map<String, String>): CompanyActivationState {
        val token = values[SettingKeys.SYNC_ACTIVATION_TOKEN]
        return CompanyActivationState(
            isActivated = !token.isNullOrBlank(),
            companyId = values[SettingKeys.COMPANY_ID]?.toLongOrNull(),
            companyName = values[SettingKeys.COMPANY_NAME],
            companyCode = values[SettingKeys.COMPANY_CODE],
            deviceUid = values[SettingKeys.SYNC_DEVICE_UID],
            deviceName = values[SettingKeys.SYNC_DEVICE_NAME],
            activationToken = token,
            lastSyncSuccessAt = values[SettingKeys.SYNC_LAST_SUCCESS_AT]?.toLongOrNull(),
            lastSyncError = values[SettingKeys.SYNC_LAST_ERROR]
        )
    }
}


