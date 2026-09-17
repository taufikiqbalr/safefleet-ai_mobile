package com.safefleet.ai.mobile.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.safeFleetDataStore by preferencesDataStore(name = "safefleet_preferences")

data class PendingTripCompletion(
    val tripId: String,
    val endedAt: String,
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val installationIdKey = stringPreferencesKey("installation_id")
    private val pendingStartClientTripIdKey = stringPreferencesKey("pending_start_client_trip_id")
    private val pendingCompleteTripIdKey = stringPreferencesKey("pending_complete_trip_id")
    private val pendingCompleteEndedAtKey = stringPreferencesKey("pending_complete_ended_at")

    val onboardingCompleted: Flow<Boolean> = context.safeFleetDataStore.data
        .map { preferences -> preferences[onboardingCompletedKey] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.safeFleetDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }

    suspend fun getOrCreateInstallationId(): String {
        val generated = UUID.randomUUID().toString()
        var resolved = generated
        context.safeFleetDataStore.edit { preferences ->
            val existing = preferences[installationIdKey]
            if (existing.isNullOrBlank()) {
                preferences[installationIdKey] = generated
            } else {
                resolved = existing
            }
        }
        return resolved
    }

    suspend fun getPendingStartClientTripId(): String? =
        context.safeFleetDataStore.data.first()[pendingStartClientTripIdKey]

    suspend fun setPendingStartClientTripId(clientTripId: String) {
        context.safeFleetDataStore.edit { preferences ->
            preferences[pendingStartClientTripIdKey] = clientTripId
        }
    }

    suspend fun clearPendingStartClientTripId() {
        context.safeFleetDataStore.edit { preferences ->
            preferences.remove(pendingStartClientTripIdKey)
        }
    }

    suspend fun getPendingTripCompletion(): PendingTripCompletion? {
        val preferences = context.safeFleetDataStore.data.first()
        val tripId = preferences[pendingCompleteTripIdKey] ?: return null
        val endedAt = preferences[pendingCompleteEndedAtKey] ?: return null
        return PendingTripCompletion(tripId = tripId, endedAt = endedAt)
    }

    suspend fun setPendingTripCompletion(completion: PendingTripCompletion) {
        context.safeFleetDataStore.edit { preferences ->
            preferences[pendingCompleteTripIdKey] = completion.tripId
            preferences[pendingCompleteEndedAtKey] = completion.endedAt
        }
    }

    suspend fun clearPendingTripCompletion() {
        context.safeFleetDataStore.edit { preferences ->
            preferences.remove(pendingCompleteTripIdKey)
            preferences.remove(pendingCompleteEndedAtKey)
        }
    }

    suspend fun clearTripRetryState() {
        clearPendingStartClientTripId()
        clearPendingTripCompletion()
    }
}
