package com.marketpos.domain.model

data class PremiumState(
    val tier: AppTier = AppTier.FREE,
    val source: PremiumSource = PremiumSource.NONE,
    val deviceCode: String = "",
    val activatedAt: Long? = null,
    val expiresAt: Long? = null,
    val licenseCodeMasked: String? = null,
    val trialUsed: Boolean = false
) {
    val isPro: Boolean get() = tier == AppTier.PRO
}
