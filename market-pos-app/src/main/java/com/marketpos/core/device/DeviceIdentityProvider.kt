package com.marketpos.core.device

interface DeviceIdentityProvider {
    fun getDeviceUid(): String
    fun getDeviceName(): String
}
