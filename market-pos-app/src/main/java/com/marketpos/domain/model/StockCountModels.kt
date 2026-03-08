package com.marketpos.domain.model

data class StockCountItem(
    val barcode: String,
    val name: String,
    val expectedQty: Int,
    val countedQty: Int,
    val differenceQty: Int
)

data class StockCountSession(
    val startedAt: Long? = null,
    val items: List<StockCountItem> = emptyList()
) {
    val uniqueProductCount: Int = items.size
    val totalCountedUnits: Int = items.sumOf { it.countedQty }
    val differenceProductCount: Int = items.count { it.differenceQty != 0 }
}

data class StockCountScanResult(
    val item: StockCountItem,
    val newCount: Int
)
