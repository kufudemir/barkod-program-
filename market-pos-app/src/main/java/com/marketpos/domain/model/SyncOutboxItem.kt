package com.marketpos.domain.model

enum class SyncOutboxStatus {
    PENDING,
    FAILED
}

data class SyncOutboxItem(
    val outboxId: Long = 0,
    val eventUuid: String,
    val eventType: String,
    val payloadJson: String,
    val createdAt: Long,
    val attemptCount: Int,
    val lastAttemptAt: Long? = null,
    val status: SyncOutboxStatus = SyncOutboxStatus.PENDING,
    val errorMessage: String? = null
)

data class SyncFlushResult(
    val processedCount: Int,
    val failedCount: Int,
    val pendingCount: Int
)
