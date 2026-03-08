package com.marketpos.domain.model

data class LegalConsentState(
    val currentVersion: String,
    val acceptedVersion: String? = null,
    val acceptedAt: Long? = null
) {
    val isAccepted: Boolean
        get() = acceptedVersion == currentVersion && acceptedAt != null
}
