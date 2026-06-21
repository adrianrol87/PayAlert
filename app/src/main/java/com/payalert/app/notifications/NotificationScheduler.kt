package com.payalert.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.payalert.app.data.CreditCardItem
import com.payalert.app.data.NotificationSettings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun rescheduleAll(cards: List<CreditCardItem>, settings: NotificationSettings) {
        workManager.cancelAllWorkByTag(NotificationWorker.ALL_NOTIFICATIONS_TAG)

        cards.forEach { item ->
            if (!item.isPaid) {
                if (settings.dueEnabled) {
                    settings.dueDaysBefore.forEach { daysBefore ->
                        scheduleReminder(
                            uniqueName = "due_${item.id}_$daysBefore",
                            cardId = item.id,
                            title = "PayAlert",
                            body = dueNotificationBody(item, daysBefore, settings.includeAmount),
                            whenToSend = dateAtConfiguredTime(item.dueDate.minusDays(daysBefore.toLong()), settings),
                            kind = "due",
                            quickActionsEnabled = settings.quickActionsEnabled,
                        )
                    }

                    if (settings.sameDayFollowUpEnabled) {
                        scheduleReminder(
                            uniqueName = "due_followup_${item.id}",
                            cardId = item.id,
                            title = "PayAlert",
                            body = "Tu pago sigue pendiente hoy: ${item.bankName} ${item.cardType} • ${item.lastDigits}${amountSuffix(item, settings.includeAmount)}",
                            whenToSend = LocalDateTime.of(item.dueDate, LocalTime.of(18, 0)),
                            kind = "due_followup",
                            quickActionsEnabled = settings.quickActionsEnabled,
                        )
                    }
                }

                if (settings.cutEnabled) {
                    settings.cutDaysBefore.forEach { daysBefore ->
                        scheduleReminder(
                            uniqueName = "cut_${item.id}_$daysBefore",
                            cardId = item.id,
                            title = "PayAlert",
                            body = cutNotificationBody(item, daysBefore, settings.includeAmount),
                            whenToSend = dateAtConfiguredTime(item.cutDate.minusDays(daysBefore.toLong()), settings),
                            kind = "cut",
                            quickActionsEnabled = false,
                        )
                    }
                }
            }
        }

        if (settings.weeklySummaryEnabled) {
            scheduleWeeklySummary(cards, settings)
        }
    }

    fun scheduleReminderTomorrow(item: CreditCardItem, settings: NotificationSettings) {
        scheduleReminder(
            uniqueName = "quick_reminder_${item.id}",
            cardId = item.id,
            title = "PayAlert",
            body = "Recordatorio pendiente: ${item.bankName} ${item.cardType} • ${item.lastDigits}${amountSuffix(item, settings.includeAmount)}",
            whenToSend = LocalDateTime.of(
                LocalDate.now().plusDays(1),
                LocalTime.of(settings.notificationHour, settings.notificationMinute),
            ),
            kind = "quick_reminder",
            quickActionsEnabled = settings.quickActionsEnabled,
        )
    }

    private fun scheduleWeeklySummary(cards: List<CreditCardItem>, settings: NotificationSettings) {
        val upcoming = cards
            .filter { !it.isPaid && !it.dueDate.isBefore(LocalDate.now()) && it.daysRemaining <= 7L }
            .sortedBy { it.dueDate }

        if (upcoming.isEmpty()) return

        val nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY))
        val candidateDateTime = dateAtConfiguredTime(nextDate, settings)
        val nextDateTime = if (candidateDateTime.isAfter(LocalDateTime.now())) {
            candidateDateTime
        } else {
            candidateDateTime.plusWeeks(1)
        }
        val nextItem = upcoming.first()
        val body = "Esta semana vencen ${upcoming.size} tarjeta(s). Proxima: ${nextItem.bankName} ${nextItem.cardType} • ${nextItem.lastDigits} el ${nextItem.dueDate}${amountSuffix(nextItem, settings.includeAmount)}"

        scheduleReminder(
            uniqueName = "weekly_summary",
            cardId = "weekly_summary",
            title = "Resumen semanal",
            body = body,
            whenToSend = nextDateTime,
            kind = "weekly",
            quickActionsEnabled = false,
        )
    }

    private fun scheduleReminder(
        uniqueName: String,
        cardId: String,
        title: String,
        body: String,
        whenToSend: LocalDateTime,
        kind: String,
        quickActionsEnabled: Boolean,
    ) {
        val now = LocalDateTime.now()
        if (!whenToSend.isAfter(now)) return

        val delay = Duration.between(now, whenToSend).toMillis().coerceAtLeast(1_000L)
        val input = Data.Builder()
            .putString(NotificationWorker.KEY_TITLE, title)
            .putString(NotificationWorker.KEY_BODY, body)
            .putString(NotificationWorker.KEY_CARD_ID, cardId)
            .putString(NotificationWorker.KEY_KIND, kind)
            .putBoolean(NotificationWorker.KEY_QUICK_ACTIONS_ENABLED, quickActionsEnabled)
            .build()

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(input)
            .addTag(NotificationWorker.ALL_NOTIFICATIONS_TAG)
            .build()

        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun dateAtConfiguredTime(date: LocalDate, settings: NotificationSettings): LocalDateTime {
        return LocalDateTime.of(date, LocalTime.of(settings.notificationHour, settings.notificationMinute))
    }

    private fun dueNotificationBody(item: CreditCardItem, daysBefore: Int, includeAmount: Boolean): String {
        val prefix = if (daysBefore == 0) {
            "Hoy vence tu tarjeta"
        } else {
            "En $daysBefore dia(s) vence tu tarjeta"
        }
        return "$prefix ${item.bankName} ${item.cardType} • ${item.lastDigits}${amountSuffix(item, includeAmount)}"
    }

    private fun cutNotificationBody(item: CreditCardItem, daysBefore: Int, includeAmount: Boolean): String {
        val prefix = if (daysBefore == 0) {
            "Hoy es tu fecha de corte en"
        } else {
            "En $daysBefore dia(s) llega tu corte en"
        }
        return "$prefix ${item.bankName} ${item.cardType} • ${item.lastDigits}${amountSuffix(item, includeAmount)}"
    }

    private fun amountSuffix(item: CreditCardItem, includeAmount: Boolean): String {
        if (!includeAmount) return ""
        val amount = item.totalDebt ?: return ""
        if (amount <= 0.0) return ""
        return ". Monto: $${"%.2f".format(amount)}"
    }
}
