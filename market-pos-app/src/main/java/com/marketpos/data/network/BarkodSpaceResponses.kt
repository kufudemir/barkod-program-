package com.marketpos.data.network

data class DeviceActivationResponse(
    val companyId: Long,
    val companyName: String,
    val companyCode: String,
    val deviceId: Long,
    val activationToken: String,
    val activatedAt: Long
)

data class MobileUserResponse(
    val id: Long,
    val name: String,
    val email: String
)

data class AuthSessionResponse(
    val user: MobileUserResponse,
    val accessToken: String
)

data class AccountPremiumResponse(
    val tier: String,
    val source: String,
    val activatedAt: Long?,
    val expiresAt: Long?,
    val licenseCodeMasked: String?
)

data class PasswordResetRequestResponse(
    val message: String,
    val expiresAt: Long?
)

data class AppUpdateResponse(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String?,
    val forceUpdate: Boolean
)

data class RecoverableCompanyResponse(
    val companyId: Long,
    val companyName: String,
    val companyCode: String,
    val createdVia: String?,
    val productCount: Int,
    val lastSyncedAt: Long?
)

data class CloudCatalogProductResponse(
    val barcode: String,
    val name: String,
    val groupName: String?,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val note: String?,
    val updatedAt: Long
)

data class CloudCatalogChangeResponse(
    val barcode: String,
    val name: String,
    val groupName: String?,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val note: String?,
    val isActive: Boolean,
    val updatedAt: Long
)

data class CloudCatalogChangePageResponse(
    val nextCursor: Long,
    val hasMore: Boolean,
    val changes: List<CloudCatalogChangeResponse>
)

data class GlobalCatalogSuggestionResponse(
    val barcode: String,
    val name: String,
    val groupName: String?,
    val updatedAt: Long?
)

data class SyncEventResult(
    val eventUuid: String,
    val status: String,
    val message: String? = null
)

data class CatalogSyncResponse(
    val accepted: Int,
    val rejected: Int,
    val results: List<SyncEventResult>,
    val serverTime: Long
)

data class CompanionCartItemResponse(
    val barcode: String,
    val productName: String,
    val quantity: Int,
    val baseSalePriceKurus: Long,
    val salePriceKurus: Long,
    val lineTotalKurus: Long,
    val hasCustomPrice: Boolean
)

data class CompanionSaleSummaryResponse(
    val itemCount: Int,
    val totalAmountKurus: Long,
    val canCheckout: Boolean
)

data class CompanionSaleReceiptResponse(
    val saleId: Long,
    val totalAmountKurus: Long,
    val totalItems: Int,
    val completedAtEpochMs: Long?
)

data class CompanionRecentSaleResponse(
    val saleId: Long,
    val totalAmountKurus: Long,
    val totalItems: Int,
    val completedAtEpochMs: Long?,
    val completedAtLabel: String?,
    val registerName: String?,
    val paymentMethod: String?
)

data class ActiveWebSaleSessionResponse(
    val hasActiveSession: Boolean,
    val companyCode: String?,
    val companyName: String?,
    val branchName: String?,
    val registerName: String?,
    val posSessionId: Long?,
    val saleSessionId: Long?,
    val saleSessionLabel: String?,
    val summary: CompanionSaleSummaryResponse,
    val cartItems: List<CompanionCartItemResponse>,
    val lastSale: CompanionSaleReceiptResponse?,
    val recentSales: List<CompanionRecentSaleResponse> = emptyList(),
    val message: String?
)

data class WebSalePrintResponse(
    val printReady: Boolean,
    val message: String,
    val printUrl: String?,
    val previewUrl: String?,
    val pdfUrl: String?,
    val saleId: Long?
)

data class SupportTicketSummaryResponse(
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

data class SupportTicketMessageResponse(
    val messageId: Long,
    val authorType: String,
    val authorId: Long?,
    val message: String,
    val createdAt: Long?
)

data class SupportTicketDetailResponse(
    val ticketId: Long,
    val type: String,
    val source: String,
    val status: String,
    val title: String,
    val description: String,
    val createdAt: Long?,
    val updatedAt: Long?,
    val messages: List<SupportTicketMessageResponse>
)
