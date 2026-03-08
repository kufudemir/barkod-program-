package com.marketpos.domain.usecase

import com.marketpos.domain.model.SyncFlushResult
import com.marketpos.domain.repository.CatalogSyncRepository
import javax.inject.Inject

class FlushSyncQueueUseCase @Inject constructor(
    private val catalogSyncRepository: CatalogSyncRepository
) {
    suspend operator fun invoke(): Result<SyncFlushResult> = catalogSyncRepository.flushPending()
}
