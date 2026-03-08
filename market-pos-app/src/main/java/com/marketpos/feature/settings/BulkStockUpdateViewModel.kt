package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.usecase.ApplyBulkStockChangeUseCase
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

private data class BulkStockConfigState(
    val selectedBarcodes: Set<String>,
    val applyToAll: Boolean,
    val deltaText: String,
    val isIncrease: Boolean,
    val mode: AppMode
)

data class BulkStockPreviewItem(
    val barcode: String,
    val name: String,
    val oldStockQty: Int,
    val newStockQty: Int
)

data class BulkStockUpdateUiState(
    val mode: AppMode = AppMode.CASHIER,
    val query: String = "",
    val products: List<Product> = emptyList(),
    val selectedBarcodes: Set<String> = emptySet(),
    val applyToAll: Boolean = true,
    val deltaText: String = "5",
    val isIncrease: Boolean = true,
    val preview: List<BulkStockPreviewItem> = emptyList()
)

sealed interface BulkStockUpdateEvent {
    data class ShowMessage(val message: String) : BulkStockUpdateEvent
}

@HiltViewModel
class BulkStockUpdateViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val applyBulkStockChangeUseCase: ApplyBulkStockChangeUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<Set<String>>(emptySet())
    private val applyToAll = MutableStateFlow(true)
    private val deltaText = MutableStateFlow("5")
    private val isIncrease = MutableStateFlow(true)
    private val mode = MutableStateFlow(AppMode.CASHIER)

    private val _events = MutableSharedFlow<BulkStockUpdateEvent>()
    val events: SharedFlow<BulkStockUpdateEvent> = _events.asSharedFlow()

    private val filteredProductsFlow = combine(
        productRepository.observeAllActive(),
        query
    ) { allProducts, queryValue ->
        if (queryValue.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(queryValue, ignoreCase = true) ||
                    it.barcode.contains(queryValue) ||
                    it.groupName.orEmpty().contains(queryValue, ignoreCase = true)
            }
        }
    }

    private val configState = combine(
        selected,
        applyToAll,
        deltaText,
        isIncrease,
        mode
    ) { selectedValue, applyToAllValue, deltaValue, increaseValue, modeValue ->
        BulkStockConfigState(
            selectedBarcodes = selectedValue,
            applyToAll = applyToAllValue,
            deltaText = deltaValue,
            isIncrease = increaseValue,
            mode = modeValue
        )
    }

    val uiState: StateFlow<BulkStockUpdateUiState> = combine(
        filteredProductsFlow,
        configState,
        query
    ) { filtered, configValue, queryValue ->
        val delta = configValue.deltaText.toIntOrNull() ?: 0
        val signedDelta = if (configValue.isIncrease) delta else -delta
        val targets = if (configValue.applyToAll) filtered else filtered.filter { configValue.selectedBarcodes.contains(it.barcode) }
        val preview = if (delta > 0) {
            targets.map { product ->
                BulkStockPreviewItem(
                    barcode = product.barcode,
                    name = product.name,
                    oldStockQty = product.stockQty,
                    newStockQty = (product.stockQty + signedDelta).coerceAtLeast(0)
                )
            }
        } else {
            emptyList()
        }

        BulkStockUpdateUiState(
            mode = configValue.mode,
            query = queryValue,
            products = filtered,
            selectedBarcodes = configValue.selectedBarcodes,
            applyToAll = configValue.applyToAll,
            deltaText = configValue.deltaText,
            isIncrease = configValue.isIncrease,
            preview = preview
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = BulkStockUpdateUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode.value = it }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun updateDelta(value: String) {
        deltaText.value = value.filter(Char::isDigit)
    }

    fun setApplyToAll(value: Boolean) {
        applyToAll.value = value
    }

    fun setIncrease(value: Boolean) {
        isIncrease.value = value
    }

    fun toggleSelection(barcode: String) {
        val current = selected.value.toMutableSet()
        if (!current.add(barcode)) current.remove(barcode)
        selected.value = current
    }

    fun apply(pinIfCashier: String?) {
        viewModelScope.launch {
            val state = uiState.value
            val delta = state.deltaText.toIntOrNull()
            if (delta == null || delta <= 0) {
                _events.emit(BulkStockUpdateEvent.ShowMessage("Geçerli bir stok değişimi giriniz"))
                return@launch
            }

            if (state.mode == AppMode.CASHIER) {
                if (pinIfCashier.isNullOrBlank() || !settingsRepository.verifyPin(pinIfCashier)) {
                    _events.emit(BulkStockUpdateEvent.ShowMessage("PIN hatalı"))
                    return@launch
                }
            }

            val targets = if (state.applyToAll) {
                state.preview
            } else {
                state.preview.filter { state.selectedBarcodes.contains(it.barcode) }
            }
            if (targets.isEmpty()) {
                _events.emit(BulkStockUpdateEvent.ShowMessage("Uygulanacak ürün seçilmedi"))
                return@launch
            }

            applyBulkStockChangeUseCase(
                targets.map { it.barcode to it.newStockQty }
            ).onSuccess {
                _events.emit(BulkStockUpdateEvent.ShowMessage("Toplu stok güncelleme tamamlandı"))
            }.onFailure { error ->
                _events.emit(BulkStockUpdateEvent.ShowMessage(error.message ?: "Toplu stok güncelleme başarısız"))
            }
        }
    }
}
