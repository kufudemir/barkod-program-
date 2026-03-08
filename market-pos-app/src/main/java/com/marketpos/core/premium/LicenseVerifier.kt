package com.marketpos.core.premium

interface LicenseVerifier {
    fun verify(licenseCode: String): Result<LicensePayload>
}
