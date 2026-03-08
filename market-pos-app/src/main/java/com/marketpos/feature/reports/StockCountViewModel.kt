package com.marketpos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
import com.marketpos.domain.model.StockCountItem
import com.marketpos.domain.model.StockCountSession
import com.marketpos.domain.repository.PremiumRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.repository.StockCountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StockCountFilter(val label: String) {
    ALL("Tüm"),
    DIFFERENT("Farkli"),
    MATCHED("Eslesen")
}

data class StockCountUiState(
    val isPro: Boolean = false,
    val cameraEnabled: Boolean = true,
    val scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM,
    val cooldownOption: SerialScanCooldownOption = SerialScanCooldownOption.SAFE,
    val session: StockCountSession = StockCountSession(),
    val selectedFilter: StockCountFilter = StockCountFilter.DIFFERENT,
    val lastScannedItem: StockCountItem? = null,
    val statusMessage: String = "Sayilan ürünler fark raporuna eklenir. Sayilmayan ürünler rapora dahil edilmez."
) {
    val visibleItems: List<StockCountItem>
        get() = when (selectedFilter) {
            StockCountFilter.ALL -> session.items
            StockCountFilter.DIFFERENT -> session.items.filter { it.differenceQty != 0 }
            StockCountFilter.MATCHED -> session.items.filter { it.differenceQty == 0 }
        }
}

sealed interface StockCountEvent {
    data class ShowMessage(val message: String) : StockCountEvent
    data object PlayFeedback : StockCountEvent
    data object RequirePremium : StockCountEvent
    data class RequestApplyConfirmation(val affectedCount: Int) : StockCountEvent
}

@HiltViewModel
class StockCountViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val premiumRepository: PremiumRepository,
    private val stockCountRepository: StockCountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockCountUiState())
    val uiState: StateFlow<StockCountUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StockCountEvent>()
    val events: SharedFlow<StockCountEvent> = _events.asSharedFlow()

    private var processing = false
    private var nextAllowedScanAt = 0L

    init {
        viewModelScope.launch {
            premiumRepository.observeState().collect { state ->
                _uiState.value = _uiState.value.copy(isPro = state.isPro)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeStockCountCameraEnabled().collect { enabled ->
                _uiState.value = _uiState.value.copy(cameraEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeSerialScanCooldown().collect { option ->
                _uiState.value = _uiState.value.copy(cooldownOption = option)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeScanBoxSize().collect { option ->
                _uiState.value = _uiState.value.copy(scanBoxSize = option)
            }
        }
        viewModelScope.launch {
            stockCountRepository.observeSession().collect { session ->
                val currentLast = _uiState.value.lastScannedItem
                val updatedLast = currentLast?.let { last ->
                    session.items.firstOrNull { it.barcode == last.barcode }
                }
                _uiState.value = _uiState.value.copy(
                    session = session,
                    lastScannedItem = updatedLast
                )
            }
        }
    }

    fun toggleCamera() {
        viewModelScope.launch {
            settingsRepository.setStockCountCameraEnabled(!_uiState.value.cameraEnabled)
        }
    }

    fun selectFilter(filter: StockCountFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun onBarcodeScanned(barcode: String) {
        if (!_uiState.value.isPro) {
            viewModelScope.launch { _events.emit(StockCountEvent.RequirePremium) }
            return
        }
        if (!_uiState.value.cameraEnabled || processing) return
        val now = System.currentTimeMillis()
        if (now < nextAllowedScanAt) return

        processing = true
        nextAllowedScanAt = now + _uiState.value.cooldownOption.millis

        viewModelScope.launch {
            stockCountRepository.addScan(barcode)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        lastScannedItem = result.item,
                        statusMessage = "${result.item.name} sayıma eklendi. Yeni adet: ${result.newCount}"
                    )
                    _events.emit(StockCountEvent.PlayFeedback)
                }
                .onFailure {
                    _events.emit(StockCountEvent.ShowMessage(it.message ?: "Sayım kaydı oluşturulamadı"))
                }
            processing = false
        }
    }

    fun updateItemCount(barcode: String, countInput: String) {
        val count = countInput.toIntOrNull()
        if (count == null || count < 0) {
            viewModelScope.launch { _events.emit(StockCountEvent.ShowMessage("Geçerli sayım adedi girin")) }
            return
        }
        viewModelScope.launch {
            stockCountRepository.updateCount(barcode, count)
                .onSuccess {
                    _events.emit(StockCountEvent.ShowMessage("Sayım adedi güncellendi"))
                }
                .onFailure {
                    _events.emit(StockCountEvent.ShowMessage(it.message ?: "Sayım adedi güncellenemedi"))
                }
        }
    }

    fun removeItem(barcode: String) {
        viewModelScope.launch {
            stockCountRepository.removeItem(barcode)
                .onSuccess {
                    if (_uiState.value.lastScannedItem?.barcode == barcode) {
                        _uiState.value = _uiState.value.copy(lastScannedItem = null)
                    }
                    _events.emit(StockCountEvent.ShowMessage("Sayım satırı kaldırıldı"))
                }
                .onFailure {
                    _events.emit(StockCountEvent.ShowMessage(it.message ?: "Sayım satırı kaldırılamadı"))
                }
        }
    }

    fun requestApplyCountResult() {
        val affectedCount = _uiState.value.session.items.size
        if (affectedCount == 0) {
            viewModelScope.launch { _events.emit(StockCountEvent.ShowMessage("Uygulanacak sayım sonucu yok")) }
            return
        }
        viewModelScope.launch {
            _events.emit(StockCountEvent.RequestApplyConfirmation(affectedCount))
        }
    }

    fun applyCountResult() {
        viewModelScope.launch {
            stockCountRepository.applyCountResult()
                .onSuccess { affected ->
                    _uiState.value = _uiState.value.copy(
                        lastScannedItem = null,
                        statusMessage = "$affected ürün icin sayim sonucu stoga uygulandi"
                    )
                    _events.emit(StockCountEvent.ShowMessage("Sayım farkları stoğa uygulandı"))
                }
                .onFailure {
                    _events.emit(StockCountEvent.ShowMessage(it.message ?: "Sayım sonucu uygulanamadı"))
                }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            stockCountRepository.clearSession()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        lastScannedItem = null,
                        statusMessage = "Sayım oturumu temizlendi"
                    )
                    _events.emit(StockCountEvent.ShowMessage("Sayım oturumu temizlendi"))
                }
                .onFailure {
                    _events.emit(StockCountEvent.ShowMessage(it.message ?: "Sayım oturumu temizlenemedi"))
                }
        }
    }
}

