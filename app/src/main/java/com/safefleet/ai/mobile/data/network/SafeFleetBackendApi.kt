package com.safefleet.ai.mobile.data.network

import retrofit2.Response
import retrofit2.http.GET

interface SafeFleetBackendApi {
    @GET("health/live")
    suspend fun liveness(): Response<Unit>
}
