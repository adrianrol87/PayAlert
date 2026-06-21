package com.payalert.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.payalert.app.data.CardsRepository
import com.payalert.app.data.NotificationSettingsRepository
import kotlinx.coroutines.runBlocking

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getStringExtra(EXTRA_CARD_ID).orEmpty()
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        if (cardId.isBlank()) return

        runBlocking {
            val cardsRepository = CardsRepository(context)
            val settingsRepository = NotificationSettingsRepository(context)
            val scheduler = NotificationScheduler(context)

            val cards = cardsRepository.getCards()
            val settings = settingsRepository.getSettings()

            when (intent.action) {
                ACTION_MARK_PAID -> {
                    val updated = cards.map { item ->
                        if (item.id == cardId) item.copy(isPaid = true, totalDebt = 0.0) else item
                    }
                    cardsRepository.saveCards(updated)
                    scheduler.rescheduleAll(updated, settings)
                }

                ACTION_REMIND_TOMORROW -> {
                    val item = cards.firstOrNull { it.id == cardId && !it.isPaid } ?: return@runBlocking
                    scheduler.scheduleReminderTomorrow(item, settings)
                }
            }
        }

        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object {
        const val ACTION_MARK_PAID = "com.payalert.app.action.MARK_PAID"
        const val ACTION_REMIND_TOMORROW = "com.payalert.app.action.REMIND_TOMORROW"
        const val EXTRA_CARD_ID = "extra_card_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
