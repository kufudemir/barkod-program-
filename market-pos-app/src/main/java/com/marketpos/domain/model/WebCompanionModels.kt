package com.marketpos.domain.model

data class CompanionCartItem(
    val barcode: String,
    val productName: String,
    val quantity: Int,
    val baseSalePriceKurus: Long,
    val salePriceKurus: Long,
    val lineTotalKurus: Long,
    val hasCustomPrice: Boolean
)

data class CompanionSaleSummary(
    val itemCount: Int,
    val totalAmountKurus: Long,
    val canCheckout: Boolean
)

data class CompanionSaleReceipt(
    val saleId: Long,
    val totalAmountKurus: Long,
    val totalItems: Int,
    val completedAtEpochMs: Long?
)

data class CompanionRecentSale(
    val saleId: Long,
    val totalAmountKurus: Long,
    val totalItems: Int,
    val completedAtEpochMs: Long?,
    val completedAtLabel: String?,
    val registerName: String?,
    val paymentMethod: String?
)

data class ActiveWebPosSessionState(
    val hasActiveSession: Boolean,
    val companyCode: String? = null,
    val companyName: String? = null,
    val branchName: String? = null,
    val registerName: String? = null,
    val posSessionId: Long? = null,
    val saleSessionId: Long? = null,
    val saleSessionLabel: String? = null,
    val summary: CompanionSaleSummary = CompanionSaleSummary(
        itemCount = 0,
        totalAmountKurus = 0L,
        canCheckout = false
    ),
    val cartItems: List<CompanionCartItem> = emptyList(),
    val lastSale: CompanionSaleReceipt? = null,
    val recentSales: List<CompanionRecentSale> = emptyList(),
    val message: String? = null
)

data class CompanionPrintPayload(
    val printReady: Boolean,
    val message: String,
    val printUrl: String? = null,
    val previewUrl: String? = null,
    val pdfUrl: String? = null,
    val saleId: Long? = null
)

data class MobilePosSaleSyncItem(
    val barcode: String,
    val productName: String,
    val quantity: Int,
    val unitSalePriceKurus: Long,
    val unitCostPriceKurus: Long,
    val lineTotalKurus: Long,
    val lineProfitKurus: Long
)

data class MobilePosSaleSyncPayload(
    val localSaleId: Long,
    val createdAt: Long,
    val totalItems: Int,
    val totalAmountKurus: Long,
    val totalCostKurus: Long,
    val profitKurus: Long,
    val paymentMethod: String = "cash",
    val items: List<MobilePosSaleSyncItem>
)
