package com.marketpos.feature.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.model.AppMode
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

data class NotFoundUiState(
    val barcode: String = "",
    val mode: AppMode = AppMode.CASHIER
)

sealed interface NotFoundEvent {
    data class NavigateAddProduct(val barcode: String) : NotFoundEvent
    data object NavigateScan : NotFoundEvent
    data class ShowMessage(val message: String) : NotFoundEvent
}

@HiltViewModel
class NotFoundViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val barcodeArg = savedStateHandle.get<String>("barcode").orEmpty()
    private val _uiState = MutableStateFlow(NotFoundUiState(barcode = barcodeArg))
    val uiState: StateFlow<NotFoundUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotFoundEvent>()
    val events: SharedFlow<NotFoundEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeMode().collect { mode ->
                _uiState.value = _uiState.value.copy(mode = mode)
            }
        }
    }

    fun onRetry() {
        viewModelScope.launch { _events.emit(NotFoundEvent.NavigateScan) }
    }

    fun onAddDirect() {
        viewModelScope.launch {
            if (_uiState.value.mode == AppMode.ADMIN) {
                _events.emit(NotFoundEvent.NavigateAddProduct(_uiState.value.barcode))
            } else {
                _events.emit(NotFoundEvent.ShowMessage("Kasiyer modunda PIN gerekli"))
            }
        }
    }

    fun onAddWithPin(pin: String) {
        viewModelScope.launch {
            if (!settingsRepository.verifyPin(pin)) {
                _events.emit(NotFoundEvent.ShowMessage("PIN hatali"))
                return@launch
            }
            _events.emit(NotFoundEvent.NavigateAddProduct(_uiState.value.barcode))
        }
    }
}
