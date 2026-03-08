package com.marketpos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.repository.AccountSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface SessionEntryEvent {
    data object NavigateActivation : SessionEntryEvent
    data class ShowMessage(val message: String) : SessionEntryEvent
}

@HiltViewModel
class SessionEntryViewModel @Inject constructor(
    private val accountSessionRepository: AccountSessionRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<SessionEntryEvent>()
    val events: SharedFlow<SessionEntryEvent> = _events.asSharedFlow()

    fun continueAsGuest() {
        viewModelScope.launch {
            accountSessionRepository.continueAsGuest()
                .onSuccess { _events.emit(SessionEntryEvent.NavigateActivation) }
                .onFailure { _events.emit(SessionEntryEvent.ShowMessage(it.message ?: "Misafir oturumu açılamadı")) }
        }
    }
}
