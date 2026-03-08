package com.marketpos.domain.repository

import com.marketpos.domain.model.WebBarcodeSearchResult

interface WebBarcodeSearchRepository {
    suspend fun searchByBarcode(barcode: String): Result<List<WebBarcodeSearchResult>>
}
