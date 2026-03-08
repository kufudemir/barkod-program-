package com.marketpos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class StockFilter(val label: String) {
    ALL("Tüm"),
    OUT_OF_STOCK("Stoksuz"),
    CRITICAL("Kritik"),
    LOW("Düşük")
}

data class StockTrackingUiState(
    val selectedFilter: StockFilter = StockFilter.ALL,
    val products: List<Product> = emptyList()
)

sealed interface StockTrackingEvent {
    data class ShowMessage(val message: String) : StockTrackingEvent
}

@HiltViewModel
class StockTrackingViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<StockTrackingEvent>()
    val events: SharedFlow<StockTrackingEvent> = _events.asSharedFlow()
    private val selectedFilter = MutableStateFlow(StockFilter.ALL)

    val uiState: StateFlow<StockTrackingUiState> = combine(
        selectedFilter,
        productRepository.observeAllActive()
    ) { filter, products ->
        val filteredProducts = products
            .filter { product ->
                when (filter) {
                    StockFilter.ALL -> product.stockQty <= product.minStockQty * 2
                    StockFilter.OUT_OF_STOCK -> product.stockQty <= 0
                    StockFilter.CRITICAL -> product.stockQty <= product.minStockQty
                    StockFilter.LOW -> product.stockQty > product.minStockQty && product.stockQty <= product.minStockQty * 2
                }
            }
            .sortedWith(
                compareBy<Product> { it.stockQty > it.minStockQty }
                    .thenBy { it.stockQty }
                    .thenBy { it.name.lowercase() }
            )
        StockTrackingUiState(
            selectedFilter = filter,
            products = filteredProducts
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StockTrackingUiState()
        )

    fun selectFilter(filter: StockFilter) {
        selectedFilter.value = filter
    }

    fun updateStock(barcode: String, newStockInput: String) {
        val newStock = newStockInput.toIntOrNull()
        if (newStock == null || newStock < 0) {
            viewModelScope.launch {
                _events.emit(StockTrackingEvent.ShowMessage("Geçerli bir stok miktarı girin"))
            }
            return
        }

        viewModelScope.launch {
            productRepository.updateStock(barcode, newStock)
            _events.emit(StockTrackingEvent.ShowMessage("Stok güncellendi"))
        }
    }

    fun adjustStock(barcode: String, currentStock: Int, delta: Int) {
        val newStock = (currentStock + delta).coerceAtLeast(0)
        if (newStock == currentStock) {
            viewModelScope.launch {
                _events.emit(StockTrackingEvent.ShowMessage("Stok zaten 0"))
            }
            return
        }

        viewModelScope.launch {
            productRepository.updateStock(barcode, newStock)
            _events.emit(StockTrackingEvent.ShowMessage("Stok $newStock olarak güncellendi"))
        }
    }
}
