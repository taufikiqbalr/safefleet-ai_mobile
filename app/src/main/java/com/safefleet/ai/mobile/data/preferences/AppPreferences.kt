package com.safefleet.ai.mobile.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.safeFleetDataStore by preferencesDataStore(name = "safefleet_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val onboardingCompleted: Flow<Boolean> = context.safeFleetDataStore.data
        .map { preferences -> preferences[onboardingCompletedKey] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.safeFleetDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }
}
