package com.marketpos.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["eventUuid"], unique = true), Index("status"), Index("createdAt")]
)
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true) val outboxId: Long = 0,
    val eventUuid: String,
    val eventType: String,
    val payloadJson: String,
    val createdAt: Long,
    val attemptCount: Int,
    val lastAttemptAt: Long?,
    val status: String,
    val errorMessage: String?
)
