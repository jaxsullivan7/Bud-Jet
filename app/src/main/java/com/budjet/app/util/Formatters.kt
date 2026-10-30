package com.budjet.app.util

import com.budjet.app.data.Currency
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val es: Locale = Locale("es", "ES")
private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", es)
private val dayMonth = DateTimeFormatter.ofPattern("d MMM", es)

fun String.capitalizeEs(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(es) else it.toString() }

fun formatMoney(
    cents: Long,
    currency: Currency,
    decimals: Boolean = true,
    symbol: Boolean = true,
    signed: Boolean = false,
): String {
    val negative = cents < 0
    val absolute = abs(cents)
    val unit = if (decimals) absolute / 100 else (absolute + 50) / 100
    val fraction = if (decimals) absolute % 100 else 0
    val grouped = unit.toString().reversed().chunked(3).joinToString(".").reversed()
    val decimalPart = if (decimals) "," + fraction.toString().padStart(2, '0') else ""
    val sign = when {
        negative -> "−"
        signed && cents > 0 -> "+"
        else -> ""
    }
    return when {
        !symbol -> "$sign$grouped$decimalPart"
        currency.symbolAfter -> "$sign$grouped$decimalPart ${currency.symbol}"
        else -> "$sign${currency.symbol}$grouped$decimalPart"
    }
}

fun amountParts(cents: Long): Pair<String, String> {
    val absolute = abs(cents)
    val unit = absolute / 100
    val fraction = absolute % 100
    val grouped = unit.toString().reversed().chunked(3).joinToString(".").reversed()
    return grouped to fraction.toString().padStart(2, '0')
}

fun formatMonth(month: YearMonth): String = month.format(monthYear).capitalizeEs()

fun formatMonthShort(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("MMM", es)).replace(".", "").capitalizeEs()

fun formatDayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    date == today -> "Hoy"
    date == today.minusDays(1) -> "Ayer"
    else -> date.format(dayMonth)
}

fun formatDayNumber(date: LocalDate): String = date.format(dayMonth)

fun formatFullDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", es))

fun formatShortDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", es))

fun greetingForHour(hour: Int): String = when {
    hour < 6 -> "Buenas noches"
    hour < 13 -> "Buenos días"
    hour < 20 -> "Buenas tardes"
    else -> "Buenas noches"
}

fun percentText(value: Float, decimals: Int = 0): String =
    String.format(es, "%.${decimals}f%%", value * 100).replace('.', ',')

/** "2100" -> 210000 cents; "2100,50" -> 210050; ignores thousand dots. */
fun parseMoneyInput(text: String): Long {
    if (text.isBlank()) return 0L
    val normalized = text.replace(".", "").replace(',', '.')
    return normalized.toDoubleOrNull()?.let { kotlin.math.round(it * 100).toLong() } ?: 0L
}

fun centsToInput(cents: Long): String =
    if (cents % 100L == 0L) (cents / 100L).toString()
    else "${cents / 100L},${(cents % 100L).toString().padStart(2, '0')}"
