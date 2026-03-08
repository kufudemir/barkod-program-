package com.marketpos.domain.repository

import com.marketpos.domain.model.CompanyActivationState
import com.marketpos.domain.model.CloudCatalogProduct
import com.marketpos.domain.model.CloudCatalogChangePage
import com.marketpos.domain.model.RecoverableCompany
import kotlinx.coroutines.flow.Flow

interface ActivationRepository {
    fun observeActivationState(): Flow<CompanyActivationState>
    suspend fun getActivationState(): CompanyActivationState
    suspend fun activate(companyName: String): Result<CompanyActivationState>
    suspend fun activateAsNewCompany(companyName: String): Result<CompanyActivationState>
    suspend fun activateWithCompanyCode(companyCode: String): Result<CompanyActivationState>
    suspend fun clearActivation(): Result<Unit>
    suspend fun updateSyncStatus(lastSuccessAt: Long? = null, lastError: String? = null)
    suspend fun getDeviceUid(): String
    suspend fun getDeviceName(): String
    suspend fun listOwnedCompanies(): Result<List<RecoverableCompany>>
    suspend fun fetchCompanyCatalog(companyCode: String): Result<List<CloudCatalogProduct>>
    suspend fun fetchCompanyCatalogChanges(
        companyCode: String,
        sinceUpdatedAt: Long,
        limit: Int = 200
    ): Result<CloudCatalogChangePage>
}
