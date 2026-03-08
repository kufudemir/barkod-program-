package com.marketpos.data.repository

import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.dao.SyncOutboxDao
import com.marketpos.data.db.entity.SyncOutboxEntity
import com.marketpos.domain.model.SyncOutboxItem
import com.marketpos.domain.model.SyncOutboxStatus
import com.marketpos.domain.repository.SyncOutboxRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SyncOutboxRepositoryImpl @Inject constructor(
    private val syncOutboxDao: SyncOutboxDao
) : SyncOutboxRepository {

    override fun observePendingCount(): Flow<Int> = syncOutboxDao.observePendingCount()

    override suspend fun enqueue(eventType: String, payloadJson: String): Result<Unit> = runCatching {
        syncOutboxDao.insert(
            SyncOutboxEntity(
                eventUuid = UUID.randomUUID().toString(),
                eventType = eventType,
                payloadJson = payloadJson,
                createdAt = DateUtils.now(),
                attemptCount = 0,
                lastAttemptAt = null,
                status = SyncOutboxStatus.PENDING.name,
                errorMessage = null
            )
        )
    }

    override suspend fun listPending(limit: Int): List<SyncOutboxItem> {
        return syncOutboxDao.listPending(limit).map {
            SyncOutboxItem(
                outboxId = it.outboxId,
                eventUuid = it.eventUuid,
                eventType = it.eventType,
                payloadJson = it.payloadJson,
                createdAt = it.createdAt,
                attemptCount = it.attemptCount,
                lastAttemptAt = it.lastAttemptAt,
                status = runCatching { SyncOutboxStatus.valueOf(it.status) }.getOrDefault(SyncOutboxStatus.PENDING),
                errorMessage = it.errorMessage
            )
        }
    }

    override suspend fun deleteByEventUuids(eventUuids: List<String>) {
        if (eventUuids.isNotEmpty()) {
            syncOutboxDao.deleteByEventUuids(eventUuids)
        }
    }

    override suspend fun markFailed(eventUuids: List<String>, message: String) {
        if (eventUuids.isNotEmpty()) {
            syncOutboxDao.markFailed(eventUuids, DateUtils.now(), message)
        }
    }

    override suspend fun countPending(): Int = syncOutboxDao.countPending()

    override suspend fun clearAll() {
        syncOutboxDao.clearAll()
    }
}
