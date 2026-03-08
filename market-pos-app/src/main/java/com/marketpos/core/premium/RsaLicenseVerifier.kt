package com.marketpos.core.premium

import android.content.Context
import com.marketpos.domain.model.AppTier
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class RsaLicenseVerifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceCodeProvider: DeviceCodeProvider
) : LicenseVerifier {

    private val publicKeys: List<Pair<PublicKey, String>> by lazy {
        val json = context.assets.open("premium_public_key.json")
            .bufferedReader()
            .use { it.readText().trim() }
        val keyJson = JSONObject(json)
        buildList {
            parseEcdsaPublicKey(keyJson)?.let { add(it to "SHA256withECDSA") }
            parseRsaPublicKey(keyJson)?.let { add(it to "SHA256withRSA") }
        }
    }

    override fun verify(licenseCode: String): Result<LicensePayload> {
        return runCatching {
            val parts = licenseCode.trim().filterNot(Char::isWhitespace).split(".")
            require(parts.size == 2) { "Lisans kodu formatı geçersiz" }

            val payloadBytes = Base64.getUrlDecoder().decode(parts[0])
            val signatureBytes = Base64.getUrlDecoder().decode(parts[1])

            val verified = publicKeys.any { (publicKey, algorithm) ->
                val verifier = Signature.getInstance(algorithm)
                verifier.initVerify(publicKey)
                verifier.update(payloadBytes)
                verifier.verify(signatureBytes)
            }
            require(verified) { "Lisans imzasi dogrulanamadi" }

            val payloadText = String(payloadBytes, Charsets.UTF_8)
            val payload = payloadText.toLicensePayload()

            require(payload.tier == AppTier.PRO) { "Bu lisans PRO degil" }
            require(payload.deviceCode.normalizeDeviceCode() == deviceCodeProvider.getDeviceCode().normalizeDeviceCode()) {
                "Bu lisans bu cihaza ait degil"
            }
            if (payload.expiresAt != null) {
                require(payload.expiresAt >= System.currentTimeMillis()) { "Lisans süresi dolmuş" }
            }
            payload
        }
    }

    private fun parseRsaPublicKey(json: JSONObject): PublicKey? {
        val rsaJson = json.optJSONObject("rsa")
        val modulusText = rsaJson?.optString("modulus") ?: json.optString("modulus")
        val exponentText = rsaJson?.optString("exponent") ?: json.optString("exponent")
        if (modulusText.isBlank() || exponentText.isBlank()) return null

        val modulus = modulusText.decodeBase64UrlToBigInteger()
        val exponent = exponentText.decodeBase64UrlToBigInteger()
        return KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(modulus, exponent))
    }

    private fun parseEcdsaPublicKey(json: JSONObject): PublicKey? {
        val ecJson = json.optJSONObject("ecdsa") ?: return null
        val curve = ecJson.optString("curve", "secp256r1")
        val x = ecJson.optString("x")
        val y = ecJson.optString("y")
        if (x.isBlank() || y.isBlank()) return null

        val parameters = AlgorithmParameters.getInstance("EC").apply {
            init(ECGenParameterSpec(curve))
        }.getParameterSpec(java.security.spec.ECParameterSpec::class.java)

        return KeyFactory.getInstance("EC").generatePublic(
            ECPublicKeySpec(
                ECPoint(
                    x.decodeBase64UrlToBigInteger(),
                    y.decodeBase64UrlToBigInteger()
                ),
                parameters
            )
        )
    }

    private fun String.decodeBase64UrlToBigInteger(): java.math.BigInteger {
        val normalized = replace('-', '+').replace('_', '/').let {
            it + "=".repeat((4 - it.length % 4) % 4)
        }
        val bytes = java.util.Base64.getDecoder().decode(normalized)
        return java.math.BigInteger(1, bytes)
    }

    private fun String.toLicensePayload(): LicensePayload {
        return if (startsWith("{")) {
            val json = JSONObject(this)
            LicensePayload(
                tier = AppTier.valueOf(json.getString("tier")),
                deviceCode = json.getString("deviceCode"),
                issuedAt = json.getLong("issuedAt"),
                expiresAt = if (json.has("expiresAt") && !json.isNull("expiresAt")) json.getLong("expiresAt") else null,
                nonce = json.optString("nonce", "")
            )
        } else {
            val parts = split("|")
            require(parts.size == 5) { "Lisans içeriği geçersiz" }
            LicensePayload(
                tier = when (parts[0]) {
                    "P" -> AppTier.PRO
                    else -> throw IllegalArgumentException("Lisans seviyesi bilinmiyor")
                },
                deviceCode = parts[1],
                issuedAt = parts[2].toLong(36),
                expiresAt = parts[3].takeIf { it != "-" }?.toLong(36),
                nonce = parts[4]
            )
        }
    }

    private fun String.normalizeDeviceCode(): String = uppercase().replace("-", "")
}

