package com.marketpos.domain.repository

import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.PremiumState
import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    fun observeState(): Flow<PremiumState>
    suspend fun getState(): PremiumState
    suspend fun hasAccess(feature: PremiumFeature): Boolean
    suspend fun startTrial(days: Int): Result<PremiumState>
    suspend fun activateWithLicenseCode(licenseCode: String): Result<PremiumState>
    suspend fun clearLicense(): Result<Unit>
}
