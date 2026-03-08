package com.marketpos.domain.repository

import com.marketpos.domain.model.SyncOutboxItem
import kotlinx.coroutines.flow.Flow

interface SyncOutboxRepository {
    fun observePendingCount(): Flow<Int>
    suspend fun enqueue(eventType: String, payloadJson: String): Result<Unit>
    suspend fun listPending(limit: Int = 50): List<SyncOutboxItem>
    suspend fun deleteByEventUuids(eventUuids: List<String>)
    suspend fun markFailed(eventUuids: List<String>, message: String)
    suspend fun countPending(): Int
    suspend fun clearAll()
}
