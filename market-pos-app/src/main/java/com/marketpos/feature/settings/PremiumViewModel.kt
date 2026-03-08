package com.marketpos.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.PremiumFeature
import com.marketpos.domain.model.PremiumState
import com.marketpos.domain.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PremiumUiState(
    val state: PremiumState = PremiumState(),
    val focusedFeature: PremiumFeature? = null,
    val licenseInput: String = "",
    val isActivating: Boolean = false,
    val comparisonExpanded: Boolean = false
)

sealed interface PremiumEvent {
    data class ShowMessage(val message: String) : PremiumEvent
    data class CopyText(val text: String, val message: String) : PremiumEvent
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val focusedFeature = savedStateHandle.get<String>("feature")
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { PremiumFeature.valueOf(it) }.getOrNull() }

    private val _uiState = MutableStateFlow(PremiumUiState(focusedFeature = focusedFeature))
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PremiumEvent>()
    val events: SharedFlow<PremiumEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            premiumRepository.observeState().collect { state ->
                _uiState.value = _uiState.value.copy(state = state)
            }
        }
    }

    fun updateLicenseInput(value: String) {
        _uiState.value = _uiState.value.copy(licenseInput = value.filterNot(Char::isWhitespace))
    }

    fun activateLicense() {
        val code = _uiState.value.licenseInput
        if (code.isBlank()) {
            viewModelScope.launch { _events.emit(PremiumEvent.ShowMessage("Lisans kodu girin")) }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true)
            premiumRepository.activateWithLicenseCode(code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isActivating = false, licenseInput = "")
                    _events.emit(PremiumEvent.ShowMessage("Premium aktif edildi"))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isActivating = false)
                    _events.emit(PremiumEvent.ShowMessage(it.message ?: "Lisans dogrulanamadi"))
                }
        }
    }

    fun startTrial(days: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true)
            premiumRepository.startTrial(days)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isActivating = false)
                    _events.emit(PremiumEvent.ShowMessage("$days gunluk deneme surumu baslatildi"))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isActivating = false)
                    _events.emit(PremiumEvent.ShowMessage(it.message ?: "Deneme baslatilamadi"))
                }
        }
    }

    fun clearLicense() {
        viewModelScope.launch {
            premiumRepository.clearLicense()
                .onSuccess { _events.emit(PremiumEvent.ShowMessage("Premium lisansi kaldirildi")) }
                .onFailure { _events.emit(PremiumEvent.ShowMessage(it.message ?: "Lisans kaldirilamadi")) }
        }
    }

    fun copyDeviceCode() {
        viewModelScope.launch {
            _events.emit(
                PremiumEvent.CopyText(
                    text = _uiState.value.state.deviceCode,
                    message = "Cihaz kodu kopyalandı"
                )
            )
        }
    }

    fun toggleComparison() {
        _uiState.value = _uiState.value.copy(comparisonExpanded = !_uiState.value.comparisonExpanded)
    }
}
