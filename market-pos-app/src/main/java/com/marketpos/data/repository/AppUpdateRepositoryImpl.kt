package com.marketpos.data.repository

import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.domain.model.AppUpdateInfo
import com.marketpos.domain.repository.AppUpdateRepository
import javax.inject.Inject

class AppUpdateRepositoryImpl @Inject constructor(
    private val barkodSpaceApiClient: BarkodSpaceApiClient
) : AppUpdateRepository {

    override suspend fun getLatestAvailableUpdate(currentVersionCode: Int): AppUpdateInfo? {
        val latest = runCatching { barkodSpaceApiClient.fetchLatestAppUpdate() }
            .getOrNull()
            ?: return null

        return latest.takeIf { it.versionCode > currentVersionCode }
    }
}
