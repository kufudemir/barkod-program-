package com.marketpos.domain.model

data class BarcodeBankasiGroup(
    val value: String,
    val label: String
)

data class BarcodeBankasiImportItem(
    val barcode: String,
    val name: String,
    val salePriceKurus: Long,
    val sourcePage: Int,
    val lastChangedAt: String?
)

data class BarcodeBankasiPreviewResult(
    val query: String,
    val startPage: Int,
    val requestedItemCount: Int,
    val fetchedPages: Int,
    val totalAvailable: Int?,
    val items: List<BarcodeBankasiImportItem>,
    val skippedItems: Int
)

data class BarcodeBankasiImportSummary(
    val importedCount: Int,
    val updatedCount: Int,
    val createdCount: Int
)
