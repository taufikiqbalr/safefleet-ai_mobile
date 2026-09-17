package com.safefleet.ai.mobile.security

interface CredentialVault {
    fun saveDeviceCredential(credential: DeviceCredential)
    fun readDeviceCredential(): DeviceCredential?
    fun clearDeviceCredential()
}

data class DeviceCredential(
    val deviceId: String,
    val deviceKey: String,
)
