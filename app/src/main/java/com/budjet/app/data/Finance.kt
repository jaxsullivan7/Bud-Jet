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

data class NetWorth(
    val cashCents: Long,
    val investmentsCents: Long,
    val liabilitiesCents: Long,
) {
    val totalCents: Long get() = cashCents + investmentsCents - liabilitiesCents
}

data class NetWorthPoint(
    val month: YearMonth,
    val cashCents: Long,
    val investmentsCents: Long,
    val liabilitiesCents: Long,
) {
    val totalCents: Long get() = cashCents + investmentsCents - liabilitiesCents
}

object Finance {

    fun transactionsOfMonth(data: AppData, month: YearMonth): List<Transaction> {
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        return data.transactions.filter { it.dateEpochDay in start..end }
    }

    /**
     * Movements that have already happened. Future-dated entries live in the
     * list as "Programado" but must not touch balances, budgets or analysis
     * until their date arrives.
     */
    fun postedTransactionsOfMonth(
        data: AppData,
        month: YearMonth,
        today: LocalDate = LocalDate.now(),
    ): List<Transaction> = transactionsOfMonth(data, month).filter {
        it.dateEpochDay <= today.toEpochDay()
    }

    fun totals(transactions: List<Transaction>): MonthTotals {
        var income = 0L
        var expense = 0L
        for (tx in transactions) {
            if (tx.type == TxType.INCOME) income += tx.amountCents else expense += tx.amountCents
        }
        return MonthTotals(income, expense)
    }

    fun totalBalance(data: AppData, today: LocalDate = LocalDate.now()): Long {
        val limit = today.toEpochDay()
        return data.settings.openingBalanceCents +
            data.accounts.sumOf { it.initialBalanceCents } +
            data.transactions.filter { it.dateEpochDay <= limit }.sumOf {
                if (it.type == TxType.INCOME) it.amountCents else -it.amountCents
            } +
            data.transfers.filter { it.dateEpochDay <= limit }.sumOf(::cashDelta) -
            data.liabilityPayments.filter { it.dateEpochDay <= limit }.sumOf { it.amountCents }
    }

    fun balanceAt(data: AppData, date: LocalDate): Long {
        val end = date.toEpochDay()
        return data.settings.openingBalanceCents +
            data.accounts.sumOf { it.initialBalanceCents } +
            data.transactions.filter { it.dateEpochDay <= end }.sumOf {
                if (it.type == TxType.INCOME) it.amountCents else -it.amountCents
            } +
            data.transfers.filter { it.dateEpochDay <= end }.sumOf(::cashDelta) -
            data.liabilityPayments.filter { it.dateEpochDay <= end }.sumOf { it.amountCents }
    }

    /** Cash impact of a transfer: only account legs move the available balance. */
    fun cashDelta(transfer: Transfer): Long =
        (if (transfer.toAccountId != null) transfer.amountCents else 0L) -
            (if (transfer.fromAccountId != null) transfer.amountCents else 0L)

    fun accountBalance(data: AppData, accountId: String, today: LocalDate = LocalDate.now()): Long =
        accountBalanceAt(data, accountId, today)

    fun accountBalanceAt(data: AppData, accountId: String, date: LocalDate): Long {
        val limit = date.toEpochDay()
        val opening = (if (data.accounts.firstOrNull()?.id == accountId) data.settings.openingBalanceCents else 0L) +
            (data.account(accountId)?.initialBalanceCents ?: 0L)
        val fromTransactions = data.transactions
            .filter { it.accountId == accountId && it.dateEpochDay <= limit }
            .sumOf { if (it.type == TxType.INCOME) it.amountCents else -it.amountCents }
        val fromTransfers = data.transfers
            .filter { it.dateEpochDay <= limit }
            .sumOf { transfer ->
                when {
                    transfer.fromAccountId == accountId -> -transfer.amountCents
                    transfer.toAccountId == accountId -> transfer.amountCents
                    else -> 0L
                }
            }
        val fromLiabilityPayments = data.liabilityPayments
            .filter { it.accountId == accountId && it.dateEpochDay <= limit }
            .sumOf { -it.amountCents }
        return opening + fromTransactions + fromTransfers + fromLiabilityPayments
    }

    fun liabilitiesTotal(data: AppData): Long =
        data.liabilities.sumOf { it.balanceCents.coerceAtLeast(0L) }

    fun netWorth(data: AppData, today: LocalDate = LocalDate.now()): NetWorth {
        val investments = InvestmentMath.portfolio(data.investments, data.investmentEntries, today).valueCents
        return NetWorth(
            cashCents = totalBalance(data, today),
            investmentsCents = investments,
            liabilitiesCents = liabilitiesTotal(data),
        )
    }

    fun netWorthSeries(
        data: AppData,
        months: Int = 6,
        today: LocalDate = LocalDate.now(),
    ): List<NetWorthPoint> {
        val investments = InvestmentMath.valueSeries(data.investmentEntries, months, today)
        val liabilities = liabilitiesTotal(data)
        return (months - 1 downTo 0).map { back ->
            val month = YearMonth.from(today).minusMonths(back.toLong())
            val end = minOf(month.atEndOfMonth(), today)
            NetWorthPoint(
                month = month,
                cashCents = balanceAt(data, end),
                investmentsCents = investments.firstOrNull { it.first == month }?.second ?: 0L,
                liabilitiesCents = liabilities,
            )
        }
    }

    /** Money available at the start of [month], including anything earned the previous month. */
    fun initialBalance(data: AppData, month: YearMonth): Long =
        balanceAt(data, month.atDay(1).minusDays(1))

    /** Closing balance for [month]; the current month stops at today so future entries do not count. */
    fun finalBalance(data: AppData, month: YearMonth, today: LocalDate = LocalDate.now()): Long {
        val end = month.atEndOfMonth()
        return balanceAt(data, if (end.isAfter(today)) today else end)
    }

    fun series(
        data: AppData,
        anchor: YearMonth,
        months: Int = 6,
        today: LocalDate = LocalDate.now(),
    ): List<MonthPoint> =
        (months - 1 downTo 0).map { back ->
            val month = anchor.minusMonths(back.toLong())
            val totals = totals(postedTransactionsOfMonth(data, month, today))
            MonthPoint(month, totals.income, totals.expense)
        }

    fun spendByCategory(
        data: AppData,
        month: YearMonth,
        today: LocalDate = LocalDate.now(),
    ): List<CategorySpend> {
        val txs = postedTransactionsOfMonth(data, month, today).filter { it.type == TxType.EXPENSE }
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

    fun spentForCategory(
        data: AppData,
        month: YearMonth,
        categoryId: String,
        today: LocalDate = LocalDate.now(),
    ): Long =
        postedTransactionsOfMonth(data, month, today)
            .filter { it.type == TxType.EXPENSE && it.categoryId == categoryId }
            .sumOf { it.amountCents }

    fun categoryTransactions(
        data: AppData,
        month: YearMonth,
        categoryId: String,
        today: LocalDate = LocalDate.now(),
    ): List<Transaction> =
        postedTransactionsOfMonth(data, month, today)
            .filter { it.categoryId == categoryId }
            .sortedByDescending { it.dateEpochDay }

    fun categorySeries(
        data: AppData,
        anchor: YearMonth,
        categoryId: String,
        months: Int = 6,
        today: LocalDate = LocalDate.now(),
    ): List<Pair<YearMonth, Long>> =
        (months - 1 downTo 0).map { back ->
            val month = anchor.minusMonths(back.toLong())
            month to spentForCategory(data, month, categoryId, today)
        }

    fun goalProgress(goal: SavingsGoal): Float =
        if (goal.targetCents <= 0L) 0f else (goal.savedCents.toFloat() / goal.targetCents).coerceIn(0f, 1f)

    fun goalRemaining(goal: SavingsGoal): Long = (goal.targetCents - goal.savedCents).coerceAtLeast(0L)

    fun goalMonthlySuggestion(goal: SavingsGoal, today: LocalDate = LocalDate.now()): Long {
        val deadline = goal.deadlineEpochDay?.let(LocalDate::ofEpochDay) ?: return 0L
        if (!deadline.isAfter(today)) return 0L
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, deadline).coerceAtLeast(1)
        val months = (days / 30).coerceAtLeast(1)
        return (goalRemaining(goal) / months).coerceAtLeast(0L)
    }

    fun budgetFor(data: AppData, categoryId: String?): Budget? =
        data.budgets.firstOrNull { it.categoryId == categoryId }

    fun overallBudget(data: AppData): Budget? = budgetFor(data, null)

    fun monthlyAverageExpense(
        data: AppData,
        anchor: YearMonth,
        months: Int = 3,
        today: LocalDate = LocalDate.now(),
    ): Long {
        if (months <= 0) return 0
        val values = (1..months).map {
            totals(postedTransactionsOfMonth(data, anchor.minusMonths(it.toLong()), today)).expense
        }
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
