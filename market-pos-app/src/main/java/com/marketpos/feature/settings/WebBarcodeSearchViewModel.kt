package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.WebBarcodeSearchResult
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.usecase.SearchBarcodeOnWebUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WebBarcodeSearchResultUi(
    val barcode: String,
    val name: String,
    val salePriceKurus: Long?,
    val priceLabel: String?,
    val sourceLabel: String,
    val sourceUrl: String?,
    val isExisting: Boolean
)

data class WebBarcodeSearchUiState(
    val barcodeInput: String = "",
    val isLoading: Boolean = false,
    val hideExisting: Boolean = false,
    val results: List<WebBarcodeSearchResultUi> = emptyList(),
    val error: String? = null
) {
    val visibleResults: List<WebBarcodeSearchResultUi>
        get() = if (hideExisting) results.filterNot { it.isExisting } else results

    val existingCount: Int
        get() = results.count { it.isExisting }
}

sealed interface WebBarcodeSearchEvent {
    data class ShowMessage(val message: String) : WebBarcodeSearchEvent
}

@HiltViewModel
class WebBarcodeSearchViewModel @Inject constructor(
    private val searchBarcodeOnWebUseCase: SearchBarcodeOnWebUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebBarcodeSearchUiState())
    val uiState: StateFlow<WebBarcodeSearchUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WebBarcodeSearchEvent>()
    val events: SharedFlow<WebBarcodeSearchEvent> = _events.asSharedFlow()

    fun updateBarcodeInput(value: String) {
        _uiState.value = _uiState.value.copy(
            barcodeInput = value.filter(Char::isDigit).take(32),
            error = null
        )
    }

    fun toggleHideExisting() {
        _uiState.value = _uiState.value.copy(hideExisting = !_uiState.value.hideExisting)
    }

    fun onBarcodeScanned(barcode: String) {
        updateBarcodeInput(barcode)
        search()
    }

    fun search() {
        val barcode = _uiState.value.barcodeInput
        if (barcode.isBlank()) {
            viewModelScope.launch {
                _events.emit(WebBarcodeSearchEvent.ShowMessage("Önce barkod numarası girin"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, results = emptyList())
            searchBarcodeOnWebUseCase(barcode)
                .onSuccess { results ->
                    val existingBarcodes = productRepository.listByBarcodes(results.map { it.barcode })
                        .map { it.barcode }
                        .toSet()
                    val mapped = results.map { it.toUi(existingBarcodes.contains(it.barcode)) }
                    _uiState.value = _uiState.value.copy(isLoading = false, results = mapped)
                    if (mapped.isEmpty()) {
                        _events.emit(WebBarcodeSearchEvent.ShowMessage("Sonuç bulunamadı"))
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Web arama başarısız"
                    )
                }
        }
    }

    private fun WebBarcodeSearchResult.toUi(isExisting: Boolean): WebBarcodeSearchResultUi {
        return WebBarcodeSearchResultUi(
            barcode = barcode,
            name = name,
            salePriceKurus = salePriceKurus,
            priceLabel = salePriceKurus?.let(MoneyUtils::formatKurus),
            sourceLabel = sourceLabel,
            sourceUrl = sourceUrl,
            isExisting = isExisting
        )
    }
}
