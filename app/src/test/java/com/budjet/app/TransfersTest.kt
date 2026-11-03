package com.budjet.app

import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Finance
import com.budjet.app.data.Investment
import com.budjet.app.data.InvestmentEntry
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.data.Liability
import com.budjet.app.data.LiabilityKind
import com.budjet.app.data.Settings
import com.budjet.app.data.Transaction
import com.budjet.app.data.Transfer
import com.budjet.app.data.TxType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TransfersTest {

    private val today = LocalDate.of(2026, 11, 2)

    private val base = AppData(
        version = 6,
        settings = Settings(openingBalanceCents = 1_000_000),
        categories = listOf(
            Category("salary", "Nómina", "salary", "#2F6B54", TxType.INCOME),
            Category("food", "Comida", "groceries", "#6C7F5B", TxType.EXPENSE),
        ),
        accounts = listOf(
            Account("main", "Principal", "bank", "#4E6E81"),
            Account("savings", "Ahorro", "savings", "#6C7F5B"),
        ),
        transactions = listOf(
            Transaction(
                id = "t1",
                amountCents = 200_000,
                type = TxType.INCOME,
                categoryId = "salary",
                accountId = "main",
                dateEpochDay = today.minusDays(1).toEpochDay(),
            ),
        ),
    )

    @Test
    fun `transfer between accounts keeps total and moves account balances`() {
        val data = base.copy(
            transfers = listOf(
                Transfer(
                    id = "tr1",
                    amountCents = 250_000,
                    dateEpochDay = today.toEpochDay(),
                    fromAccountId = "main",
                    toAccountId = "savings",
                ),
            ),
        )
        assertEquals(1_200_000L, Finance.totalBalance(data, today))
        assertEquals(950_000L, Finance.accountBalance(data, "main", today))
        assertEquals(250_000L, Finance.accountBalance(data, "savings", today))
    }

    @Test
    fun `cash to investment transfer changes cash but not net worth`() {
        val data = base.copy(
            investments = listOf(Investment("i1", "Fondo")),
            investmentEntries = listOf(
                InvestmentEntry(
                    id = "ie1",
                    investmentId = "i1",
                    kind = InvestmentEntryKind.CONTRIBUTION,
                    amountCents = 300_000,
                    dateEpochDay = today.toEpochDay(),
                    transferId = "tr1",
                ),
            ),
            transfers = listOf(
                Transfer(
                    id = "tr1",
                    amountCents = 300_000,
                    dateEpochDay = today.toEpochDay(),
                    fromAccountId = "main",
                    toInvestmentId = "i1",
                ),
            ),
        )
        val worth = Finance.netWorth(data, today)
        assertEquals(900_000L, worth.cashCents)
        assertEquals(300_000L, worth.investmentsCents)
        assertEquals(1_200_000L, worth.totalCents)
    }

    @Test
    fun `liabilities reduce net worth`() {
        val data = base.copy(
            liabilities = listOf(
                Liability("l1", "Préstamo", LiabilityKind.LOAN, 250_000),
            ),
        )
        val worth = Finance.netWorth(data, today)
        assertEquals(1_200_000L, worth.cashCents)
        assertEquals(250_000L, worth.liabilitiesCents)
        assertEquals(950_000L, worth.totalCents)
    }

    @Test
    fun `liability payments reduce cash and debt without changing net worth`() {
        val data = base.copy(
            liabilities = listOf(
                Liability("l1", "Préstamo", LiabilityKind.LOAN, 200_000),
            ),
            liabilityPayments = listOf(
                com.budjet.app.data.LiabilityPayment(
                    id = "lp1",
                    liabilityId = "l1",
                    accountId = "main",
                    amountCents = 50_000,
                    dateEpochDay = today.toEpochDay(),
                ),
            ),
        )
        val worth = Finance.netWorth(data, today)
        assertEquals(1_150_000L, worth.cashCents)
        assertEquals(200_000L, worth.liabilitiesCents)
        assertEquals(950_000L, worth.totalCents)
        assertEquals(1_150_000L, Finance.accountBalance(data, "main", today))
    }

    @Test
    fun `future transfers are ignored until their date`() {
        val data = base.copy(
            transfers = listOf(
                Transfer(
                    id = "tr1",
                    amountCents = 100_000,
                    dateEpochDay = today.plusDays(3).toEpochDay(),
                    fromAccountId = "main",
                    toAccountId = "savings",
                ),
            ),
        )
        assertEquals(1_200_000L, Finance.totalBalance(data, today))
        assertEquals(1_200_000L, Finance.accountBalance(data, "main", today))
    }
}
