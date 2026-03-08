package com.marketpos.domain.model

data class WebBarcodeSearchResult(
    val barcode: String,
    val name: String,
    val salePriceKurus: Long?,
    val sourceLabel: String,
    val sourceUrl: String?
)
