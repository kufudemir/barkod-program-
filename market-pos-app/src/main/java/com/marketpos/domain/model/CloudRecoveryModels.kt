package com.marketpos.domain.model

data class RecoverableCompany(
    val companyId: Long,
    val companyName: String,
    val companyCode: String,
    val createdVia: String?,
    val productCount: Int,
    val lastSyncedAt: Long?
)

data class CloudCatalogProduct(
    val barcode: String,
    val name: String,
    val groupName: String?,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val note: String?,
    val updatedAt: Long
)

data class CloudCatalogChange(
    val barcode: String,
    val name: String,
    val groupName: String?,
    val salePriceKurus: Long,
    val costPriceKurus: Long,
    val note: String?,
    val isActive: Boolean,
    val updatedAt: Long
)

data class CloudCatalogChangePage(
    val nextCursor: Long,
    val hasMore: Boolean,
    val changes: List<CloudCatalogChange>
)
