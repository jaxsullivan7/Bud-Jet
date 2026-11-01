package com.budjet.app

import com.budjet.app.data.CompoundFrequency
import com.budjet.app.data.InvestmentEntry
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.data.InvestmentKind
import com.budjet.app.data.InvestmentMath
import com.budjet.app.data.Investment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InvestmentMathTest {

    private val today = LocalDate.of(2026, 11, 1)

    private fun entry(
        id: String,
        kind: InvestmentEntryKind,
        amount: Long,
        date: LocalDate,
        investmentId: String = "i1",
    ) = InvestmentEntry(
        id = id,
        investmentId = investmentId,
        kind = kind,
        amountCents = amount,
        dateEpochDay = date.toEpochDay(),
    )

    @Test
    fun `summary separates contributions, withdrawals and returns`() {
        val summary = InvestmentMath.summary(
            listOf(
                entry("1", InvestmentEntryKind.CONTRIBUTION, 100_000, LocalDate.of(2026, 1, 10)),
                entry("2", InvestmentEntryKind.WITHDRAWAL, 20_000, LocalDate.of(2026, 3, 10)),
                entry("3", InvestmentEntryKind.RETURN, 15_000, LocalDate.of(2026, 5, 10)),
                entry("4", InvestmentEntryKind.VALUATION, 5_000, LocalDate.of(2026, 6, 10)),
            ),
            today,
        )
        assertEquals(80_000L, summary.contributedCents)
        assertEquals(20_000L, summary.returnsCents)
        assertEquals(100_000L, summary.valueCents)
        assertEquals(0.25f, summary.returnPercent ?: 0f, 0.001f)
    }

    @Test
    fun `xirr annualizes a one year ten percent return`() {
        val rate = InvestmentMath.annualized(
            entries = listOf(
                entry("1", InvestmentEntryKind.CONTRIBUTION, 100_000, LocalDate.of(2025, 11, 1)),
            ),
            finalValueCents = 110_000,
            today = today,
        )
        assertNotNull(rate)
        assertEquals(0.10, rate ?: 0.0, 0.01)
    }

    @Test
    fun `portfolio aggregates products`() {
        val portfolio = InvestmentMath.portfolio(
            investments = listOf(
                Investment("a", "Fondo", InvestmentKind.INDEX_FUND),
                Investment("b", "Cripto", InvestmentKind.CRYPTO),
            ),
            entries = listOf(
                entry("1", InvestmentEntryKind.CONTRIBUTION, 100_000, LocalDate.of(2025, 1, 1), "a"),
                entry("2", InvestmentEntryKind.VALUATION, 10_000, LocalDate.of(2026, 1, 1), "a"),
                entry("3", InvestmentEntryKind.CONTRIBUTION, 50_000, LocalDate.of(2025, 6, 1), "b"),
                entry("4", InvestmentEntryKind.VALUATION, -5_000, LocalDate.of(2026, 2, 1), "b"),
            ),
            today = today,
        )
        assertEquals(155_000L, portfolio.valueCents)
        assertEquals(150_000L, portfolio.contributedCents)
        assertEquals(5_000L, portfolio.returnsCents)
        assertEquals(2, portfolio.products.size)
    }

    @Test
    fun `deterministic compound interest uses the effective annual rate`() {
        val result = InvestmentMath.compound(
            initialCents = 100_000,
            monthlyCents = 0,
            annualRatePercent = 10.0,
            years = 1,
            frequency = CompoundFrequency.MONTHLY,
        )
        assertTrue(kotlin.math.abs(result.finalCents - 110_000L) <= 50L)
        assertEquals(100_000L, result.contributedCents)
        assertTrue(result.interestCents > 0L)
    }

    @Test
    fun `monthly contributions are added to the result`() {
        val result = InvestmentMath.compound(
            initialCents = 100_000,
            monthlyCents = 10_000,
            annualRatePercent = 0.0,
            years = 1,
            frequency = CompoundFrequency.MONTHLY,
        )
        assertTrue(kotlin.math.abs(result.finalCents - 220_000L) <= 10L)
        assertEquals(220_000L, result.contributedCents)
    }

    @Test
    fun `volatility produces a range around the median`() {
        val result = InvestmentMath.compound(
            initialCents = 100_000,
            monthlyCents = 10_000,
            annualRatePercent = 7.0,
            years = 10,
            frequency = CompoundFrequency.MONTHLY,
            annualVolatilityPercent = 15.0,
            simulations = 500,
            seed = 7L,
        )
        assertTrue(result.simulated)
        assertTrue(result.lowCents <= result.medianCents)
        assertTrue(result.medianCents <= result.highCents)
        assertTrue(result.highCents > result.lowCents)
    }
}
