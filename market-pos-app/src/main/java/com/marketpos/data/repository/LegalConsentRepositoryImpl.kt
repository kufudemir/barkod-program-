package com.marketpos.data.repository

import com.marketpos.core.legal.LegalContent
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.domain.model.LegalConsentState
import com.marketpos.domain.repository.LegalConsentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LegalConsentRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao
) : LegalConsentRepository {

    override fun observeState(): Flow<LegalConsentState> {
        return appSettingDao.observeAll().map { entities ->
            val values = entities.associateBy { it.key }.mapValues { it.value.value }
            buildState(values)
        }
    }

    override suspend fun getState(): LegalConsentState {
        val values = appSettingDao.getMany(
            listOf(
                SettingKeys.LEGAL_CONSENT_VERSION,
                SettingKeys.LEGAL_CONSENT_ACCEPTED_AT
            )
        ).associateBy { it.key }.mapValues { it.value.value }

        return buildState(values)
    }

    override suspend fun acceptCurrentVersion(acceptedAt: Long): Result<LegalConsentState> = runCatching {
        appSettingDao.set(AppSettingEntity(SettingKeys.LEGAL_CONSENT_VERSION, LegalContent.CURRENT_VERSION))
        appSettingDao.set(AppSettingEntity(SettingKeys.LEGAL_CONSENT_ACCEPTED_AT, acceptedAt.toString()))
        getState()
    }

    private fun buildState(values: Map<String, String>): LegalConsentState {
        return LegalConsentState(
            currentVersion = LegalContent.CURRENT_VERSION,
            acceptedVersion = values[SettingKeys.LEGAL_CONSENT_VERSION],
            acceptedAt = values[SettingKeys.LEGAL_CONSENT_ACCEPTED_AT]?.toLongOrNull()
        )
    }
}
