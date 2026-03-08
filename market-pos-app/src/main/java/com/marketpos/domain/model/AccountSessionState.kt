package com.marketpos.domain.model

data class AccountSessionState(
    val type: AccountSessionType = AccountSessionType.NONE,
    val userId: Long? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val authToken: String? = null
) {
    val hasChosenSession: Boolean get() = type != AccountSessionType.NONE
    val isGuest: Boolean get() = type == AccountSessionType.GUEST
    val isRegistered: Boolean get() = type == AccountSessionType.REGISTERED && !authToken.isNullOrBlank()
}
