package com.marketpos.feature.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.core.util.MoneyUtils
import com.marketpos.domain.model.ActiveWebPosSessionState
import com.marketpos.domain.model.AppSaleMode
import com.marketpos.domain.model.CompanionCartItem
import com.marketpos.domain.model.CompanionRecentSale
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.repository.WebSaleCompanionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WebCompanionUiState(
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val hasActiveSession: Boolean = false,
    val statusMessage: String? = null,
    val companyName: String = "",
    val branchName: String = "",
    val registerName: String = "",
    val sessionLabel: String = "",
    val itemCount: Int = 0,
    val totalAmountLabel: String = MoneyUtils.formatKurus(0L),
    val canCheckout: Boolean = false,
    val cartItems: List<CompanionCartItem> = emptyList(),
    val recentSales: List<CompanionRecentSale> = emptyList(),
    val selectedBarcode: String? = null,
    val cameraEnabled: Boolean = true,
    val flashEnabled: Boolean = false,
    val scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM
)

sealed interface WebCompanionEvent {
    data class ShowMessage(val message: String) : WebCompanionEvent
    data class OpenExternalUrl(val url: String) : WebCompanionEvent
    data object NavigateMobileScan : WebCompanionEvent
}

@HiltViewModel
class WebCompanionViewModel @Inject constructor(
    private val activationRepository: ActivationRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val settingsRepository: SettingsRepository,
    private val companionRepository: WebSaleCompanionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebCompanionUiState())
    val uiState: StateFlow<WebCompanionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WebCompanionEvent>()
    val events: SharedFlow<WebCompanionEvent> = _events.asSharedFlow()

    private var processingScan = false
    private var lastScannedBarcode: String = ""
    private var lastScanAt: Long = 0L
    private var nextAllowedScanAt: Long = 0L

    init {
        viewModelScope.launch {
            settingsRepository.observeScanBoxSize().collect { option ->
                _uiState.value = _uiState.value.copy(scanBoxSize = option)
            }
        }
        refreshActiveSession()
    }

    fun refreshActiveSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, statusMessage = null)

            val context = resolveRemoteContext()
            if (context == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasActiveSession = false,
                    statusMessage = "Web companion baglantisi icin kayitli hesap ve firma aktivasyonu gerekli."
                )
                return@launch
            }

            companionRepository.getActiveSession(
                accessToken = context.accessToken,
                companyCode = context.companyCode,
                deviceUid = context.deviceUid,
                deviceName = context.deviceName
            ).onSuccess { state ->
                applyRemoteState(state, isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasActiveSession = false,
                    statusMessage = error.message ?: "Web oturum bilgisi alinamadi."
                )
                _events.emit(WebCompanionEvent.ShowMessage(error.message ?: "Web oturum bilgisi alinamadi."))
            }
        }
    }

    fun onBarcodeScanned(barcode: String) {
        val normalizedBarcode = barcode.trim()
        if (normalizedBarcode.isBlank()) return
        if (processingScan || !_uiState.value.hasActiveSession || !_uiState.value.cameraEnabled) return

        val now = System.currentTimeMillis()
        if (now < nextAllowedScanAt) return
        if (normalizedBarcode == lastScannedBarcode && now - lastScanAt < 2_200L) return

        processingScan = true
        lastScannedBarcode = normalizedBarcode
        lastScanAt = now
        nextAllowedScanAt = now + 1_600L

        viewModelScope.launch {
            val context = resolveRemoteContext()
            if (context == null) {
                processingScan = false
                nextAllowedScanAt = System.currentTimeMillis() + 800L
                _events.emit(WebCompanionEvent.ShowMessage("Web companion icin gecerli hesap/firma baglantisi gerekli."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isProcessing = true, statusMessage = "Barkod isleniyor...")

            companionRepository.scanBarcode(
                accessToken = context.accessToken,
                companyCode = context.companyCode,
                deviceUid = context.deviceUid,
                deviceName = context.deviceName,
                barcode = normalizedBarcode
            ).onSuccess { state ->
                applyRemoteState(state, isLoading = false)
                nextAllowedScanAt = System.currentTimeMillis() + 1_500L
                _events.emit(WebCompanionEvent.ShowMessage(state.message ?: "Urun sepete eklendi."))
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    statusMessage = error.message ?: "Barkod islenemedi."
                )
                nextAllowedScanAt = System.currentTimeMillis() + 900L
                _events.emit(WebCompanionEvent.ShowMessage(error.message ?: "Barkod islenemedi."))
            }

            processingScan = false
        }
    }

    fun increment(barcode: String) {
        mutateSession(
            loadingMessage = "Adet guncelleniyor...",
            action = { context ->
                companionRepository.incrementItem(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode
                )
            }
        )
    }

    fun decrement(barcode: String) {
        mutateSession(
            loadingMessage = "Adet guncelleniyor...",
            action = { context ->
                companionRepository.decrementItem(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode
                )
            }
        )
    }

    fun remove(barcode: String) {
        mutateSession(
            loadingMessage = "Satir kaldiriliyor...",
            action = { context ->
                companionRepository.removeItem(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode
                )
            }
        )
    }

    fun setCustomPrice(barcode: String, salePriceKurus: Long) {
        mutateSession(
            loadingMessage = "Satir fiyati guncelleniyor...",
            action = { context ->
                companionRepository.setCustomPrice(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode,
                    salePriceKurus = salePriceKurus
                )
            }
        )
    }

    fun applyPercentDiscount(barcode: String, percent: Double) {
        mutateSession(
            loadingMessage = "Yuzde indirim uygulaniyor...",
            action = { context ->
                companionRepository.applyPercentDiscount(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode,
                    percent = percent
                )
            }
        )
    }

    fun applyFixedDiscount(barcode: String, discountKurus: Long) {
        mutateSession(
            loadingMessage = "Sabit indirim uygulaniyor...",
            action = { context ->
                companionRepository.applyFixedDiscount(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode,
                    discountKurus = discountKurus
                )
            }
        )
    }

    fun resetPrice(barcode: String) {
        mutateSession(
            loadingMessage = "Liste fiyati geri yukleniyor...",
            action = { context ->
                companionRepository.resetPrice(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName,
                    barcode = barcode
                )
            }
        )
    }

    fun completeSale() {
        mutateSession(
            loadingMessage = "Satis tamamlanıyor...",
            action = { context ->
                companionRepository.completeSale(
                    accessToken = context.accessToken,
                    companyCode = context.companyCode,
                    deviceUid = context.deviceUid,
                    deviceName = context.deviceName
                )
            }
        )
    }

    fun triggerPrint() {
        viewModelScope.launch {
            val context = resolveRemoteContext()
            if (context == null) {
                _events.emit(WebCompanionEvent.ShowMessage("Yazdirma tetigi icin gecerli hesap/firma baglantisi gerekli."))
                return@launch
            }

            companionRepository.triggerPrint(
                accessToken = context.accessToken,
                companyCode = context.companyCode,
                deviceUid = context.deviceUid,
                deviceName = context.deviceName
            ).onSuccess { payload ->
                _events.emit(WebCompanionEvent.ShowMessage(payload.message))
                val preferredUrl = payload.printUrl ?: payload.previewUrl ?: payload.pdfUrl
                if (!preferredUrl.isNullOrBlank()) {
                    _events.emit(WebCompanionEvent.OpenExternalUrl(preferredUrl))
                }
            }.onFailure { error ->
                _events.emit(WebCompanionEvent.ShowMessage(error.message ?: "Yazdirma tetigi gonderilemedi."))
            }
        }
    }

    fun toggleCamera() {
        _uiState.value = _uiState.value.copy(cameraEnabled = !_uiState.value.cameraEnabled)
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(flashEnabled = !_uiState.value.flashEnabled)
    }

    fun selectBarcode(barcode: String?) {
        _uiState.value = _uiState.value.copy(selectedBarcode = barcode)
    }

    fun switchToMobilePos() {
        viewModelScope.launch {
            settingsRepository.setSaleMode(AppSaleMode.MOBILE_SALES)
            _events.emit(WebCompanionEvent.NavigateMobileScan)
        }
    }

    private fun mutateSession(
        loadingMessage: String,
        action: suspend (RemoteContext) -> Result<ActiveWebPosSessionState>,
        finallyBlock: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val context = resolveRemoteContext()
            if (context == null) {
                _events.emit(WebCompanionEvent.ShowMessage("Web companion icin gecerli hesap/firma baglantisi gerekli."))
                finallyBlock?.invoke()
                return@launch
            }

            _uiState.value = _uiState.value.copy(isProcessing = true, statusMessage = loadingMessage)

            action(context)
                .onSuccess { state ->
                    applyRemoteState(state, isLoading = false)
                    state.message?.let { _events.emit(WebCompanionEvent.ShowMessage(it)) }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        statusMessage = error.message ?: "Islem basarisiz."
                    )
                    _events.emit(WebCompanionEvent.ShowMessage(error.message ?: "Islem basarisiz."))
                }

            finallyBlock?.invoke()
        }
    }

    private suspend fun resolveRemoteContext(): RemoteContext? {
        val accessToken = accountSessionRepository.getAccessToken() ?: return null
        val activation = activationRepository.getActivationState()
        val companyCode = activation.companyCode?.takeIf { it.isNotBlank() } ?: return null
        val deviceUid = activation.deviceUid?.takeIf { it.isNotBlank() } ?: activationRepository.getDeviceUid()
        val deviceName = activation.deviceName?.takeIf { it.isNotBlank() } ?: activationRepository.getDeviceName()

        return RemoteContext(
            accessToken = accessToken,
            companyCode = companyCode,
            deviceUid = deviceUid,
            deviceName = deviceName
        )
    }

    private fun applyRemoteState(state: ActiveWebPosSessionState, isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            isProcessing = false,
            hasActiveSession = state.hasActiveSession,
            statusMessage = state.message,
            companyName = state.companyName.orEmpty(),
            branchName = state.branchName.orEmpty(),
            registerName = state.registerName.orEmpty(),
            sessionLabel = state.saleSessionLabel.orEmpty(),
            itemCount = state.summary.itemCount,
            totalAmountLabel = MoneyUtils.formatKurus(state.summary.totalAmountKurus),
            canCheckout = state.summary.canCheckout,
            cartItems = state.cartItems,
            recentSales = state.recentSales,
            selectedBarcode = state.cartItems.firstOrNull()?.barcode
        )
    }

    private data class RemoteContext(
        val accessToken: String,
        val companyCode: String,
        val deviceUid: String,
        val deviceName: String
    )
}
