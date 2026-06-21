package com.payalert.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.onboardingDataStore by preferencesDataStore(name = "payalert_onboarding")

class OnboardingRepository(private val context: Context) {
    val hasSeenOnboarding: Flow<Boolean> = context.onboardingDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.hasSeenOnboarding] ?: false
        }

    suspend fun setHasSeenOnboarding(value: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[Keys.hasSeenOnboarding] = value
        }
    }

    private object Keys {
        val hasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
    }
}
