package com.marketpos.data.repository

import com.marketpos.data.network.BarkodSpaceApiClient
import com.marketpos.data.network.SupportTicketDetailResponse
import com.marketpos.data.network.SupportTicketMessageResponse
import com.marketpos.data.network.SupportTicketSummaryResponse
import com.marketpos.domain.model.SupportTicketDetail
import com.marketpos.domain.model.SupportTicketMessage
import com.marketpos.domain.model.SupportTicketSummary
import com.marketpos.domain.repository.SupportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepositoryImpl @Inject constructor(
    private val apiClient: BarkodSpaceApiClient
) : SupportRepository {

    override suspend fun fetchInbox(accessToken: String, status: String?): Result<List<SupportTicketSummary>> = runCatching {
        apiClient.fetchSupportInbox(accessToken = accessToken, status = status).map { it.toDomain() }
    }

    override suspend fun fetchTicket(accessToken: String, ticketId: Long): Result<SupportTicketDetail> = runCatching {
        apiClient.fetchSupportTicket(accessToken = accessToken, ticketId = ticketId).toDomain()
    }

    override suspend fun createTicket(
        accessToken: String,
        type: String,
        title: String,
        description: String,
        source: String,
        companyCode: String?,
        deviceUid: String?,
        appVersion: String?
    ): Result<SupportTicketDetail> = runCatching {
        apiClient.createSupportTicket(
            accessToken = accessToken,
            type = type,
            title = title,
            description = description,
            source = source,
            companyCode = companyCode,
            deviceUid = deviceUid,
            appVersion = appVersion
        ).toDomain()
    }

    override suspend fun replyTicket(accessToken: String, ticketId: Long, message: String): Result<SupportTicketDetail> = runCatching {
        apiClient.replySupportTicket(accessToken = accessToken, ticketId = ticketId, message = message).toDomain()
    }

    override suspend fun reopenTicket(accessToken: String, ticketId: Long): Result<SupportTicketDetail> = runCatching {
        apiClient.reopenSupportTicket(accessToken = accessToken, ticketId = ticketId).toDomain()
    }

    private fun SupportTicketSummaryResponse.toDomain(): SupportTicketSummary {
        return SupportTicketSummary(
            ticketId = ticketId,
            type = type,
            source = source,
            status = status,
            title = title,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun SupportTicketDetailResponse.toDomain(): SupportTicketDetail {
        return SupportTicketDetail(
            ticketId = ticketId,
            type = type,
            source = source,
            status = status,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            messages = messages.map { it.toDomain() }
        )
    }

    private fun SupportTicketMessageResponse.toDomain(): SupportTicketMessage {
        return SupportTicketMessage(
            messageId = messageId,
            authorType = authorType,
            authorId = authorId,
            message = message,
            createdAt = createdAt
        )
    }
}

