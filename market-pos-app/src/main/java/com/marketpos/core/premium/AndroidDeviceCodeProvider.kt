package com.marketpos.core.premium

import android.content.Context
import android.provider.Settings
import com.marketpos.core.util.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceCodeProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceCodeProvider {

    override fun getDeviceCode(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty()

        val raw = "$androidId:${context.packageName}:marketpos-premium-v1"
        val hex = SecurityUtils.sha256Hex(raw).uppercase()
        return hex
            .take(20)
            .chunked(4)
            .joinToString("-")
    }
}
