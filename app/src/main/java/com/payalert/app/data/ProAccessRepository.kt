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

private val Context.proAccessDataStore by preferencesDataStore(name = "payalert_pro_access")

object Monetization {
    const val freeCardsLimit = 3
    const val proPriceLabel = "$149 MXN pago unico"
}

class ProAccessRepository(private val context: Context) {
    val isPro: Flow<Boolean> = context.proAccessDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.previewUnlocked] ?: false
        }

    suspend fun setPreviewUnlocked(value: Boolean) {
        context.proAccessDataStore.edit { preferences ->
            preferences[Keys.previewUnlocked] = value
        }
    }

    private object Keys {
        val previewUnlocked = booleanPreferencesKey("pro_preview_unlocked")
    }
}
