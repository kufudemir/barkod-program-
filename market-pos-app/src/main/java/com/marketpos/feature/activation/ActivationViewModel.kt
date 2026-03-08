package com.marketpos.feature.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.data.network.ActivationConflictException
import com.marketpos.domain.model.RecoverableCompany
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.ProductRepository
import com.marketpos.domain.usecase.ActivateCompanyUseCase
import com.marketpos.domain.usecase.FlushSyncQueueUseCase
import com.marketpos.domain.usecase.RestoreCompanyCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivationUiState(
    val companyName: String = "",
    val deviceName: String = "",
    val deviceUid: String = "",
    val isLoading: Boolean = true,
    val isActivating: Boolean = false,
    val isLoadingCompanies: Boolean = false,
    val existingCompanyName: String? = null,
    val existingCompanyCode: String? = null,
    val error: String? = null,
    val ownedCompanies: List<RecoverableCompany> = emptyList(),
    val pendingRestoreCompanyCode: String? = null,
    val pendingRestoreCompanyName: String? = null,
    val pendingRestoreProductCount: Int = 0
)

sealed interface ActivationEvent {
    data object NavigateScan : ActivationEvent
    data class ShowMessage(val message: String) : ActivationEvent
    data class ShowExistingCompanyPrompt(val companyName: String) : ActivationEvent
    data class ShowRestorePrompt(val companyName: String, val productCount: Int) : ActivationEvent
}

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activationRepository: ActivationRepository,
    private val activateCompanyUseCase: ActivateCompanyUseCase,
    private val flushSyncQueueUseCase: FlushSyncQueueUseCase,
    private val restoreCompanyCatalogUseCase: RestoreCompanyCatalogUseCase,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActivationEvent>()
    val events: SharedFlow<ActivationEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                deviceName = activationRepository.getDeviceName(),
                deviceUid = activationRepository.getDeviceUid(),
                isLoading = false
            )
            loadOwnedCompanies()
        }
    }

    fun updateCompanyName(value: String) {
        _uiState.value = _uiState.value.copy(
            companyName = value,
            error = null,
            existingCompanyName = null,
            existingCompanyCode = null
        )
    }

    fun activate() {
        val companyName = _uiState.value.companyName.trim()
        if (companyName.isBlank()) {
            viewModelScope.launch { _events.emit(ActivationEvent.ShowMessage("Firma ünvanı gerekli")) }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, error = null)
            activateCompanyUseCase(companyName)
                .onSuccess {
                    handleActivationSuccess()
                }
                .onFailure { error ->
                    if (error is ActivationConflictException) {
                        _uiState.value = _uiState.value.copy(
                            isActivating = false,
                            existingCompanyName = error.existingCompanyName,
                            existingCompanyCode = error.existingCompanyCode,
                            error = error.message
                        )
                        _events.emit(ActivationEvent.ShowExistingCompanyPrompt(error.existingCompanyName))
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isActivating = false,
                            error = error.message ?: "Aktivasyon başarısız"
                        )
                    }
                }
        }
    }

    fun continueWithExistingCompany() {
        val companyCode = _uiState.value.existingCompanyCode ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, error = null)
            activateCompanyUseCase.continueWithExistingCompany(companyCode)
                .onSuccess {
                    handleActivationSuccess(companyCode)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        error = error.message ?: "Aktivasyon başarısız"
                    )
                }
        }
    }

    fun dismissExistingCompanyPrompt() {
        _uiState.value = _uiState.value.copy(
            existingCompanyName = null,
            existingCompanyCode = null,
            error = null
        )
    }

    fun createNewCompanyInstead() {
        val companyName = _uiState.value.companyName.trim()
        if (companyName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, error = null)
            activateCompanyUseCase.createNewCompany(companyName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        existingCompanyName = null,
                        existingCompanyCode = null
                    )
                    handleActivationSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        error = error.message ?: "Yeni firma oluşturulamadı"
                    )
                }
        }
    }

    fun activateOwnedCompany(company: RecoverableCompany) {
        updateCompanyName(company.companyName)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, error = null)
            activateCompanyUseCase.continueWithExistingCompany(company.companyCode)
                .onSuccess {
                    handleActivationSuccess(company.companyCode)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        error = error.message ?: "Firma aktivasyonu başarısız"
                    )
                }
        }
    }

    fun restoreCatalog(replaceExisting: Boolean) {
        val companyCode = _uiState.value.pendingRestoreCompanyCode ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, error = null)
            restoreCompanyCatalogUseCase(companyCode, replaceExisting)
                .onSuccess { restoredCount ->
                    flushSyncQueueUseCase()
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        pendingRestoreCompanyCode = null,
                        pendingRestoreCompanyName = null,
                        pendingRestoreProductCount = 0
                    )
                    _events.emit(ActivationEvent.ShowMessage("$restoredCount ürün buluttan geri yüklendi"))
                    _events.emit(ActivationEvent.NavigateScan)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        error = error.message ?: "Bulut katalog geri yüklenemedi"
                    )
                }
        }
    }

    fun skipRestore() {
        _uiState.value = _uiState.value.copy(
            pendingRestoreCompanyCode = null,
            pendingRestoreCompanyName = null,
            pendingRestoreProductCount = 0
        )
        viewModelScope.launch {
            flushSyncQueueUseCase()
            _events.emit(ActivationEvent.NavigateScan)
        }
    }

    private suspend fun handleActivationSuccess(companyCodeForRestore: String? = null) {
        val state = activationRepository.getActivationState()
        val companyCode = companyCodeForRestore ?: state.companyCode
        if (companyCode.isNullOrBlank()) {
            flushSyncQueueUseCase()
            _uiState.value = _uiState.value.copy(isActivating = false)
            _events.emit(ActivationEvent.NavigateScan)
            return
        }

        activationRepository.fetchCompanyCatalog(companyCode)
            .onSuccess { catalog ->
                if (catalog.isEmpty()) {
                    flushSyncQueueUseCase()
                    _uiState.value = _uiState.value.copy(isActivating = false)
                    _events.emit(ActivationEvent.NavigateScan)
                    return@onSuccess
                }

                val localCount = productRepository.countActiveProducts()
                if (localCount == 0) {
                    restoreCompanyCatalogUseCase(companyCode, replaceExisting = false)
                        .onSuccess { restoredCount ->
                            flushSyncQueueUseCase()
                            _uiState.value = _uiState.value.copy(isActivating = false)
                            _events.emit(ActivationEvent.ShowMessage("$restoredCount ürün buluttan geri yüklendi"))
                            _events.emit(ActivationEvent.NavigateScan)
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isActivating = false,
                                error = error.message ?: "Bulut katalog geri yüklenemedi"
                            )
                        }
                } else {
                    val companyName = state.companyName ?: _uiState.value.companyName
                    _uiState.value = _uiState.value.copy(
                        isActivating = false,
                        pendingRestoreCompanyCode = companyCode,
                        pendingRestoreCompanyName = companyName,
                        pendingRestoreProductCount = catalog.size
                    )
                    _events.emit(ActivationEvent.ShowRestorePrompt(companyName, catalog.size))
                }
            }
            .onFailure {
                flushSyncQueueUseCase()
                _uiState.value = _uiState.value.copy(isActivating = false)
                _events.emit(ActivationEvent.NavigateScan)
            }
    }

    private suspend fun loadOwnedCompanies() {
        _uiState.value = _uiState.value.copy(isLoadingCompanies = true)
        activationRepository.listOwnedCompanies()
            .onSuccess { companies ->
                _uiState.value = _uiState.value.copy(
                    isLoadingCompanies = false,
                    ownedCompanies = companies
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoadingCompanies = false,
                    ownedCompanies = emptyList()
                )
            }
    }
}

