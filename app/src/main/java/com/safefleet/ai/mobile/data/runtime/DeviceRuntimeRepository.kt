package com.safefleet.ai.mobile.data.runtime

import com.safefleet.ai.mobile.data.network.CompleteDeviceTripRequest
import com.safefleet.ai.mobile.data.network.DeviceContextResponse
import com.safefleet.ai.mobile.data.network.DeviceTripResponse
import com.safefleet.ai.mobile.data.network.SafeFleetBackendApi
import com.safefleet.ai.mobile.data.network.StartDeviceTripRequest
import com.safefleet.ai.mobile.data.preferences.AppPreferences
import com.safefleet.ai.mobile.data.preferences.PendingTripCompletion
import com.safefleet.ai.mobile.security.CredentialVault
import com.safefleet.ai.mobile.security.DeviceCredential
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRuntimeRepository @Inject constructor(
    private val api: SafeFleetBackendApi,
    private val credentialVault: CredentialVault,
    private val preferences: AppPreferences,
) {
    suspend fun installationId(): String = preferences.getOrCreateInstallationId()

    fun isPaired(): Boolean = credentialVault.readDeviceCredential() != null

    suspend fun pair(rawPayload: String): DeviceContextResponse {
        val credential = PairingPayloadParser.parse(rawPayload)
        val context = fetchContext(credential)
        require(context.device.id == credential.deviceId) {
            "Backend context does not match the pairing credential"
        }
        credentialVault.saveDeviceCredential(credential)
        preferences.clearTripRetryState()
        return context
    }

    suspend fun context(): DeviceContextResponse = fetchContext(requireCredential())

    suspend fun startTrip(): DeviceTripResponse {
        val credential = requireCredential()
        val existingClientTripId = preferences.getPendingStartClientTripId()
        val clientTripId = existingClientTripId ?: UUID.randomUUID().toString().also {
            preferences.setPendingStartClientTripId(it)
        }
        val trip = api.startDeviceTrip(
            deviceId = credential.deviceId,
            deviceKey = credential.deviceKey,
            request = StartDeviceTripRequest(
                clientTripId = clientTripId,
                startedAt = Instant.now().toString(),
            ),
        )
        preferences.clearPendingStartClientTripId()
        return trip
    }

    suspend fun completeTrip(tripId: String): DeviceTripResponse {
        val credential = requireCredential()
        val existing = preferences.getPendingTripCompletion()
        val completion = if (existing?.tripId == tripId) {
            existing
        } else {
            PendingTripCompletion(
                tripId = tripId,
                endedAt = Instant.now().toString(),
            ).also { preferences.setPendingTripCompletion(it) }
        }

        val trip = api.completeDeviceTrip(
            deviceId = credential.deviceId,
            deviceKey = credential.deviceKey,
            tripId = tripId,
            request = CompleteDeviceTripRequest(endedAt = completion.endedAt),
        )
        preferences.clearPendingTripCompletion()
        return trip
    }

    suspend fun unpair() {
        credentialVault.clearDeviceCredential()
        preferences.clearTripRetryState()
    }

    private suspend fun fetchContext(credential: DeviceCredential): DeviceContextResponse =
        api.deviceContext(
            deviceId = credential.deviceId,
            deviceKey = credential.deviceKey,
        )

    private fun requireCredential(): DeviceCredential =
        credentialVault.readDeviceCredential()
            ?: error("This installation is not paired with a SafeFleet device")
}
