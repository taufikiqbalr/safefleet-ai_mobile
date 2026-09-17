package com.safefleet.ai.mobile.data.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoundationHealthRepository @Inject constructor(
    private val backendApi: SafeFleetBackendApi,
) {
    suspend fun isBackendReachable(): Boolean = runCatching {
        backendApi.liveness().isSuccessful
    }.getOrDefault(false)
}
