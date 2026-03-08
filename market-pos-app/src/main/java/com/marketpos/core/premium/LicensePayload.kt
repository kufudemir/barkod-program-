package com.marketpos.core.premium

import com.marketpos.domain.model.AppTier

data class LicensePayload(
    val tier: AppTier,
    val deviceCode: String,
    val issuedAt: Long,
    val expiresAt: Long?,
    val nonce: String
)
