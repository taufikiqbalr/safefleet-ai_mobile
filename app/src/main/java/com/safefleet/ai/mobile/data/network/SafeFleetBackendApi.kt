package com.safefleet.ai.mobile.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SafeFleetBackendApi {
    @GET("health/live")
    suspend fun liveness(): Response<Unit>

    @GET("device/context")
    suspend fun deviceContext(
        @Header("X-SafeFleet-Device-Id") deviceId: String,
        @Header("X-SafeFleet-Device-Key") deviceKey: String,
    ): DeviceContextResponse

    @POST("device/trips/start")
    suspend fun startDeviceTrip(
        @Header("X-SafeFleet-Device-Id") deviceId: String,
        @Header("X-SafeFleet-Device-Key") deviceKey: String,
        @Body request: StartDeviceTripRequest,
    ): DeviceTripResponse

    @POST("device/trips/{tripId}/complete")
    suspend fun completeDeviceTrip(
        @Header("X-SafeFleet-Device-Id") deviceId: String,
        @Header("X-SafeFleet-Device-Key") deviceKey: String,
        @Path("tripId") tripId: String,
        @Body request: CompleteDeviceTripRequest,
    ): DeviceTripResponse
}
