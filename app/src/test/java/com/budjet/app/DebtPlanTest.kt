package com.budjet.app

import com.budjet.app.data.DebtPlanDebt
import com.budjet.app.data.DebtPlanMath
import com.budjet.app.data.DebtStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DebtPlanTest {

    private val today = LocalDate.of(2026, 11, 4)

    private val debts = listOf(
        DebtPlanDebt("a", "Préstamo", 100_000, 18.0, 10_000),
        DebtPlanDebt("b", "Tarjeta", 50_000, 24.0, 5_000),
    )

    @Test
    fun `snowball starts with the smallest balance`() {
        val result = DebtPlanMath.simulate(debts, 200_000, DebtStrategy.SNOWBALL, today)
        assertTrue(result.feasible)
        assertEquals("b", result.items.first().debtId)
        assertTrue(result.months > 0)
    }

    @Test
    fun `avalanche starts with the highest rate`() {
        val result = DebtPlanMath.simulate(debts, 200_000, DebtStrategy.AVALANCHE, today)
        assertTrue(result.feasible)
        assertEquals("b", result.items.first().debtId)
    }

    @Test
    fun `avalanche does not pay more interest than snowball`() {
        val snowball = DebtPlanMath.simulate(debts, 200_000, DebtStrategy.SNOWBALL, today)
        val avalanche = DebtPlanMath.simulate(debts, 200_000, DebtStrategy.AVALANCHE, today)
        assertTrue(avalanche.totalInterestCents <= snowball.totalInterestCents)
    }

    @Test
    fun `a budget below the minimum payments is not feasible`() {
        val result = DebtPlanMath.simulate(debts, 10_000, DebtStrategy.SNOWBALL, today)
        assertFalse(result.feasible)
        assertEquals(0, result.months)
    }

    @Test
    fun `a single debt with interest is paid in one month`() {
        val result = DebtPlanMath.simulate(
            listOf(DebtPlanDebt("x", "Deuda", 100_000, 12.0, 0)),
            101_000,
            DebtStrategy.AVALANCHE,
            today,
        )
        assertTrue(result.feasible)
        assertEquals(1, result.months)
        assertEquals(1_000L, result.totalInterestCents)
        assertEquals(101_000L, result.totalPaidCents)
    }
}
