package com.marketpos.feature.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductSortOption(val label: String) {
    NAME("Ada Göre"),
    PRICE_DESC("Fiyata Göre"),
    STOCK_ASC("Stoğa Göre"),
    UPDATED_DESC("Son Güncellenen")
}

private data class ProductListFilterState(
    val query: String,
    val criticalOnly: Boolean,
    val outOfStockOnly: Boolean,
    val recentOnly: Boolean,
    val sortOption: ProductSortOption
)

data class ProductListUiState(
    val mode: AppMode = AppMode.CASHIER,
    val query: String = "",
    val criticalOnly: Boolean = false,
    val outOfStockOnly: Boolean = false,
    val recentOnly: Boolean = false,
    val sortOption: ProductSortOption = ProductSortOption.NAME,
    val products: List<Product> = emptyList()
)

sealed interface ProductListEvent {
    data class ShowMessage(val message: String) : ProductListEvent
}

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val mode = MutableStateFlow(AppMode.CASHIER)
    private val criticalOnly = MutableStateFlow(false)
    private val outOfStockOnly = MutableStateFlow(false)
    private val recentOnly = MutableStateFlow(false)
    private val sortOption = MutableStateFlow(ProductSortOption.NAME)
    private val _events = MutableSharedFlow<ProductListEvent>()
    val events: SharedFlow<ProductListEvent> = _events.asSharedFlow()

    private val filterState = combine(
        query,
        criticalOnly,
        outOfStockOnly,
        recentOnly,
        sortOption
    ) { queryValue, criticalOnlyValue, outOfStockOnlyValue, recentOnlyValue, sortOptionValue ->
        ProductListFilterState(
            query = queryValue,
            criticalOnly = criticalOnlyValue,
            outOfStockOnly = outOfStockOnlyValue,
            recentOnly = recentOnlyValue,
            sortOption = sortOptionValue
        )
    }

    val uiState: StateFlow<ProductListUiState> = combine(
        mode,
        filterState,
        productRepository.observeAllActive()
    ) { modeValue, filterStateValue, products ->
        val normalizedQuery = filterStateValue.query.trim()
        val filteredProducts = products
            .filter { product ->
                normalizedQuery.isBlank() ||
                    product.name.contains(normalizedQuery, ignoreCase = true) ||
                    product.barcode.contains(normalizedQuery) ||
                    product.groupName.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
            .filter { product ->
                !filterStateValue.criticalOnly || product.stockQty <= product.minStockQty
            }
            .filter { product ->
                !filterStateValue.outOfStockOnly || product.stockQty <= 0
            }
            .let { list ->
                when (filterStateValue.sortOption) {
                    ProductSortOption.NAME -> list.sortedBy { it.name.lowercase() }
                    ProductSortOption.PRICE_DESC -> list.sortedByDescending { it.salePriceKurus }
                    ProductSortOption.STOCK_ASC -> list.sortedBy { it.stockQty }
                    ProductSortOption.UPDATED_DESC -> list.sortedByDescending { it.updatedAt }
                }
            }
            .let { list ->
                if (filterStateValue.recentOnly) list.take(20) else list
            }
        ProductListUiState(
            mode = modeValue,
            query = filterStateValue.query,
            criticalOnly = filterStateValue.criticalOnly,
            outOfStockOnly = filterStateValue.outOfStockOnly,
            recentOnly = filterStateValue.recentOnly,
            sortOption = filterStateValue.sortOption,
            products = filteredProducts
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode.value = it }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun toggleCriticalOnly() {
        criticalOnly.value = !criticalOnly.value
    }

    fun toggleOutOfStockOnly() {
        outOfStockOnly.value = !outOfStockOnly.value
    }

    fun toggleRecentOnly() {
        recentOnly.value = !recentOnly.value
        if (recentOnly.value) {
            sortOption.value = ProductSortOption.UPDATED_DESC
        }
    }

    fun updateSortOption(option: ProductSortOption) {
        sortOption.value = option
    }

    fun updatePrice(barcode: String, priceTlInput: String, pinIfCashier: String?) {
        val priceKurus = MoneyUtils.parseTlInputToKurus(priceTlInput)
        if (priceKurus == null) {
            viewModelScope.launch { _events.emit(ProductListEvent.ShowMessage("Geçerli fiyat giriniz")) }
            return
        }
        viewModelScope.launch {
            if (uiState.value.mode == AppMode.CASHIER) {
                val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
                if (!valid) {
                    _events.emit(ProductListEvent.ShowMessage("PIN hatali"))
                    return@launch
                }
            }
            productRepository.updateSalePrice(barcode, MoneyUtils.roundUpToWholeTL(priceKurus))
            _events.emit(ProductListEvent.ShowMessage("Fiyat güncellendi"))
        }
    }

    fun updateStock(barcode: String, stockInput: String, pinIfCashier: String?) {
        val stockQty = stockInput.toIntOrNull()
        if (stockQty == null || stockQty < 0) {
            viewModelScope.launch { _events.emit(ProductListEvent.ShowMessage("Geçerli stok giriniz")) }
            return
        }
        viewModelScope.launch {
            if (uiState.value.mode == AppMode.CASHIER) {
                val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
                if (!valid) {
                    _events.emit(ProductListEvent.ShowMessage("PIN hatali"))
                    return@launch
                }
            }
            productRepository.updateStock(barcode, stockQty)
            _events.emit(ProductListEvent.ShowMessage("Stok güncellendi"))
        }
    }

    fun deleteProduct(barcode: String, pinIfCashier: String?) {
        viewModelScope.launch {
            if (uiState.value.mode == AppMode.CASHIER) {
                val valid = !pinIfCashier.isNullOrBlank() && settingsRepository.verifyPin(pinIfCashier)
                if (!valid) {
                    _events.emit(ProductListEvent.ShowMessage("PIN hatali"))
                    return@launch
                }
            }
            productRepository.deactivate(barcode)
            _events.emit(ProductListEvent.ShowMessage("Ürün silindi"))
        }
    }
}

