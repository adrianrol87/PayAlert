package com.payalert.app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.text.Normalizer

data class CreditCardItem(
    val id: String,
    val bankCode: String,
    val bankName: String,
    val cardType: String,
    val lastDigits: String,
    val totalDebt: Double? = null,
    val cutDate: LocalDate,
    val dueDate: LocalDate,
    val isPaid: Boolean = false,
    val manualOrder: Int = 0,
) {
    val daysRemaining: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)

    val statusText: String
        get() = when {
            isPaid -> "Pagada"
            daysRemaining < 0L -> "Pago vencido"
            daysRemaining == 0L -> "Vence hoy"
            else -> "Faltan $daysRemaining dias"
        }

    val statusStyle: StatusStyle
        get() = when {
            isPaid -> StatusStyle.Gray
            daysRemaining < 0L -> StatusStyle.Red
            daysRemaining <= 5L -> StatusStyle.Orange
            else -> StatusStyle.Green
        }

    val hasDebt: Boolean
        get() = (totalDebt ?: 0.0) > 0

    val cardTypeCode: String
        get() = normalizeAssetPart(cardType)

    val preferredAssetDirectory: String
        get() = "${bankCode}_${cardTypeCode}"

    val fallbackAssetDirectory: String
        get() = bankCode

    fun advanceToNextMonth(today: LocalDate = LocalDate.now()): CreditCardItem {
        var nextCutDate = cutDate
        var nextDueDate = dueDate

        while (nextDueDate.isBefore(today)) {
            nextCutDate = nextCutDate.plusMonths(1)
            nextDueDate = nextDueDate.plusMonths(1)
        }

        return copy(
            cutDate = nextCutDate,
            dueDate = nextDueDate,
            isPaid = false,
        )
    }
}

enum class StatusStyle {
    Green, Orange, Red, Gray
}

fun normalizeAssetPart(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()

    return buildString {
        normalized.forEach { char ->
            when {
                char.isLetterOrDigit() -> append(char)
                char == '&' -> append("&")
                else -> {}
            }
        }
    }
}
