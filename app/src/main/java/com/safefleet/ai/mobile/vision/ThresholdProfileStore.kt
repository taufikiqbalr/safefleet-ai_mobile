package com.safefleet.ai.mobile.vision

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.visionProfileDataStore by preferencesDataStore(name = "safefleet_vision_profile")

@Singleton
class ThresholdProfileStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) {
    private val activeProfileJsonKey = stringPreferencesKey("active_threshold_profile_json")

    val activeProfile: Flow<ThresholdProfile?> = context.visionProfileDataStore.data
        .map { preferences ->
            preferences[activeProfileJsonKey]?.let { json ->
                runCatching {
                    ThresholdProfileValidator.validate(
                        gson.fromJson(json, ThresholdProfile::class.java),
                    )
                }.getOrNull()
            }
        }

    suspend fun save(profile: ThresholdProfile) {
        val validated = ThresholdProfileValidator.validate(profile)
        context.visionProfileDataStore.edit { preferences ->
            preferences[activeProfileJsonKey] = gson.toJson(validated)
        }
    }

    suspend fun clear() {
        context.visionProfileDataStore.edit { preferences ->
            preferences.remove(activeProfileJsonKey)
        }
    }
}
