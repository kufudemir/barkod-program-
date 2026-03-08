package com.marketpos.domain.model

data class CompanyActivationState(
    val isActivated: Boolean = false,
    val companyId: Long? = null,
    val companyName: String? = null,
    val companyCode: String? = null,
    val deviceUid: String? = null,
    val deviceName: String? = null,
    val activationToken: String? = null,
    val lastSyncSuccessAt: Long? = null,
    val lastSyncError: String? = null
)
