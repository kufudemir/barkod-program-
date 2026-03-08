package com.marketpos.feature.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.BuildConfig
import com.marketpos.domain.model.AppMode
import com.marketpos.domain.model.AppUpdateInfo
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.AppUpdateRepository
import com.marketpos.domain.repository.LegalConsentRepository
import com.marketpos.domain.repository.SettingsRepository
import com.marketpos.domain.usecase.FlushSyncQueueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppBootstrapUiState(
    val pendingUpdate: AppUpdateInfo? = null,
    val requiresLegalConsent: Boolean = false,
    val legalConsentAccepted: Boolean = false
)

sealed interface AppBootstrapEvent {
    data object NavigateSessionEntry : AppBootstrapEvent
    data object NavigateActivation : AppBootstrapEvent
    data object NavigateModeSelection : AppBootstrapEvent
}

@HiltViewModel
class AppBootstrapViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val activationRepository: ActivationRepository,
    private val appUpdateRepository: AppUpdateRepository,
    private val legalConsentRepository: LegalConsentRepository,
    private val flushSyncQueueUseCase: FlushSyncQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppBootstrapUiState())
    val uiState: StateFlow<AppBootstrapUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AppBootstrapEvent>()
    val events: SharedFlow<AppBootstrapEvent> = _events.asSharedFlow()

    private var bootstrapResolved = false

    init {
        viewModelScope.launch {
            settingsRepository.initializeDefaultsIfNeeded()
            val latestUpdate = appUpdateRepository.getLatestAvailableUpdate(BuildConfig.VERSION_CODE)
            if (latestUpdate != null) {
                _uiState.update { it.copy(pendingUpdate = latestUpdate) }
                return@launch
            }
            continueBootstrap()
        }
    }

    fun skipUpdateAndContinue() {
        _uiState.update { it.copy(pendingUpdate = null) }
        viewModelScope.launch {
            continueBootstrap()
        }
    }

    fun continueAfterOpeningUpdateLink() {
        if (_uiState.value.pendingUpdate?.forceUpdate == true) {
            return
        }
        skipUpdateAndContinue()
    }

    fun acceptLegalConsent() {
        viewModelScope.launch {
            legalConsentRepository.acceptCurrentVersion()
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            requiresLegalConsent = false,
                            legalConsentAccepted = true
                        )
                    }
                    continueBootstrap()
                }
        }
    }

    private suspend fun continueBootstrap() {
        if (bootstrapResolved) return
        val consentState = legalConsentRepository.getState()
        if (!consentState.isAccepted) {
            _uiState.update {
                it.copy(
                    requiresLegalConsent = true,
                    legalConsentAccepted = false
                )
            }
            return
        }
        bootstrapResolved = true

        val session = accountSessionRepository.getState()
        if (!session.hasChosenSession) {
            _events.emit(AppBootstrapEvent.NavigateSessionEntry)
            return
        }

        val activation = activationRepository.getActivationState()
        if (activation.isActivated) {
            if (settingsRepository.getMode() == AppMode.ADMIN) {
                settingsRepository.setMode(AppMode.CASHIER)
            }
            flushSyncQueueUseCase()
            _events.emit(AppBootstrapEvent.NavigateModeSelection)
        } else {
            _events.emit(AppBootstrapEvent.NavigateActivation)
        }
    }
}
