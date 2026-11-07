package com.budjet.app.data

import java.time.LocalDate
import java.time.YearMonth

/** Utilities for monthly and yearly rules. All dates are local and month lengths are respected. */
object Recurring {

    fun periodKey(date: LocalDate, frequency: RecurringFrequency): String =
        if (frequency == RecurringFrequency.MONTHLY) {
            YearMonth.from(date).toString()
        } else {
            date.year.toString()
        }

    fun nextOccurrence(rule: RecurringRule, today: LocalDate = LocalDate.now()): LocalDate? {
        if (!rule.active) return null
        val start = LocalDate.ofEpochDay(rule.startEpochDay)
        return when (rule.frequency) {
            RecurringFrequency.MONTHLY -> nextMonthly(rule, today, start)
            RecurringFrequency.YEARLY -> nextYearly(rule, today, start)
        }
    }

    fun dueToday(rule: RecurringRule, today: LocalDate = LocalDate.now()): LocalDate? {
        if (!rule.active) return null
        val start = LocalDate.ofEpochDay(rule.startEpochDay)
        val date = occurrenceInPeriod(rule, today)
        val alreadyPosted = rule.lastPostedMonth == periodKey(today, rule.frequency)
        return if (date in start..today && !alreadyPosted) date else null
    }

    fun nextOccurrenceLabel(rule: RecurringRule, today: LocalDate = LocalDate.now()): String {
        val date = nextOccurrence(rule, today) ?: return "Pausado"
        return com.budjet.app.util.formatShortDate(date)
    }

    private fun nextMonthly(
        rule: RecurringRule,
        today: LocalDate,
        start: LocalDate,
    ): LocalDate? {
        var month = YearMonth.from(today)
        if (rule.lastPostedMonth == month.toString()) month = month.plusMonths(1)
        repeat(24) {
            val date = month.atDay(rule.dayOfMonth.coerceIn(1, month.lengthOfMonth()))
            val alreadyPosted = rule.lastPostedMonth == month.toString()
            when {
                date < start -> Unit
                date > today -> return date
                date == today && !alreadyPosted -> return date
            }
            month = month.plusMonths(1)
        }
        return null
    }

    private fun nextYearly(
        rule: RecurringRule,
        today: LocalDate,
        start: LocalDate,
    ): LocalDate? {
        var year = today.year
        if (rule.lastPostedMonth == year.toString()) year += 1
        repeat(10) {
            val date = occurrenceInYear(rule, year)
            val alreadyPosted = rule.lastPostedMonth == year.toString()
            when {
                date < start -> Unit
                date > today -> return date
                date == today && !alreadyPosted -> return date
            }
            year += 1
        }
        return null
    }

    private fun occurrenceInPeriod(rule: RecurringRule, today: LocalDate): LocalDate =
        when (rule.frequency) {
            RecurringFrequency.MONTHLY -> {
                val month = YearMonth.from(today)
                month.atDay(rule.dayOfMonth.coerceIn(1, month.lengthOfMonth()))
            }
            RecurringFrequency.YEARLY -> occurrenceInYear(rule, today.year)
        }

    private fun occurrenceInYear(rule: RecurringRule, year: Int): LocalDate {
        val month = rule.monthOfYear.coerceIn(1, 12)
        val lastDay = YearMonth.of(year, month).lengthOfMonth()
        return LocalDate.of(year, month, rule.dayOfMonth.coerceIn(1, lastDay))
    }
}
