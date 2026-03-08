package com.marketpos.domain.repository

import com.marketpos.domain.model.AccountSessionState
import kotlinx.coroutines.flow.Flow

interface AccountSessionRepository {
    fun observeState(): Flow<AccountSessionState>
    suspend fun getState(): AccountSessionState
    suspend fun continueAsGuest(): Result<AccountSessionState>
    suspend fun register(name: String, email: String, password: String): Result<AccountSessionState>
    suspend fun login(email: String, password: String): Result<AccountSessionState>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun requestPasswordReset(email: String): Result<Long?>
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun clearLocalSession()
    suspend fun getAccessToken(): String?
}
