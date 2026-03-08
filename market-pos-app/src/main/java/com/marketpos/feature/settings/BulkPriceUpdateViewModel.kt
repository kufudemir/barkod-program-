package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.Product
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.usecase.ApplyPercentPriceChangeUseCase
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

data class BulkPricePreviewItem(
    val barcode: String,
    val name: String,
    val oldPriceKurus: Long,
    val newPriceKurus: Long
)

data class BulkPriceUpdateUiState(
    val mode: AppMode = AppMode.CASHIER,
    val query: String = "",
    val products: List<Product> = emptyList(),
    val selectedBarcodes: Set<String> = emptySet(),
    val applyToAll: Boolean = true,
    val percentText: String = "10",
    val preview: List<BulkPricePreviewItem> = emptyList()
)

sealed interface BulkPriceUpdateEvent {
    data class ShowMessage(val message: String) : BulkPriceUpdateEvent
}

@HiltViewModel
class BulkPriceUpdateViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val applyPercentPriceChangeUseCase: ApplyPercentPriceChangeUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<Set<String>>(emptySet())
    private val applyToAll = MutableStateFlow(true)
    private val percentText = MutableStateFlow("10")
    private val mode = MutableStateFlow(AppMode.CASHIER)

    private val _events = MutableSharedFlow<BulkPriceUpdateEvent>()
    val events: SharedFlow<BulkPriceUpdateEvent> = _events.asSharedFlow()

    private val filteredProductsFlow = combine(
        productRepository.observeAllActive(),
        query
    ) { allProducts, queryValue ->
        if (queryValue.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(queryValue, ignoreCase = true) || it.barcode.contains(queryValue)
            }
        }
    }

    private val baseStateFlow = combine(
        filteredProductsFlow,
        selected,
        applyToAll,
        percentText,
        mode
    ) { filtered, selectedValue, applyToAllValue, percentValue, modeValue ->
        val percent = percentValue.replace(",", ".").toDoubleOrNull() ?: 0.0
        val targets = if (applyToAllValue) filtered else filtered.filter { selectedValue.contains(it.barcode) }
        val preview = if (percent > 0) {
            targets.map { product ->
                BulkPricePreviewItem(
                    barcode = product.barcode,
                    name = product.name,
                    oldPriceKurus = product.salePriceKurus,
                    newPriceKurus = applyPercentPriceChangeUseCase(product.salePriceKurus, percent)
                )
            }
        } else {
            emptyList()
        }
        BulkPriceUpdateUiState(
            mode = modeValue,
            query = "",
            products = filtered,
            selectedBarcodes = selectedValue,
            applyToAll = applyToAllValue,
            percentText = percentValue,
            preview = preview
        )
    }

    val uiState: StateFlow<BulkPriceUpdateUiState> = combine(
        baseStateFlow,
        query
    ) { baseState, queryValue ->
        baseState.copy(query = queryValue)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = BulkPriceUpdateUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode.value = it }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun updatePercent(value: String) {
        percentText.value = value
    }

    fun setApplyToAll(value: Boolean) {
        applyToAll.value = value
    }

    fun toggleSelection(barcode: String) {
        val current = selected.value.toMutableSet()
        if (!current.add(barcode)) current.remove(barcode)
        selected.value = current
    }

    fun apply(pinIfCashier: String?) {
        viewModelScope.launch {
            val state = uiState.value
            val percent = state.percentText.replace(",", ".").toDoubleOrNull()
            if (percent == null || percent <= 0.0) {
                _events.emit(BulkPriceUpdateEvent.ShowMessage("Geçerli bir yüzde giriniz"))
                return@launch
            }

            if (state.mode == AppMode.CASHIER) {
                if (pinIfCashier.isNullOrBlank() || !settingsRepository.verifyPin(pinIfCashier)) {
                    _events.emit(BulkPriceUpdateEvent.ShowMessage("PIN hatalı"))
                    return@launch
                }
            }

            val targets = if (state.applyToAll) {
                state.preview
            } else {
                state.preview.filter { state.selectedBarcodes.contains(it.barcode) }
            }
            if (targets.isEmpty()) {
                _events.emit(BulkPriceUpdateEvent.ShowMessage("Uygulanacak ürün seçilmedi"))
                return@launch
            }

            targets.forEach {
                productRepository.updateSalePrice(it.barcode, MoneyUtils.roundUpToWholeTL(it.newPriceKurus))
            }
            _events.emit(BulkPriceUpdateEvent.ShowMessage("Toplu fiyat güncelleme tamamlandı"))
        }
    }
}
