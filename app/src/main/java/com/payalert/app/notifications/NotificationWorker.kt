package com.payalert.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.payalert.app.MainActivity

class NotificationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val title = inputData.getString(KEY_TITLE).orEmpty()
        val body = inputData.getString(KEY_BODY).orEmpty()
        val cardId = inputData.getString(KEY_CARD_ID).orEmpty()
        val kind = inputData.getString(KEY_KIND).orEmpty()
        val notificationId = "${cardId}_$kind".hashCode()
        val quickActionsEnabled = inputData.getBoolean(KEY_QUICK_ACTIONS_ENABLED, false)

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (quickActionsEnabled && cardId.isNotBlank()) {
            val markPaidIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_PAID
                putExtra(NotificationActionReceiver.EXTRA_CARD_ID, cardId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val remindTomorrowIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_REMIND_TOMORROW
                putExtra(NotificationActionReceiver.EXTRA_CARD_ID, cardId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }

            val markPaidPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationId + 1,
                markPaidIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val remindTomorrowPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationId + 2,
                remindTomorrowIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            builder.addAction(0, "Marcar pagada", markPaidPendingIntent)
            builder.addAction(0, "Recordar manana", remindTomorrowPendingIntent)
        }

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios PayAlert",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisos de pago, corte y resumen semanal"
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ALL_NOTIFICATIONS_TAG = "payalert_all_notifications"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_CARD_ID = "card_id"
        const val KEY_KIND = "kind"
        const val KEY_QUICK_ACTIONS_ENABLED = "quick_actions_enabled"
        private const val CHANNEL_ID = "payalert_reminders"
    }
}
