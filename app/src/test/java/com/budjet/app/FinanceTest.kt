package com.budjet.app

import com.budjet.app.data.AppData
import com.budjet.app.data.Finance
import com.budjet.app.data.Settings
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FinanceTest {

    private val today = LocalDate.of(2026, 10, 29)
    private val month = YearMonth.of(2026, 10)

    private fun tx(
        id: String,
        amount: Long,
        type: TxType,
        date: LocalDate = today,
        category: String = "food",
    ) = Transaction(
        id = id,
        amountCents = amount,
        type = type,
        categoryId = category,
        accountId = "main",
        dateEpochDay = date.toEpochDay(),
    )

    private fun data(transactions: List<Transaction>) = AppData(
        settings = Settings(openingBalanceCents = 100_000),
        transactions = transactions,
    )

    @Test
    fun `totals separate income and expenses`() {
        val totals = Finance.totals(
            listOf(
                tx("1", 2_450_00, TxType.INCOME),
                tx("2", 1_200_50, TxType.EXPENSE),
                tx("3", 300_00, TxType.EXPENSE),
            ),
        )
        assertEquals(2_450_00L, totals.income)
        assertEquals(1_500_50L, totals.expense)
        assertEquals(949_50L, totals.balance)
    }

    @Test
    fun `total balance includes opening balance and all movements`() {
        val app = data(
            listOf(
                tx("1", 500_00, TxType.INCOME),
                tx("2", 120_00, TxType.EXPENSE),
            ),
        )
        assertEquals(1_380_00L, Finance.totalBalance(app))
    }

    @Test
    fun `month filter only counts movements in that month`() {
        val app = data(
            listOf(
                tx("1", 100_00, TxType.EXPENSE, LocalDate.of(2026, 9, 30)),
                tx("2", 200_00, TxType.EXPENSE, LocalDate.of(2026, 10, 1)),
                tx("3", 300_00, TxType.EXPENSE, LocalDate.of(2026, 10, 31)),
                tx("4", 400_00, TxType.EXPENSE, LocalDate.of(2026, 11, 1)),
            ),
        )
        val totals = Finance.totals(Finance.transactionsOfMonth(app, month))
        assertEquals(500_00L, totals.expense)
    }

    @Test
    fun `balance at a date ignores future movements`() {
        val app = data(
            listOf(
                tx("1", 1_000_00, TxType.INCOME, LocalDate.of(2026, 10, 1)),
                tx("2", 200_00, TxType.EXPENSE, LocalDate.of(2026, 11, 2)),
            ),
        )
        assertEquals(2_000_00L, Finance.balanceAt(app, LocalDate.of(2026, 10, 31)))
        assertEquals(1_800_00L, Finance.balanceAt(app, LocalDate.of(2026, 11, 30)))
    }

    @Test
    fun `savings rate is balance over income`() {
        val totals = Finance.totals(
            listOf(
                tx("1", 1_000_00, TxType.INCOME),
                tx("2", 250_00, TxType.EXPENSE),
            ),
        )
        assertEquals(0.75f, totals.savingsRate, 0.0001f)
    }

    @Test
    fun `daily allowance spreads remaining budget over remaining days`() {
        val remaining = 300_00L
        val allowance = Finance.dailyAllowance(remaining, today, month)
        assertEquals(100_00L, allowance)
    }

    @Test
    fun `budget progress caps over spend at 150 percent`() {
        assertEquals(0.5f, Finance.budgetProgress(50_00, 100_00), 0.001f)
        assertTrue(Finance.budgetProgress(500_00, 100_00) <= 1.5f)
    }

    @Test
    fun `category spending is sorted and shares add up`() {
        val app = data(
            listOf(
                tx("1", 100_00, TxType.EXPENSE, category = "food"),
                tx("2", 300_00, TxType.EXPENSE, category = "home"),
                tx("3", 50_00, TxType.INCOME, category = "salary"),
            ),
        ).copy(
            categories = com.budjet.app.data.DemoData.categories,
            accounts = com.budjet.app.data.DemoData.accounts,
        )
        val spends = Finance.spendByCategory(app, month)
        assertEquals("home", spends.first().category.id)
        assertEquals(1f, spends.sumOf { it.share.toDouble() }.toFloat(), 0.0001f)
        assertEquals(0L, Finance.spentForCategory(app, month, "salary"))
    }
}
