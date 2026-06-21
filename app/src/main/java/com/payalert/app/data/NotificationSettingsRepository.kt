package com.payalert.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.notificationSettingsDataStore by preferencesDataStore(name = "payalert_notification_settings")

class NotificationSettingsRepository(private val context: Context) {
    val settings: Flow<NotificationSettings> = context.notificationSettingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            NotificationSettings(
                dueEnabled = preferences[Keys.dueEnabled] ?: true,
                cutEnabled = preferences[Keys.cutEnabled] ?: true,
                includeAmount = preferences[Keys.includeAmount] ?: true,
                quickActionsEnabled = preferences[Keys.quickActionsEnabled] ?: true,
                weeklySummaryEnabled = preferences[Keys.weeklySummaryEnabled] ?: false,
                sameDayFollowUpEnabled = preferences[Keys.sameDayFollowUpEnabled] ?: true,
                dueSameDay = preferences[Keys.dueSameDay] ?: true,
                due1Day = preferences[Keys.due1Day] ?: true,
                due2Days = preferences[Keys.due2Days] ?: false,
                due3Days = preferences[Keys.due3Days] ?: true,
                due5Days = preferences[Keys.due5Days] ?: false,
                due7Days = preferences[Keys.due7Days] ?: false,
                cutSameDay = preferences[Keys.cutSameDay] ?: true,
                cut1Day = preferences[Keys.cut1Day] ?: true,
                cut2Days = preferences[Keys.cut2Days] ?: false,
                cut3Days = preferences[Keys.cut3Days] ?: true,
                cut5Days = preferences[Keys.cut5Days] ?: false,
                cut7Days = preferences[Keys.cut7Days] ?: false,
                notificationHour = preferences[Keys.notificationHour] ?: 9,
                notificationMinute = preferences[Keys.notificationMinute] ?: 0,
            )
        }

    suspend fun save(settings: NotificationSettings) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[Keys.dueEnabled] = settings.dueEnabled
            preferences[Keys.cutEnabled] = settings.cutEnabled
            preferences[Keys.includeAmount] = settings.includeAmount
            preferences[Keys.quickActionsEnabled] = settings.quickActionsEnabled
            preferences[Keys.weeklySummaryEnabled] = settings.weeklySummaryEnabled
            preferences[Keys.sameDayFollowUpEnabled] = settings.sameDayFollowUpEnabled
            preferences[Keys.dueSameDay] = settings.dueSameDay
            preferences[Keys.due1Day] = settings.due1Day
            preferences[Keys.due2Days] = settings.due2Days
            preferences[Keys.due3Days] = settings.due3Days
            preferences[Keys.due5Days] = settings.due5Days
            preferences[Keys.due7Days] = settings.due7Days
            preferences[Keys.cutSameDay] = settings.cutSameDay
            preferences[Keys.cut1Day] = settings.cut1Day
            preferences[Keys.cut2Days] = settings.cut2Days
            preferences[Keys.cut3Days] = settings.cut3Days
            preferences[Keys.cut5Days] = settings.cut5Days
            preferences[Keys.cut7Days] = settings.cut7Days
            preferences[Keys.notificationHour] = settings.notificationHour
            preferences[Keys.notificationMinute] = settings.notificationMinute
        }
    }

    suspend fun getSettings(): NotificationSettings = settings.first()

    private object Keys {
        val dueEnabled = booleanPreferencesKey("due_enabled")
        val cutEnabled = booleanPreferencesKey("cut_enabled")
        val includeAmount = booleanPreferencesKey("include_amount")
        val quickActionsEnabled = booleanPreferencesKey("quick_actions_enabled")
        val weeklySummaryEnabled = booleanPreferencesKey("weekly_summary_enabled")
        val sameDayFollowUpEnabled = booleanPreferencesKey("same_day_follow_up_enabled")
        val dueSameDay = booleanPreferencesKey("due_same_day")
        val due1Day = booleanPreferencesKey("due_1_day")
        val due2Days = booleanPreferencesKey("due_2_days")
        val due3Days = booleanPreferencesKey("due_3_days")
        val due5Days = booleanPreferencesKey("due_5_days")
        val due7Days = booleanPreferencesKey("due_7_days")
        val cutSameDay = booleanPreferencesKey("cut_same_day")
        val cut1Day = booleanPreferencesKey("cut_1_day")
        val cut2Days = booleanPreferencesKey("cut_2_days")
        val cut3Days = booleanPreferencesKey("cut_3_days")
        val cut5Days = booleanPreferencesKey("cut_5_days")
        val cut7Days = booleanPreferencesKey("cut_7_days")
        val notificationHour = intPreferencesKey("notification_hour")
        val notificationMinute = intPreferencesKey("notification_minute")
    }
}
