package com.marketpos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketpos.BuildConfig
import com.marketpos.domain.model.SupportTicketDetail
import com.marketpos.domain.model.SupportTicketSummary
import com.marketpos.domain.model.SupportTicketType
import com.marketpos.domain.repository.AccountSessionRepository
import com.marketpos.domain.repository.ActivationRepository
import com.marketpos.domain.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupportUiState(
    val isLoadingInbox: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val tickets: List<SupportTicketSummary> = emptyList(),
    val selectedTicketId: Long? = null,
    val selectedTicket: SupportTicketDetail? = null,
    val statusFilter: String = "",
    val isCreateMode: Boolean = false,
    val createType: SupportTicketType = SupportTicketType.BUG,
    val createTitle: String = "",
    val createDescription: String = "",
    val replyMessage: String = "",
    val error: String? = null
)

sealed interface SupportEvent {
    data class ShowMessage(val message: String) : SupportEvent
}

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository,
    private val accountSessionRepository: AccountSessionRepository,
    private val activationRepository: ActivationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SupportEvent>()
    val events: SharedFlow<SupportEvent> = _events.asSharedFlow()

    init {
        refreshInbox(keepSelection = false)
    }

    fun refreshInbox(keepSelection: Boolean = true) {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(error = "Destek islemleri icin kayitli kullanici oturumu gerekli.")
                _events.emit(SupportEvent.ShowMessage("Destek islemleri icin giris yapin."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoadingInbox = true, error = null)
            supportRepository.fetchInbox(
                accessToken = accessToken,
                status = _uiState.value.statusFilter.ifBlank { null }
            ).onSuccess { tickets ->
                val selectedTicketId = when {
                    keepSelection && _uiState.value.selectedTicketId != null &&
                        tickets.any { it.ticketId == _uiState.value.selectedTicketId } -> _uiState.value.selectedTicketId
                    tickets.isNotEmpty() -> tickets.first().ticketId
                    else -> null
                }

                _uiState.value = _uiState.value.copy(
                    isLoadingInbox = false,
                    tickets = tickets,
                    selectedTicketId = selectedTicketId,
                    selectedTicket = if (selectedTicketId == null) null else _uiState.value.selectedTicket
                )

                if (selectedTicketId != null) {
                    loadTicket(selectedTicketId)
                } else {
                    _uiState.value = _uiState.value.copy(selectedTicket = null)
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingInbox = false,
                    error = error.message ?: "Ticket kutusu yuklenemedi."
                )
                _events.emit(SupportEvent.ShowMessage(error.message ?: "Ticket kutusu yuklenemedi."))
            }
        }
    }

    fun updateStatusFilter(value: String) {
        _uiState.value = _uiState.value.copy(statusFilter = value)
        refreshInbox(keepSelection = false)
    }

    fun selectTicket(ticketId: Long) {
        _uiState.value = _uiState.value.copy(selectedTicketId = ticketId, isCreateMode = false)
        loadTicket(ticketId)
    }

    fun startCreateMode() {
        _uiState.value = _uiState.value.copy(
            isCreateMode = true,
            createType = SupportTicketType.BUG,
            createTitle = "",
            createDescription = "",
            error = null
        )
    }

    fun cancelCreateMode() {
        _uiState.value = _uiState.value.copy(isCreateMode = false)
    }

    fun updateCreateType(type: SupportTicketType) {
        _uiState.value = _uiState.value.copy(createType = type)
    }

    fun updateCreateTitle(value: String) {
        _uiState.value = _uiState.value.copy(createTitle = value.take(191))
    }

    fun updateCreateDescription(value: String) {
        _uiState.value = _uiState.value.copy(createDescription = value.take(20000))
    }

    fun submitCreate() {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                _events.emit(SupportEvent.ShowMessage("Destek islemleri icin giris yapin."))
                return@launch
            }

            val title = _uiState.value.createTitle.trim()
            val description = _uiState.value.createDescription.trim()
            if (title.length < 3 || description.length < 5) {
                _events.emit(SupportEvent.ShowMessage("Baslik ve aciklama zorunludur."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            val activationState = activationRepository.getActivationState()
            supportRepository.createTicket(
                accessToken = accessToken,
                type = _uiState.value.createType.key,
                title = title,
                description = description,
                source = "mobile",
                companyCode = activationState.companyCode,
                deviceUid = activationState.deviceUid,
                appVersion = BuildConfig.VERSION_NAME
            ).onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isCreateMode = false,
                    selectedTicketId = detail.ticketId,
                    selectedTicket = detail,
                    createTitle = "",
                    createDescription = ""
                )
                _events.emit(SupportEvent.ShowMessage("Ticket olusturuldu."))
                refreshInbox(keepSelection = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = error.message ?: "Ticket olusturulamadi."
                )
                _events.emit(SupportEvent.ShowMessage(error.message ?: "Ticket olusturulamadi."))
            }
        }
    }

    fun updateReplyMessage(value: String) {
        _uiState.value = _uiState.value.copy(replyMessage = value.take(20000))
    }

    fun sendReply() {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken()
            val ticketId = _uiState.value.selectedTicketId
            val message = _uiState.value.replyMessage.trim()
            if (accessToken.isNullOrBlank() || ticketId == null) {
                _events.emit(SupportEvent.ShowMessage("Once ticket secin."))
                return@launch
            }
            if (message.length < 2) {
                _events.emit(SupportEvent.ShowMessage("Yanit metni bos olamaz."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            supportRepository.replyTicket(
                accessToken = accessToken,
                ticketId = ticketId,
                message = message
            ).onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    selectedTicket = detail,
                    replyMessage = ""
                )
                _events.emit(SupportEvent.ShowMessage("Yanıt gonderildi."))
                refreshInbox(keepSelection = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = error.message ?: "Yanit gonderilemedi."
                )
                _events.emit(SupportEvent.ShowMessage(error.message ?: "Yanit gonderilemedi."))
            }
        }
    }

    fun reopenTicket() {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken()
            val ticketId = _uiState.value.selectedTicketId
            if (accessToken.isNullOrBlank() || ticketId == null) {
                _events.emit(SupportEvent.ShowMessage("Once ticket secin."))
                return@launch
            }

            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            supportRepository.reopenTicket(accessToken = accessToken, ticketId = ticketId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        selectedTicket = detail
                    )
                    _events.emit(SupportEvent.ShowMessage("Ticket yeniden acildi."))
                    refreshInbox(keepSelection = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = error.message ?: "Ticket yeniden acilamadi."
                    )
                    _events.emit(SupportEvent.ShowMessage(error.message ?: "Ticket yeniden acilamadi."))
                }
        }
    }

    private fun loadTicket(ticketId: Long) {
        viewModelScope.launch {
            val accessToken = accountSessionRepository.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoadingDetail = true)
            supportRepository.fetchTicket(accessToken = accessToken, ticketId = ticketId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        selectedTicket = detail,
                        selectedTicketId = detail.ticketId
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDetail = false,
                        error = error.message ?: "Ticket detayi yuklenemedi."
                    )
                    _events.emit(SupportEvent.ShowMessage(error.message ?: "Ticket detayi yuklenemedi."))
                }
        }
    }
}

