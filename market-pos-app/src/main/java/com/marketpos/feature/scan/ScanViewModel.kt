package com.marketpos.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.cart.CartManager
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.repository.PremiumRepository
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
import kotlinx.coroutines.launch

data class ScanUiState(
    val mode: AppMode = AppMode.CASHIER,
    val cameraEnabled: Boolean = true,
    val cartItemCount: Int = 0,
    val cartTotalLabel: String = MoneyUtils.formatKurus(0L),
    val isPro: Boolean = false,
    val criticalStockCount: Int = 0,
    val scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM
)

sealed interface ScanEvent {
    data object PlayFeedback : ScanEvent
    data class NavigateDetail(val barcode: String) : ScanEvent
    data class NavigateNotFound(val barcode: String) : ScanEvent
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val cartManager: CartManager,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>()
    val events: SharedFlow<ScanEvent> = _events.asSharedFlow()

    private var processing = false
    private var lastScannedBarcode: String = ""
    private var lastScanTime: Long = 0L

    init {
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode ->
                val enabled = settingsRepository.getCameraEnabled(mode)
                _uiState.value = _uiState.value.copy(
                    mode = mode,
                    cameraEnabled = enabled
                )
            }
        }
        viewModelScope.launch {
            cartManager.items.collect { items ->
                _uiState.value = _uiState.value.copy(
                    cartItemCount = items.sumOf { it.quantity },
                    cartTotalLabel = MoneyUtils.formatKurus(items.sumOf { it.lineTotalKurus })
                )
            }
        }
        viewModelScope.launch {
            premiumRepository.observeState().collect { premiumState ->
                _uiState.value = _uiState.value.copy(isPro = premiumState.isPro)
            }
        }
        viewModelScope.launch {
            productRepository.observeAllActive().collect { products ->
                _uiState.value = _uiState.value.copy(
                    criticalStockCount = products.count { it.stockQty <= it.minStockQty }
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.observeScanBoxSize().collect { option ->
                _uiState.value = _uiState.value.copy(scanBoxSize = option)
            }
        }
    }

    fun toggleCamera() {
        viewModelScope.launch {
            val mode = _uiState.value.mode
            val newValue = !_uiState.value.cameraEnabled
            _uiState.value = _uiState.value.copy(cameraEnabled = newValue)
            settingsRepository.setCameraEnabled(mode, newValue)
        }
    }

    fun onBarcodeScanned(barcode: String) {
        if (processing) return
        val now = System.currentTimeMillis()
        if (barcode == lastScannedBarcode && (now - lastScanTime) < 1_000L) return
        lastScannedBarcode = barcode
        lastScanTime = now
        processing = true
        viewModelScope.launch {
            val product = productRepository.getByBarcode(barcode)
            _events.emit(ScanEvent.PlayFeedback)
            if (product != null) {
                _events.emit(ScanEvent.NavigateDetail(barcode))
            } else {
                _events.emit(ScanEvent.NavigateNotFound(barcode))
            }
            processing = false
        }
    }
}
