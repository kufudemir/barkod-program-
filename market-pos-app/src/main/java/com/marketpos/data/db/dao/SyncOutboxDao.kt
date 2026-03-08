package com.marketpos.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marketpos.data.db.entity.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SyncOutboxEntity)

    @Query("SELECT * FROM sync_outbox WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC LIMIT :limit")
    suspend fun listPending(limit: Int): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status IN ('PENDING', 'FAILED')")
    suspend fun countPending(): Int

    @Query("DELETE FROM sync_outbox WHERE eventUuid IN (:eventUuids)")
    suspend fun deleteByEventUuids(eventUuids: List<String>)

    @Query("UPDATE sync_outbox SET status = 'FAILED', attemptCount = attemptCount + 1, lastAttemptAt = :attemptedAt, errorMessage = :message WHERE eventUuid IN (:eventUuids)")
    suspend fun markFailed(eventUuids: List<String>, attemptedAt: Long, message: String)

    @Query("DELETE FROM sync_outbox")
    suspend fun clearAll()
}
