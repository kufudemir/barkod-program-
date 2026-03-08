package com.marketpos.domain.usecase

import com.marketpos.domain.model.WebBarcodeSearchResult
import com.marketpos.domain.repository.WebBarcodeSearchRepository
import javax.inject.Inject

class SearchBarcodeOnWebUseCase @Inject constructor(
    private val repository: WebBarcodeSearchRepository
) {
    suspend operator fun invoke(barcode: String): Result<List<WebBarcodeSearchResult>> {
        return repository.searchByBarcode(barcode)
    }
}
