package com.marketpos.domain.repository

import com.marketpos.domain.model.SupportTicketDetail
import com.marketpos.domain.model.SupportTicketSummary

interface SupportRepository {
    suspend fun fetchInbox(accessToken: String, status: String? = null): Result<List<SupportTicketSummary>>

    suspend fun fetchTicket(accessToken: String, ticketId: Long): Result<SupportTicketDetail>

    suspend fun createTicket(
        accessToken: String,
        type: String,
        title: String,
        description: String,
        source: String = "mobile",
        companyCode: String? = null,
        deviceUid: String? = null,
        appVersion: String? = null
    ): Result<SupportTicketDetail>

    suspend fun replyTicket(
        accessToken: String,
        ticketId: Long,
        message: String
    ): Result<SupportTicketDetail>

    suspend fun reopenTicket(accessToken: String, ticketId: Long): Result<SupportTicketDetail>
}

