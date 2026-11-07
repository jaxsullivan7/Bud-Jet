package com.budjet.app.util

import com.budjet.app.data.Currency
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private fun appLocale(): Locale = Locale.getDefault()

private fun isEnglish(locale: Locale = appLocale()): Boolean = locale.language == "en"

private fun decimalSymbols(locale: Locale = appLocale()): DecimalFormatSymbols =
    DecimalFormatSymbols.getInstance(locale)

fun String.capitalizeEs(): String {
    val locale = appLocale()
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

fun formatMoney(
    cents: Long,
    currency: Currency,
    decimals: Boolean = true,
    symbol: Boolean = true,
    signed: Boolean = false,
): String {
    val locale = appLocale()
    val symbols = decimalSymbols(locale)
    val negative = cents < 0
    val absolute = abs(cents)
    val unit = if (decimals) absolute / 100 else (absolute + 50) / 100
    val fraction = if (decimals) absolute % 100 else 0
    val grouped = unit.toString().reversed().chunked(3)
        .joinToString(symbols.groupingSeparator.toString()).reversed()
    val decimalPart = if (decimals) {
        symbols.decimalSeparator + fraction.toString().padStart(2, '0')
    } else {
        ""
    }
    val sign = when {
        negative -> "−"
        signed && cents > 0 -> "+"
        else -> ""
    }
    val symbolAfter = if (isEnglish(locale)) false else currency.symbolAfter
    return when {
        !symbol -> "$sign$grouped$decimalPart"
        symbolAfter -> "$sign$grouped$decimalPart ${currency.symbol}"
        else -> "$sign${currency.symbol}$grouped$decimalPart"
    }
}

fun amountParts(cents: Long): Pair<String, String> {
    val symbols = decimalSymbols()
    val absolute = abs(cents)
    val unit = absolute / 100
    val fraction = absolute % 100
    val grouped = unit.toString().reversed().chunked(3)
        .joinToString(symbols.groupingSeparator.toString()).reversed()
    return grouped to fraction.toString().padStart(2, '0')
}

fun formatMonth(month: YearMonth): String {
    val locale = appLocale()
    return month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)).capitalizeEs()
}

fun formatMonthShort(month: YearMonth): String {
    val locale = appLocale()
    return month.format(DateTimeFormatter.ofPattern("MMM", locale))
        .replace(".", "")
        .capitalizeEs()
}

/** Month and year in lowercase when the language requires it (Spanish). */
fun formatMonthLower(month: YearMonth): String {
    val text = formatMonth(month)
    val locale = appLocale()
    return if (isEnglish(locale)) {
        text
    } else {
        text.replaceFirstChar { if (it.isUpperCase()) it.lowercase(locale) else it.toString() }
    }
}

fun formatDayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when {
    date == today -> if (isEnglish()) "Today" else "Hoy"
    date == today.minusDays(1) -> if (isEnglish()) "Yesterday" else "Ayer"
    else -> date.format(DateTimeFormatter.ofPattern(dayMonthPattern(), appLocale()))
}

fun formatDayNumber(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern(dayMonthPattern(), appLocale()))

fun formatFullDate(date: LocalDate): String {
    val locale = appLocale()
    val pattern = if (isEnglish(locale)) "MMMM d, yyyy" else "d 'de' MMMM 'de' yyyy"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}

fun formatShortDate(date: LocalDate): String {
    val locale = appLocale()
    val pattern = if (isEnglish(locale)) "MMM d, yyyy" else "d MMM yyyy"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}

private fun dayMonthPattern(): String = if (isEnglish()) "MMM d" else "d MMM"

fun greetingForHour(hour: Int): String = when {
    hour < 6 -> if (isEnglish()) "Good evening" else "Buenas noches"
    hour < 13 -> if (isEnglish()) "Good morning" else "Buenos días"
    hour < 20 -> if (isEnglish()) "Good afternoon" else "Buenas tardes"
    else -> if (isEnglish()) "Good evening" else "Buenas noches"
}

fun percentText(value: Float, decimals: Int = 0): String {
    val locale = appLocale()
    return String.format(locale, "%.${decimals}f%%", value * 100)
}

/**
 * Parses an amount typed by the user, accepting both English (`1,234.56`)
 * and Spanish (`1.234,56`) conventions. A lone separator with one or two
 * decimals is treated as a decimal separator; with three decimals it is
 * treated as a thousands separator.
 */
fun parseMoneyInput(text: String): Long {
    if (text.isBlank()) return 0L
    val cleaned = text.trim()
        .filter { it.isDigit() || it == ',' || it == '.' || it == '-' }
    if (cleaned.isEmpty()) return 0L
    val negative = cleaned.startsWith('-')
    val unsigned = cleaned.removePrefix("-")
    val lastComma = unsigned.lastIndexOf(',')
    val lastDot = unsigned.lastIndexOf('.')
    val decimalSeparator = when {
        lastComma >= 0 && lastDot >= 0 -> if (lastComma > lastDot) ',' else '.'
        lastComma >= 0 -> if (looksLikeDecimal(unsigned, ',')) ',' else null
        lastDot >= 0 -> if (looksLikeDecimal(unsigned, '.')) '.' else null
        else -> null
    }
    val normalized = if (decimalSeparator == null) {
        unsigned.replace(",", "").replace(".", "")
    } else {
        val index = unsigned.lastIndexOf(decimalSeparator)
        val integerPart = unsigned.substring(0, index).replace(",", "").replace(".", "")
        val decimalPart = unsigned.substring(index + 1).filter { it.isDigit() }
        "$integerPart.$decimalPart"
    }
    val value = normalized.toDoubleOrNull() ?: return 0L
    val cents = kotlin.math.round(value * 100).toLong()
    return if (negative) -cents else cents
}

private fun looksLikeDecimal(value: String, separator: Char): Boolean {
    val occurrences = value.count { it == separator }
    val decimals = value.substring(value.lastIndexOf(separator) + 1).count { it.isDigit() }
    return occurrences == 1 && decimals in 1..2
}

fun centsToInput(cents: Long): String {
    val symbols = decimalSymbols()
    return if (cents % 100L == 0L) {
        (cents / 100L).toString()
    } else {
        val unit = cents / 100L
        val fraction = abs(cents % 100L).toString().padStart(2, '0')
        "$unit${symbols.decimalSeparator}$fraction"
    }
}
