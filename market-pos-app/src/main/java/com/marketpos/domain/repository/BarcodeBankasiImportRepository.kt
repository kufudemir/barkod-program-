package com.marketpos.domain.repository

import com.marketpos.domain.model.BarcodeBankasiGroup
import com.marketpos.domain.model.BarcodeBankasiPreviewResult

interface BarcodeBankasiImportRepository {
    suspend fun fetchGroups(): Result<List<BarcodeBankasiGroup>>
    suspend fun fetchPreview(
        query: String?,
        group: String?,
        startPage: Int,
        requestedItemCount: Int
    ): Result<BarcodeBankasiPreviewResult>
}
