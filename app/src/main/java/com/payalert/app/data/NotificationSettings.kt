package com.payalert.app.data

data class NotificationSettings(
    val dueEnabled: Boolean = true,
    val cutEnabled: Boolean = true,
    val includeAmount: Boolean = true,
    val quickActionsEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = false,
    val sameDayFollowUpEnabled: Boolean = true,
    val dueSameDay: Boolean = true,
    val due1Day: Boolean = true,
    val due2Days: Boolean = false,
    val due3Days: Boolean = true,
    val due5Days: Boolean = false,
    val due7Days: Boolean = false,
    val cutSameDay: Boolean = true,
    val cut1Day: Boolean = true,
    val cut2Days: Boolean = false,
    val cut3Days: Boolean = true,
    val cut5Days: Boolean = false,
    val cut7Days: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
) {
    val dueDaysBefore: List<Int>
        get() = buildList {
            if (due7Days) add(7)
            if (due5Days) add(5)
            if (due3Days) add(3)
            if (due2Days) add(2)
            if (due1Day) add(1)
            if (dueSameDay) add(0)
        }

    val cutDaysBefore: List<Int>
        get() = buildList {
            if (cut7Days) add(7)
            if (cut5Days) add(5)
            if (cut3Days) add(3)
            if (cut2Days) add(2)
            if (cut1Day) add(1)
            if (cutSameDay) add(0)
        }
}
