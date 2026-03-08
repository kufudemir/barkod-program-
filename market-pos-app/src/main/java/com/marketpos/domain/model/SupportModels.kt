package com.marketpos.domain.model

enum class SupportTicketType(val key: String, val label: String) {
    BUG("bug", "Hata Bildirimi"),
    FEATURE_REQUEST("feature_request", "Ozellik Istegi"),
    GENERAL("general", "Genel");

    companion object {
        fun fromKey(value: String): SupportTicketType {
            return entries.firstOrNull { it.key == value } ?: GENERAL
        }
    }
}

data class SupportTicketSummary(
    val ticketId: Long,
    val type: String,
    val source: String,
    val status: String,
    val title: String,
    val lastMessage: String?,
    val lastMessageAt: Long?,
    val createdAt: Long?,
    val updatedAt: Long?
)

data class SupportTicketMessage(
    val messageId: Long,
    val authorType: String,
    val authorId: Long?,
    val message: String,
    val createdAt: Long?
)

data class SupportTicketDetail(
    val ticketId: Long,
    val type: String,
    val source: String,
    val status: String,
    val title: String,
    val description: String,
    val createdAt: Long?,
    val updatedAt: Long?,
    val messages: List<SupportTicketMessage>
)

