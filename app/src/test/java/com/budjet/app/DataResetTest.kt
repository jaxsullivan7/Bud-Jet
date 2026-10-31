package com.budjet.app

import com.budjet.app.data.DemoData
import com.budjet.app.data.Finance
import com.budjet.app.data.clearedFinancialData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DataResetTest {

    @Test
    fun `starting fresh leaves every figure at zero`() {
        val demo = DemoData.build(LocalDate.of(2026, 10, 31))
        val cleared = demo.clearedFinancialData()

        assertEquals(0L, cleared.settings.openingBalanceCents)
        assertEquals(0L, Finance.totalBalance(cleared))
        assertTrue(cleared.transactions.isEmpty())
        assertTrue(cleared.budgets.isEmpty())
        assertTrue(cleared.savingsGoals.isEmpty())
        assertTrue(cleared.recurringRules.isEmpty())
    }

    @Test
    fun `starting fresh keeps categories accounts and preferences`() {
        val demo = DemoData.build(LocalDate.of(2026, 10, 31))
        val cleared = demo.clearedFinancialData()

        assertEquals(demo.categories, cleared.categories)
        assertEquals(demo.accounts, cleared.accounts)
        assertEquals(demo.settings.name, cleared.settings.name)
        assertEquals(demo.settings.currencyCode, cleared.settings.currencyCode)
        assertEquals(demo.settings.themeMode, cleared.settings.themeMode)
    }
}
