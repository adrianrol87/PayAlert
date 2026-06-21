package com.payalert.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import com.payalert.app.MainActivity
import com.payalert.app.R
import com.payalert.app.data.CardsRepository
import com.payalert.app.data.StatusStyle
import com.payalert.app.ui.AssetImageResolver
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NextPaymentWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(
                appWidgetId,
                PayAlertWidgetRenderer.buildNextPaymentRemoteViews(context),
            )
        }
    }
}

class MonthSummaryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(
                appWidgetId,
                PayAlertWidgetRenderer.buildMonthSummaryRemoteViews(context),
            )
        }
    }
}

object PayAlertWidgetRenderer {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "MX"))
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateByProvider(
            context = context,
            appWidgetManager = appWidgetManager,
            provider = NextPaymentWidgetProvider::class.java,
            remoteViews = buildNextPaymentRemoteViews(context),
        )
        updateByProvider(
            context = context,
            appWidgetManager = appWidgetManager,
            provider = MonthSummaryWidgetProvider::class.java,
            remoteViews = buildMonthSummaryRemoteViews(context),
        )
    }

    fun syncAvailability(context: Context, isPro: Boolean) {
        val packageManager = context.packageManager
        val newState = if (isPro) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        listOf(
            ComponentName(context, NextPaymentWidgetProvider::class.java),
            ComponentName(context, MonthSummaryWidgetProvider::class.java),
        ).forEach { componentName ->
            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP,
            )
        }

        if (isPro) {
            updateAllWidgets(context)
        }
    }

    fun buildNextPaymentRemoteViews(context: Context): RemoteViews {
        val cards = runBlocking { CardsRepository(context).getCards() }
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_next_payment)
        val nextCard = cards
            .filterNot { it.isPaid }
            .minByOrNull { it.dueDate }

        remoteViews.setOnClickPendingIntent(R.id.widget_next_root, launchAppPendingIntent(context))

        if (nextCard == null) {
            remoteViews.setTextViewText(R.id.widget_next_title, context.getString(R.string.widget_next_title))
            remoteViews.setViewVisibility(R.id.widget_next_card_thumb, View.GONE)
            remoteViews.setTextViewText(R.id.widget_next_bank, context.getString(R.string.widget_all_clear))
            remoteViews.setTextViewText(R.id.widget_next_due, context.getString(R.string.widget_no_pending_cards))
            remoteViews.setTextViewText(R.id.widget_next_amount, context.getString(R.string.widget_enjoy_message))
            remoteViews.setTextViewText(R.id.widget_next_status, "")
            remoteViews.setImageViewResource(R.id.widget_next_status_dot, R.drawable.widget_status_gray)
            return remoteViews
        }

        remoteViews.setTextViewText(R.id.widget_next_title, context.getString(R.string.widget_next_title))
        val cardBitmap = loadWidgetCardBitmap(
            context = context,
            preferredDirectory = nextCard.preferredAssetDirectory,
            fallbackDirectory = nextCard.fallbackAssetDirectory,
        )
        if (cardBitmap != null) {
            remoteViews.setViewVisibility(R.id.widget_next_card_thumb, View.VISIBLE)
            remoteViews.setImageViewBitmap(R.id.widget_next_card_thumb, cardBitmap)
        } else {
            remoteViews.setViewVisibility(R.id.widget_next_card_thumb, View.GONE)
        }
        remoteViews.setTextViewText(
            R.id.widget_next_bank,
            "**** ${nextCard.lastDigits}",
        )
        remoteViews.setTextViewText(
            R.id.widget_next_due,
            context.getString(
                R.string.widget_due_date_format,
                nextCard.dueDate.format(dateFormatter),
            ),
        )
        remoteViews.setTextViewText(
            R.id.widget_next_amount,
            nextCard.totalDebt?.let(currencyFormatter::format) ?: context.getString(R.string.widget_no_amount),
        )
        remoteViews.setTextViewText(R.id.widget_next_status, nextCard.statusText)
        remoteViews.setImageViewResource(R.id.widget_next_status_dot, statusBackgroundRes(nextCard.statusStyle))

        return remoteViews
    }

    fun buildMonthSummaryRemoteViews(context: Context): RemoteViews {
        val cards = runBlocking { CardsRepository(context).getCards() }
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_month_summary)
        val activeCards = cards.filterNot { it.isPaid }
        val nextCards = cards
            .filterNot { it.isPaid }
            .sortedBy { it.dueDate }
            .take(3)
        val dueSoonCards = activeCards.count { it.daysRemaining in 0L..5L }
        val paidCards = cards.count { it.isPaid }
        val overdueCards = activeCards.count { it.daysRemaining < 0L }
        val totalDebt = activeCards.sumOf { it.totalDebt ?: 0.0 }

        remoteViews.setOnClickPendingIntent(R.id.widget_summary_root, launchAppPendingIntent(context))
        remoteViews.setTextViewText(R.id.widget_summary_title, context.getString(R.string.widget_upcoming_title))
        remoteViews.setTextViewText(R.id.widget_summary_due_count, dueSoonCards.toString())
        remoteViews.setTextViewText(R.id.widget_summary_paid_count, paidCards.toString())
        remoteViews.setTextViewText(R.id.widget_summary_overdue_count, overdueCards.toString())
        remoteViews.setTextViewText(R.id.widget_summary_total_debt, currencyFormatter.format(totalDebt))

        if (nextCards.isEmpty()) {
            remoteViews.setViewVisibility(R.id.widget_summary_empty, View.VISIBLE)
            remoteViews.setTextViewText(R.id.widget_summary_empty, context.getString(R.string.widget_no_pending_cards))
            bindUpcomingRow(remoteViews, 1, null, context)
            bindUpcomingRow(remoteViews, 2, null, context)
            bindUpcomingRow(remoteViews, 3, null, context)
            return remoteViews
        }

        remoteViews.setViewVisibility(R.id.widget_summary_empty, View.GONE)
        bindUpcomingRow(remoteViews, 1, nextCards.getOrNull(0), context)
        bindUpcomingRow(remoteViews, 2, nextCards.getOrNull(1), context)
        bindUpcomingRow(remoteViews, 3, nextCards.getOrNull(2), context)

        return remoteViews
    }

    private fun updateByProvider(
        context: Context,
        appWidgetManager: AppWidgetManager,
        provider: Class<out AppWidgetProvider>,
        remoteViews: RemoteViews,
    ) {
        val componentName = ComponentName(context, provider)
        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
        widgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }

    private fun launchAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun statusBackgroundRes(statusStyle: StatusStyle): Int = when (statusStyle) {
        StatusStyle.Green -> R.drawable.widget_status_green
        StatusStyle.Orange -> R.drawable.widget_status_orange
        StatusStyle.Red -> R.drawable.widget_status_red
        StatusStyle.Gray -> R.drawable.widget_status_gray
    }

    private fun loadWidgetCardBitmap(
        context: Context,
        preferredDirectory: String,
        fallbackDirectory: String,
    ): Bitmap? {
        val assetPath = AssetImageResolver.assetPathFor(
            context = context,
            preferredDirectory = preferredDirectory,
            fallbackDirectory = fallbackDirectory,
        ) ?: return null

        return try {
            context.assets.open(assetPath).use { input ->
                BitmapFactory.decodeStream(input)?.let { bitmap ->
                    Bitmap.createScaledBitmap(bitmap, 160, 100, true)
                }
            }
        } catch (_: IOException) {
            null
        }
    }

    private fun bindUpcomingRow(
        remoteViews: RemoteViews,
        index: Int,
        card: com.payalert.app.data.CreditCardItem?,
        context: Context,
    ) {
        val rowId = when (index) {
            1 -> R.id.widget_summary_row_1
            2 -> R.id.widget_summary_row_2
            else -> R.id.widget_summary_row_3
        }
        val thumbId = when (index) {
            1 -> R.id.widget_summary_thumb_1
            2 -> R.id.widget_summary_thumb_2
            else -> R.id.widget_summary_thumb_3
        }
        val digitsId = when (index) {
            1 -> R.id.widget_summary_digits_1
            2 -> R.id.widget_summary_digits_2
            else -> R.id.widget_summary_digits_3
        }
        val dueId = when (index) {
            1 -> R.id.widget_summary_due_1
            2 -> R.id.widget_summary_due_2
            else -> R.id.widget_summary_due_3
        }
        val amountId = when (index) {
            1 -> R.id.widget_summary_amount_1
            2 -> R.id.widget_summary_amount_2
            else -> R.id.widget_summary_amount_3
        }
        val dotId = when (index) {
            1 -> R.id.widget_summary_dot_1
            2 -> R.id.widget_summary_dot_2
            else -> R.id.widget_summary_dot_3
        }

        if (card == null) {
            remoteViews.setViewVisibility(rowId, View.GONE)
            return
        }

        remoteViews.setViewVisibility(rowId, View.VISIBLE)
        remoteViews.setTextViewText(digitsId, "**** ${card.lastDigits}")
        remoteViews.setTextViewText(dueId, buildDueText(card.dueDate, LocalDate.now(), context))
        remoteViews.setTextViewText(
            amountId,
            card.totalDebt?.let(currencyFormatter::format) ?: context.getString(R.string.widget_no_amount),
        )
        remoteViews.setImageViewResource(dotId, statusBackgroundRes(card.statusStyle))

        val bitmap = loadWidgetCardBitmap(
            context = context,
            preferredDirectory = card.preferredAssetDirectory,
            fallbackDirectory = card.fallbackAssetDirectory,
        )
        if (bitmap != null) {
            remoteViews.setViewVisibility(thumbId, View.VISIBLE)
            remoteViews.setImageViewBitmap(thumbId, bitmap)
        } else {
            remoteViews.setViewVisibility(thumbId, View.GONE)
        }
    }

    private fun buildDueText(dueDate: LocalDate, today: LocalDate, context: Context): String {
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
        return when {
            days < 0L -> context.getString(R.string.widget_overdue_short)
            days == 0L -> context.getString(R.string.widget_due_today_short)
            else -> context.getString(R.string.widget_due_short_format, dueDate.format(dateFormatter))
        }
    }
}
