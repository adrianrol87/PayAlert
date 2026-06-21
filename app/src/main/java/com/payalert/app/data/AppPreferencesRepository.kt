package com.payalert.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "payalert_app_preferences")

class AppPreferencesRepository(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")
    private val paidCountKey = intPreferencesKey("paid_cards_count")

    val isDarkMode: Flow<Boolean> = context.appPreferencesDataStore.data
        .map { preferences -> preferences[darkModeKey] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }

    suspend fun incrementPaidCount(): Int {
        var updatedCount = 0
        context.appPreferencesDataStore.edit { preferences ->
            updatedCount = (preferences[paidCountKey] ?: 0) + 1
            preferences[paidCountKey] = updatedCount
        }
        return updatedCount
    }
}
