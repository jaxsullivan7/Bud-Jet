package com.budjet.app.data

import java.time.LocalDate
import java.time.YearMonth

/** Utilities for monthly rules. All dates are local and month lengths are respected. */
object Recurring {

    fun nextOccurrence(rule: RecurringRule, today: LocalDate = LocalDate.now()): LocalDate? {
        if (!rule.active) return null
        val start = LocalDate.ofEpochDay(rule.startEpochDay)
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

    fun dueToday(rule: RecurringRule, today: LocalDate = LocalDate.now()): LocalDate? {
        if (!rule.active) return null
        val start = LocalDate.ofEpochDay(rule.startEpochDay)
        val month = YearMonth.from(today)
        val date = month.atDay(rule.dayOfMonth.coerceIn(1, month.lengthOfMonth()))
        val alreadyPosted = rule.lastPostedMonth == month.toString()
        return if (date in start..today && !alreadyPosted) date else null
    }

    fun nextOccurrenceLabel(rule: RecurringRule, today: LocalDate = LocalDate.now()): String {
        val date = nextOccurrence(rule, today) ?: return "Pausado"
        return com.budjet.app.util.formatShortDate(date)
    }
}
