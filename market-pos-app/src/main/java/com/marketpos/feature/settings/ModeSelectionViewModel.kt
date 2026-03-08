package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.AppSaleMode
import com.marketpos.domain.repository.AccountSessionRepository
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

data class ModeSelectionUiState(
    val currentSaleMode: AppSaleMode = AppSaleMode.MOBILE_SALES,
    val isRegisteredSession: Boolean = false,
    val isLoading: Boolean = true
)

sealed interface ModeSelectionEvent {
    data object NavigateMobileScan : ModeSelectionEvent
    data object NavigateWebCompanion : ModeSelectionEvent
    data object NavigateLogin : ModeSelectionEvent
    data class ShowMessage(val message: String) : ModeSelectionEvent
}

@HiltViewModel
class ModeSelectionViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val accountSessionRepository: AccountSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeSelectionUiState())
    val uiState: StateFlow<ModeSelectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ModeSelectionEvent>()
    val events: SharedFlow<ModeSelectionEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.initializeDefaultsIfNeeded()
            val accountSession = accountSessionRepository.getState()
            _uiState.value = ModeSelectionUiState(
                currentSaleMode = settingsRepository.getSaleMode(),
                isRegisteredSession = accountSession.isRegistered,
                isLoading = false
            )
        }
    }

    fun selectMobileSale() {
        viewModelScope.launch {
            settingsRepository.setSaleMode(AppSaleMode.MOBILE_SALES)
            _uiState.value = _uiState.value.copy(currentSaleMode = AppSaleMode.MOBILE_SALES)
            _events.emit(ModeSelectionEvent.NavigateMobileScan)
        }
    }

    fun selectWebSale() {
        viewModelScope.launch {
            val session = accountSessionRepository.getState()
            if (!session.isRegistered) {
                _events.emit(ModeSelectionEvent.ShowMessage("Web satis modu icin kayitli kullanici girisi gerekli."))
                _events.emit(ModeSelectionEvent.NavigateLogin)
                return@launch
            }

            settingsRepository.setSaleMode(AppSaleMode.WEB_SALES)
            _uiState.value = _uiState.value.copy(
                currentSaleMode = AppSaleMode.WEB_SALES,
                isRegisteredSession = true
            )
            _events.emit(ModeSelectionEvent.NavigateWebCompanion)
        }
    }
}
