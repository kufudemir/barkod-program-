package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.AccountSessionState
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.AppThemeMode
import com.marketpos.domain.model.CompanyActivationState
import com.marketpos.domain.model.LegalConsentState
import com.marketpos.domain.model.PremiumState
import com.marketpos.domain.model.ScanBoxSizeOption
import com.marketpos.domain.model.SerialScanCooldownOption
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.LegalConsentRepository
import com.marketpos.domain.repository.PremiumRepository
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.repository.SaleRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.repository.SyncOutboxRepository
import com.marketpos.domain.usecase.FlushSyncQueueUseCase
import com.marketpos.domain.usecase.ResetProductCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val accountSession: AccountSessionState = AccountSessionState(),
    val mode: AppMode = AppMode.CASHIER,
    val isResettingCatalog: Boolean = false,
    val isSyncing: Boolean = false,
    val isChangingPassword: Boolean = false,
    val premiumState: PremiumState = PremiumState(),
    val activationState: CompanyActivationState = CompanyActivationState(),
    val pendingSyncCount: Int = 0,
    val criticalStockCount: Int = 0,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val serialScanCooldown: SerialScanCooldownOption = SerialScanCooldownOption.SAFE,
    val scanBoxSize: ScanBoxSizeOption = ScanBoxSizeOption.MEDIUM,
    val totalProductCount: Int = 0,
    val activeProductCount: Int = 0,
    val totalSaleCount: Int = 0,
    val legalConsentState: LegalConsentState = LegalConsentState(currentVersion = "")
)

sealed interface SettingsEvent {
    data class ShowMessage(val message: String) : SettingsEvent
    data class RequestCatalogResetConfirmation(val verifiedPin: String) : SettingsEvent
    data object ConnectionCleared : SettingsEvent
    data object SessionCleared : SettingsEvent
    data object PasswordChanged : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val activationRepository: ActivationRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val legalConsentRepository: LegalConsentRepository,
    private val syncOutboxRepository: SyncOutboxRepository,
    private val premiumRepository: PremiumRepository,
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val flushSyncQueueUseCase: FlushSyncQueueUseCase,
    private val resetProductCatalogUseCase: ResetProductCatalogUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                accountSession = accountSessionRepository.getState(),
                mode = settingsRepository.getMode(),
                activationState = activationRepository.getActivationState(),
                premiumState = premiumRepository.getState(),
                legalConsentState = legalConsentRepository.getState()
            )
        }
        viewModelScope.launch {
            accountSessionRepository.observeState().collect { accountSession ->
                _uiState.value = _uiState.value.copy(accountSession = accountSession)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode ->
                _uiState.value = _uiState.value.copy(mode = mode)
            }
        }
        viewModelScope.launch {
            premiumRepository.observeState().collect { premiumState ->
                _uiState.value = _uiState.value.copy(premiumState = premiumState)
            }
        }
        viewModelScope.launch {
            legalConsentRepository.observeState().collect { legalConsentState ->
                _uiState.value = _uiState.value.copy(legalConsentState = legalConsentState)
            }
        }
        viewModelScope.launch {
            activationRepository.observeActivationState().collect { activationState ->
                _uiState.value = _uiState.value.copy(activationState = activationState)
            }
        }
        viewModelScope.launch {
            syncOutboxRepository.observePendingCount().collect { pendingCount ->
                _uiState.value = _uiState.value.copy(pendingSyncCount = pendingCount)
            }
        }
        viewModelScope.launch {
            productRepository.observeAllActive().collect { products ->
                _uiState.value = _uiState.value.copy(
                    criticalStockCount = products.count { it.stockQty <= it.minStockQty },
                    activeProductCount = products.size
                )
            }
        }
        viewModelScope.launch {
            settingsRepository.observeThemeMode().collect { themeMode ->
                _uiState.value = _uiState.value.copy(themeMode = themeMode)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeSerialScanCooldown().collect { option ->
                _uiState.value = _uiState.value.copy(serialScanCooldown = option)
            }
        }
        viewModelScope.launch {
            settingsRepository.observeScanBoxSize().collect { option ->
                _uiState.value = _uiState.value.copy(scanBoxSize = option)
            }
        }
        refreshSystemSummary()
    }

    fun switchMode(target: AppMode, pin: String?) {
        viewModelScope.launch {
            val current = _uiState.value.mode
            if (current == target) return@launch

            if (target == AppMode.ADMIN) {
                if (pin.isNullOrBlank()) {
                    _events.emit(SettingsEvent.ShowMessage("Admin moda gecis icin PIN gerekli"))
                    return@launch
                }
                if (!settingsRepository.verifyPin(pin)) {
                    _events.emit(SettingsEvent.ShowMessage("PIN hatalı"))
                    return@launch
                }
            }

            settingsRepository.setMode(target)
            _events.emit(SettingsEvent.ShowMessage("Mod güncellendi"))
        }
    }

    fun changePin(currentPin: String, newPin: String) {
        viewModelScope.launch {
            if (!settingsRepository.verifyPin(currentPin)) {
                _events.emit(SettingsEvent.ShowMessage("Mevcut PIN hatalı"))
                return@launch
            }
            settingsRepository.setPin(newPin)
                .onSuccess { _events.emit(SettingsEvent.ShowMessage("PIN güncellendi")) }
                .onFailure { _events.emit(SettingsEvent.ShowMessage(it.message ?: "PIN güncellenemedi")) }
        }
    }

    fun confirmResetPin(pin: String) {
        viewModelScope.launch {
            if (_uiState.value.mode != AppMode.ADMIN) {
                _events.emit(SettingsEvent.ShowMessage("Bu işlem için Admin modunda olmanız gerekir"))
                return@launch
            }
            if (!settingsRepository.verifyPin(pin)) {
                _events.emit(SettingsEvent.ShowMessage("PIN hatalı"))
                return@launch
            }
            _events.emit(SettingsEvent.RequestCatalogResetConfirmation(pin))
        }
    }

    fun resetProductCatalog(pin: String) {
        viewModelScope.launch {
            if (_uiState.value.mode != AppMode.ADMIN) {
                _events.emit(SettingsEvent.ShowMessage("Bu işlem için Admin modunda olmanız gerekir"))
                return@launch
            }
            if (!settingsRepository.verifyPin(pin)) {
                _events.emit(SettingsEvent.ShowMessage("PIN hatalı"))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isResettingCatalog = true)
            resetProductCatalogUseCase()
                .onSuccess {
                    _events.emit(SettingsEvent.ShowMessage("Ürün listesi sıfırlandı"))
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Ürün listesi sıfırlanamadı"))
                }
            _uiState.value = _uiState.value.copy(isResettingCatalog = false)
            refreshSystemSummary()
        }
    }

    fun updateSerialScanCooldown(option: SerialScanCooldownOption) {
        viewModelScope.launch {
            settingsRepository.setSerialScanCooldown(option)
            _events.emit(SettingsEvent.ShowMessage("Seri tarama bekleme süresi güncellendi"))
        }
    }

    fun updateScanBoxSize(option: ScanBoxSizeOption) {
        viewModelScope.launch {
            settingsRepository.setScanBoxSize(option)
            _events.emit(SettingsEvent.ShowMessage("Tarama kutusu boyutu güncellendi"))
        }
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
            _events.emit(SettingsEvent.ShowMessage("Tema güncellendi"))
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            flushSyncQueueUseCase()
                .onSuccess { result ->
                    _events.emit(
                        SettingsEvent.ShowMessage(
                            if (result.processedCount == 0 && result.pendingCount == 0) {
                                "Bekleyen senkron kaydı yok"
                            } else {
                                "Senkron tamamlandı. İşlenen: ${result.processedCount}, Bekleyen: ${result.pendingCount}"
                            }
                        )
                    )
                }
                .onFailure { error ->
                    _events.emit(SettingsEvent.ShowMessage(error.message ?: "Senkron başarısız"))
                }
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun clearCompanyConnection(pin: String) {
        viewModelScope.launch {
            if (_uiState.value.mode != AppMode.ADMIN) {
                _events.emit(SettingsEvent.ShowMessage("Bu işlem için Admin modunda olmanız gerekir"))
                return@launch
            }
            if (!settingsRepository.verifyPin(pin)) {
                _events.emit(SettingsEvent.ShowMessage("PIN hatalı"))
                return@launch
            }

            activationRepository.clearActivation()
                .onSuccess {
                    syncOutboxRepository.clearAll()
                    _events.emit(SettingsEvent.ShowMessage("Firma bağlantısı sıfırlandı"))
                    _events.emit(SettingsEvent.ConnectionCleared)
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Firma bağlantısı sıfırlanamadı"))
                }
        }
    }

    fun logoutSession() {
        viewModelScope.launch {
            accountSessionRepository.logout()
                .onSuccess {
                    activationRepository.clearActivation()
                    syncOutboxRepository.clearAll()
                    _events.emit(SettingsEvent.ShowMessage("Oturum kapatıldı"))
                    _events.emit(SettingsEvent.SessionCleared)
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Oturum kapatılamadı"))
                }
        }
    }

    fun changeAccountPassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (!_uiState.value.accountSession.isRegistered) {
                _events.emit(SettingsEvent.ShowMessage("Bu işlem için kayıtlı kullanıcı oturumu gerekli"))
                return@launch
            }
            if (currentPassword.length < 8) {
                _events.emit(SettingsEvent.ShowMessage("Mevcut şifre en az 8 karakter olmalı"))
                return@launch
            }
            if (newPassword.length < 8) {
                _events.emit(SettingsEvent.ShowMessage("Yeni şifre en az 8 karakter olmalı"))
                return@launch
            }
            if (newPassword != confirmPassword) {
                _events.emit(SettingsEvent.ShowMessage("Yeni şifreler birbiriyle eşleşmiyor"))
                return@launch
            }
            if (currentPassword == newPassword) {
                _events.emit(SettingsEvent.ShowMessage("Yeni şifre mevcut şifreden farklı olmalı"))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isChangingPassword = true)
            accountSessionRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _events.emit(SettingsEvent.ShowMessage("Şifre güncellendi"))
                    _events.emit(SettingsEvent.PasswordChanged)
                }
                .onFailure {
                    _events.emit(SettingsEvent.ShowMessage(it.message ?: "Şifre güncellenemedi"))
                }
            _uiState.value = _uiState.value.copy(isChangingPassword = false)
        }
    }

    fun refreshSystemSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                totalProductCount = productRepository.countAllProducts(),
                activeProductCount = productRepository.countActiveProducts(),
                totalSaleCount = saleRepository.getTotalSaleCount()
            )
        }
    }
}

