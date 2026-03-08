package com.marketpos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.domain.repository.AccountSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isRequestingCode: Boolean = false,
    val isResetting: Boolean = false,
    val expiresAt: Long? = null,
    val error: String? = null,
    val showSuccessDialog: Boolean = false
)

sealed interface ForgotPasswordEvent {
    data class ShowMessage(val message: String) : ForgotPasswordEvent
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val accountSessionRepository: AccountSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun updateCode(value: String) {
        _uiState.value = _uiState.value.copy(code = value.filter(Char::isDigit).take(6), error = null)
    }

    fun updateNewPassword(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, error = null)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun requestCode() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            emitMessage("E-posta gerekli")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequestingCode = true, error = null)
            accountSessionRepository.requestPasswordReset(email)
                .onSuccess { expiresAt ->
                    _uiState.value = _uiState.value.copy(isRequestingCode = false, expiresAt = expiresAt)
                    _events.emit(ForgotPasswordEvent.ShowMessage("Şifre sıfırlama kodu gönderildi"))
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isRequestingCode = false, error = it.message ?: "Kod gönderilemedi")
                }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank()) {
            emitMessage("E-posta gerekli")
            return
        }
        if (state.code.length != 6) {
            emitMessage("6 haneli kodu girin")
            return
        }
        if (state.newPassword.length < 8) {
            emitMessage("Yeni şifre en az 8 karakter olmalı")
            return
        }
        if (state.newPassword != state.confirmPassword) {
            emitMessage("Yeni şifreler eşleşmiyor")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResetting = true, error = null)
            accountSessionRepository.resetPassword(email, state.code, state.newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isResetting = false)
                    _uiState.value = _uiState.value.copy(
                        isResetting = false,
                        showSuccessDialog = true,
                        error = null
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isResetting = false, error = it.message ?: "Şifre sıfırlanamadı")
                }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    private fun emitMessage(message: String) {
        viewModelScope.launch {
            _events.emit(ForgotPasswordEvent.ShowMessage(message))
        }
    }
}

