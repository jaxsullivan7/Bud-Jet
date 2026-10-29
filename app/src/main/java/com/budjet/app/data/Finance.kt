package com.budjet.app.data

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

enum class Currency(val code: String, val symbol: String, val symbolAfter: Boolean) {
    EUR("EUR", "€", true),
    USD("USD", "$", false),
    GBP("GBP", "£", false),
    MXN("MXN", "$", false);

    companion object {
        fun from(code: String): Currency = entries.firstOrNull { it.code == code } ?: EUR
    }
}

data class MonthTotals(
    val income: Long = 0,
    val expense: Long = 0,
) {
    val balance: Long get() = income - expense
    val savingsRate: Float get() = if (income <= 0) 0f else balance.toFloat() / income
}

data class MonthPoint(
    val month: YearMonth,
    val income: Long,
    val expense: Long,
)

data class CategorySpend(
    val category: Category,
    val spent: Long,
    val share: Float,
)

object Finance {

    fun transactionsOfMonth(data: AppData, month: YearMonth): List<Transaction> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return data.transactions.filter { it.dateEpochDay in start..end }
    }

    fun totals(transactions: List<Transaction>): MonthTotals {
        var income = 0L
        var expense = 0L
        for (tx in transactions) {
            if (tx.type == TxType.INCOME) income += tx.amountCents else expense += tx.amountCents
        }
        return MonthTotals(income, expense)
    }

    fun totalBalance(data: AppData): Long =
        data.settings.openingBalanceCents + data.transactions.sumOf {
            if (it.type == TxType.INCOME) it.amountCents else -it.amountCents
        }

    fun balanceAt(data: AppData, date: LocalDate): Long {
        val end = date.toEpochDay()
        return data.settings.openingBalanceCents + data.transactions.filter { it.dateEpochDay <= end }.sumOf {
            if (it.type == TxType.INCOME) it.amountCents else -it.amountCents
        }
    }

    fun series(data: AppData, anchor: YearMonth, months: Int = 6): List<MonthPoint> =
        (months - 1 downTo 0).map { back ->
            val month = anchor.minusMonths(back.toLong())
            val totals = totals(transactionsOfMonth(data, month))
            MonthPoint(month, totals.income, totals.expense)
        }

    fun spendByCategory(data: AppData, month: YearMonth): List<CategorySpend> {
        val txs = transactionsOfMonth(data, month).filter { it.type == TxType.EXPENSE }
        val total = txs.sumOf { it.amountCents }
        if (total == 0L) return emptyList()
        return txs.groupBy { it.categoryId }
            .mapNotNull { (categoryId, list) ->
                val category = data.category(categoryId) ?: return@mapNotNull null
                val spent = list.sumOf { it.amountCents }
                CategorySpend(category, spent, spent.toFloat() / total)
            }
            .sortedByDescending { it.spent }
    }

    fun spentForCategory(data: AppData, month: YearMonth, categoryId: String): Long =
        transactionsOfMonth(data, month)
            .filter { it.type == TxType.EXPENSE && it.categoryId == categoryId }
            .sumOf { it.amountCents }

    fun budgetFor(data: AppData, categoryId: String?): Budget? =
        data.budgets.firstOrNull { it.categoryId == categoryId }

    fun overallBudget(data: AppData): Budget? = budgetFor(data, null)

    fun monthlyAverageExpense(data: AppData, anchor: YearMonth, months: Int = 3): Long {
        if (months <= 0) return 0
        val values = (1..months).map { totals(transactionsOfMonth(data, anchor.minusMonths(it.toLong()))).expense }
        if (values.isEmpty()) return 0
        return values.sum() / values.size
    }

    fun monthDelta(current: Long, previous: Long): Float? =
        if (previous <= 0L) null else (current - previous).toFloat() / previous

    fun budgetProgress(spent: Long, limit: Long): Float =
        if (limit <= 0L) 0f else (spent.toFloat() / limit).coerceIn(0f, 1.5f)

    /** Rough available-to-spend per remaining day in the month. */
    fun dailyAllowance(remaining: Long, today: LocalDate, month: YearMonth): Long {
        val end = month.atEndOfMonth()
        if (today.isAfter(end)) return 0
        val days = end.dayOfMonth - today.dayOfMonth + 1
        return if (days <= 0) 0 else (remaining / days).coerceAtLeast(0)
    }

    fun compact(cents: Long, currency: Currency): String {
        val value = abs(cents) / 100.0
        val text = when {
            value >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format(java.util.Locale.US, "%.1fk", value / 1_000)
            else -> value.toInt().toString()
        }
        return if (currency.symbolAfter) "$text ${currency.symbol}" else "${currency.symbol}$text"
    }
}
