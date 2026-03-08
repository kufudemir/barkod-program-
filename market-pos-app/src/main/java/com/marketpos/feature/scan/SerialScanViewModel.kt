package com.marketpos.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.cart.CartManager
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.Product
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
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

data class LastSerialScanUi(
    val barcode: String,
    val name: String,
    val salePriceLabel: String,
    val cartQuantity: Int,
    val stockQty: Int,
    val minStockQty: Int
)

data class SerialCartPreviewUi(
    val barcode: String,
    val name: String,
    val quantity: Int,
    val lineTotalLabel: String
)

data class SerialScanUiState(
    val mode: AppMode = AppMode.CASHIER,
    val isPro: Boolean = false,
    val cameraEnabled: Boolean = true,
    val cartItemCount: Int = 0,
    val cartTotalLabel: String = MoneyUtils.formatKurus(0L),
    val cooldownOption: SerialScanCooldownOption = SerialScanCooldownOption.SAFE,
    val scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM,
    val lastScan: LastSerialScanUi? = null,
    val cartPreview: List<SerialCartPreviewUi> = emptyList(),
    val statusMessage: String = "Kamerayı barkoda tutun. Her tarama arasında 1.8 sn bekleme uygulanır."
)

sealed interface SerialScanEvent {
    data object PlayFeedback : SerialScanEvent
    data class ShowMessage(val message: String) : SerialScanEvent
}

@HiltViewModel
class SerialScanViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val settingsRepository: SettingsRepository,
    private val cartManager: CartManager,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SerialScanUiState())
    val uiState: StateFlow<SerialScanUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SerialScanEvent>()
    val events: SharedFlow<SerialScanEvent> = _events.asSharedFlow()

    private var processing = false
    private var nextAllowedScanAt = 0L

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
                    cartTotalLabel = MoneyUtils.formatKurus(items.sumOf { it.lineTotalKurus }),
                    cartPreview = items.takeLast(4).reversed().map {
                        SerialCartPreviewUi(
                            barcode = it.barcode,
                            name = it.name,
                            quantity = it.quantity,
                            lineTotalLabel = MoneyUtils.formatKurus(it.lineTotalKurus)
                        )
                    }
                )
            }
        }
        viewModelScope.launch {
            premiumRepository.observeState().collect { premiumState ->
                _uiState.value = _uiState.value.copy(isPro = premiumState.isPro)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeSerialScanCooldown().collect { option ->
                _uiState.value = _uiState.value.copy(
                    cooldownOption = option,
                    statusMessage = defaultStatusMessage(option)
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
        if (!_uiState.value.isPro) {
            viewModelScope.launch {
                _events.emit(SerialScanEvent.ShowMessage("${PremiumFeature.SERIAL_SCAN.title} PRO sürümde"))
            }
            return
        }
        if (!_uiState.value.cameraEnabled || processing) return
        val now = System.currentTimeMillis()
        if (now < nextAllowedScanAt) return

        processing = true
        nextAllowedScanAt = now + _uiState.value.cooldownOption.millis

        viewModelScope.launch {
            val product = productRepository.getByBarcode(barcode)
            if (product == null) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Ürün bulunamadı: $barcode"
                )
                _events.emit(SerialScanEvent.ShowMessage("Ürün bulunamadı"))
                processing = false
                return@launch
            }

            cartManager.addProduct(product)
            val cartQuantity = cartManager.items.value.firstOrNull { it.barcode == product.barcode }?.quantity ?: 1
            _uiState.value = _uiState.value.copy(
                lastScan = product.toLastScanUi(cartQuantity),
                statusMessage = "Son ürün sepete eklendi. Yeni tarama icin barkodu kadrajdan cekin."
            )
            _events.emit(SerialScanEvent.PlayFeedback)
            processing = false
        }
    }

    fun increaseLastScan() {
        increaseLastScan(1)
    }

    fun increaseLastScan(step: Int) {
        val barcode = _uiState.value.lastScan?.barcode ?: return
        repeat(step.coerceAtLeast(1)) {
            cartManager.increase(barcode)
        }
        refreshLastScanFromCart(barcode, "Son ürünun adedi artirildi")
    }

    fun decreaseLastScan() {
        decreaseLastScan(1)
    }

    fun decreaseLastScan(step: Int) {
        val barcode = _uiState.value.lastScan?.barcode ?: return
        repeat(step.coerceAtLeast(1)) {
            cartManager.decrease(barcode)
        }
        refreshLastScanFromCart(barcode, "Son ürünun adedi azaltildi")
    }

    fun removeLastScan() {
        val barcode = _uiState.value.lastScan?.barcode ?: return
        cartManager.remove(barcode)
        _uiState.value = _uiState.value.copy(
            lastScan = null,
            statusMessage = "Son taranan ürün sepetten çıkarıldı"
        )
    }

    fun removePreviewItem(barcode: String) {
        cartManager.remove(barcode)
        if (_uiState.value.lastScan?.barcode == barcode) {
            _uiState.value = _uiState.value.copy(lastScan = null)
        }
        _uiState.value = _uiState.value.copy(
            statusMessage = "Mini kasadan ürün çıkarıldı"
        )
    }

    fun setLastScanQuantity(quantityInput: String) {
        val barcode = _uiState.value.lastScan?.barcode ?: return
        val quantity = quantityInput.toIntOrNull()
        if (quantity == null || quantity < 0) {
            viewModelScope.launch {
                _events.emit(SerialScanEvent.ShowMessage("Geçerli adet giriniz"))
            }
            return
        }
        cartManager.setQuantity(barcode, quantity)
        refreshLastScanFromCart(barcode, "Son ürünün adedi güncellendi")
    }

    private fun refreshLastScanFromCart(barcode: String, status: String) {
        viewModelScope.launch {
            val product = productRepository.getByBarcode(barcode) ?: return@launch
            val quantity = cartManager.quantityOf(barcode)
            _uiState.value = _uiState.value.copy(
                lastScan = if (quantity > 0) product.toLastScanUi(quantity) else null,
                statusMessage = status
            )
        }
    }

    private fun Product.toLastScanUi(cartQuantity: Int): LastSerialScanUi {
        return LastSerialScanUi(
            barcode = barcode,
            name = name,
            salePriceLabel = MoneyUtils.formatKurus(salePriceKurus),
            cartQuantity = cartQuantity,
            stockQty = stockQty,
            minStockQty = minStockQty
        )
    }

    private fun defaultStatusMessage(option: SerialScanCooldownOption): String {
        return "Kamerayı barkoda tutun. Her tarama arasında ${option.label} bekleme uygulanır."
    }
}




