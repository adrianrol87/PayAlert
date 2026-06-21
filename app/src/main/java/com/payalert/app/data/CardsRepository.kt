package com.payalert.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.payalert.app.widget.PayAlertWidgetRenderer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate

private val Context.cardsDataStore by preferencesDataStore(name = "payalert_cards")

class CardsRepository(private val context: Context) {
    private val gson = Gson()
    private val cardsKey = stringPreferencesKey("credit_cards_json_v1")

    val cards: Flow<List<CreditCardItem>> = context.cardsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val raw = preferences[cardsKey].orEmpty()
            decode(raw)
        }

    suspend fun saveCards(items: List<CreditCardItem>) {
        context.cardsDataStore.edit { preferences ->
            preferences[cardsKey] = encode(items)
        }
        PayAlertWidgetRenderer.updateAllWidgets(context)
    }

    suspend fun getCards(): List<CreditCardItem> = cards.first()

    private fun encode(items: List<CreditCardItem>): String {
        return gson.toJson(items.map { CardStorageRecord.fromDomain(it) })
    }

    private fun decode(raw: String): List<CreditCardItem> {
        if (raw.isBlank()) return emptyList()

        val type = object : TypeToken<List<CardStorageRecord>>() {}.type
        val records: List<CardStorageRecord> = runCatching {
            gson.fromJson<List<CardStorageRecord>>(raw, type)
        }.getOrDefault(emptyList())

        return records.mapNotNull { it.toDomainOrNull() }
    }
}

private data class CardStorageRecord(
    val id: String,
    val bankCode: String,
    val bankName: String,
    val cardType: String,
    val lastDigits: String,
    val totalDebt: Double?,
    val cutDate: String,
    val dueDate: String,
    val isPaid: Boolean,
    val manualOrder: Int = 0,
) {
    fun toDomainOrNull(): CreditCardItem? {
        val parsedCutDate = runCatching { LocalDate.parse(cutDate) }.getOrNull() ?: return null
        val parsedDueDate = runCatching { LocalDate.parse(dueDate) }.getOrNull() ?: return null

        return CreditCardItem(
            id = id,
            bankCode = bankCode,
            bankName = bankName,
            cardType = cardType,
            lastDigits = lastDigits,
            totalDebt = totalDebt,
            cutDate = parsedCutDate,
            dueDate = parsedDueDate,
            isPaid = isPaid,
            manualOrder = manualOrder,
        )
    }

    companion object {
        fun fromDomain(item: CreditCardItem): CardStorageRecord {
            return CardStorageRecord(
                id = item.id,
                bankCode = item.bankCode,
                bankName = item.bankName,
                cardType = item.cardType,
                lastDigits = item.lastDigits,
                totalDebt = item.totalDebt,
                cutDate = item.cutDate.toString(),
                dueDate = item.dueDate.toString(),
                isPaid = item.isPaid,
                manualOrder = item.manualOrder,
            )
        }
    }
}
