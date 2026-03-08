package com.marketpos.domain.repository

import com.marketpos.domain.model.AppUpdateInfo

interface AppUpdateRepository {
    suspend fun getLatestAvailableUpdate(currentVersionCode: Int): AppUpdateInfo?
}
