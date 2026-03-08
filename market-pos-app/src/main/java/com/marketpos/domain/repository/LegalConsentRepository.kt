package com.marketpos.domain.repository

import com.marketpos.domain.model.LegalConsentState
import kotlinx.coroutines.flow.Flow

interface LegalConsentRepository {
    fun observeState(): Flow<LegalConsentState>
    suspend fun getState(): LegalConsentState
    suspend fun acceptCurrentVersion(acceptedAt: Long = System.currentTimeMillis()): Result<LegalConsentState>
}
