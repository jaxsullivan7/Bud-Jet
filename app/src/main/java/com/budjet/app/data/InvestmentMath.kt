package com.budjet.app.data

import java.time.LocalDate
import java.time.YearMonth
import java.util.Random
import kotlin.math.ln
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class InvestmentSummary(
    val valueCents: Long,
    val contributedCents: Long,
    val returnsCents: Long,
    val returnPercent: Float?,
    val annualizedReturn: Double?,
)

data class ProductSummary(
    val investment: Investment,
    val summary: InvestmentSummary,
)

data class PortfolioSummary(
    val valueCents: Long,
    val contributedCents: Long,
    val returnsCents: Long,
    val returnPercent: Float?,
    val annualizedReturn: Double?,
    val products: List<ProductSummary>,
)

enum class CompoundFrequency(val label: String) {
    MONTHLY("Mensual"),
    ANNUAL("Anual"),
}

data class CompoundResult(
    val finalCents: Long,
    val contributedCents: Long,
    val interestCents: Long,
    val lowCents: Long,
    val medianCents: Long,
    val highCents: Long,
    val yearlyBalances: List<Long>,
    val simulated: Boolean,
)

object InvestmentMath {

    fun summary(entries: List<InvestmentEntry>, today: LocalDate = LocalDate.now()): InvestmentSummary {
        var contributions = 0L
        var withdrawals = 0L
        var returns = 0L
        for (entry in entries) {
            when (entry.kind) {
                InvestmentEntryKind.CONTRIBUTION -> contributions += entry.amountCents
                InvestmentEntryKind.WITHDRAWAL -> withdrawals += entry.amountCents
                InvestmentEntryKind.RETURN,
                InvestmentEntryKind.VALUATION,
                -> returns += entry.amountCents
            }
        }
        val contributed = contributions - withdrawals
        val value = contributed + returns
        return InvestmentSummary(
            valueCents = value,
            contributedCents = contributed,
            returnsCents = returns,
            returnPercent = if (contributed > 0) returns.toFloat() / contributed else null,
            annualizedReturn = annualized(entries, value, today),
        )
    }

    fun portfolio(
        investments: List<Investment>,
        entries: List<InvestmentEntry>,
        today: LocalDate = LocalDate.now(),
    ): PortfolioSummary {
        val products = investments.map { investment ->
            ProductSummary(
                investment = investment,
                summary = summary(entries.filter { it.investmentId == investment.id }, today),
            )
        }
        val value = products.sumOf { it.summary.valueCents }
        val contributed = products.sumOf { it.summary.contributedCents }
        val returns = products.sumOf { it.summary.returnsCents }
        return PortfolioSummary(
            valueCents = value,
            contributedCents = contributed,
            returnsCents = returns,
            returnPercent = if (contributed > 0) returns.toFloat() / contributed else null,
            annualizedReturn = annualized(entries, value, today),
            products = products,
        )
    }

    /** Annualized money-weighted return (XIRR) for a list of investment entries. */
    fun annualized(
        entries: List<InvestmentEntry>,
        finalValueCents: Long,
        today: LocalDate = LocalDate.now(),
    ): Double? {
        if (finalValueCents <= 0L) return null
        val flows = entries.mapNotNull { entry ->
            val amount = when (entry.kind) {
                InvestmentEntryKind.CONTRIBUTION -> -entry.amountCents
                InvestmentEntryKind.WITHDRAWAL -> entry.amountCents
                InvestmentEntryKind.RETURN,
                InvestmentEntryKind.VALUATION,
                -> null
            } ?: return@mapNotNull null
            CashFlow(entry.dateEpochDay, amount)
        }.toMutableList()
        flows += CashFlow(today.toEpochDay(), finalValueCents)
        if (flows.none { it.amountCents > 0 } || flows.none { it.amountCents < 0 }) return null
        val first = flows.minOf { it.dateEpochDay }
        if (today.toEpochDay() <= first) return null
        fun npv(rate: Double): Double = flows.sumOf { flow ->
            val days = (flow.dateEpochDay - first).toDouble()
            flow.amountCents.toDouble() / (1.0 + rate).pow(days / 365.0)
        }
        var low = -0.9999
        var high = 100.0
        if (npv(low) * npv(high) > 0) return null
        repeat(200) {
            val mid = (low + high) / 2.0
            val value = npv(mid)
            if (value > 0) low = mid else high = mid
        }
        val result = (low + high) / 2.0
        return result.takeIf { it.isFinite() }
    }

    fun valueSeries(
        entries: List<InvestmentEntry>,
        months: Int = 12,
        today: LocalDate = LocalDate.now(),
    ): List<Pair<YearMonth, Long>> =
        (months - 1 downTo 0).map { back ->
            val month = YearMonth.from(today).minusMonths(back.toLong())
            val end = minOf(month.atEndOfMonth(), today)
            val cutoff = end.toEpochDay()
            val value = entries.filter { it.dateEpochDay <= cutoff }.sumOf { signedValue(it) }
            month to value
        }

    private fun signedValue(entry: InvestmentEntry): Long = when (entry.kind) {
        InvestmentEntryKind.CONTRIBUTION -> entry.amountCents
        InvestmentEntryKind.WITHDRAWAL -> -entry.amountCents
        InvestmentEntryKind.RETURN,
        InvestmentEntryKind.VALUATION,
        -> entry.amountCents
    }

    /**
     * Compound interest with monthly contributions. [annualVolatilityPercent] is
     * optional: when greater than zero the result includes a Monte Carlo range.
     */
    fun compound(
        initialCents: Long,
        monthlyCents: Long,
        annualRatePercent: Double,
        years: Int,
        frequency: CompoundFrequency,
        annualVolatilityPercent: Double = 0.0,
        simulations: Int = 1_200,
        seed: Long = 42L,
    ): CompoundResult {
        val safeYears = years.coerceIn(1, 60)
        val months = safeYears * 12
        val annualRate = (annualRatePercent / 100.0).coerceAtLeast(-0.95)
        val vol = (annualVolatilityPercent / 100.0).coerceAtLeast(0.0)
        val contributed = initialCents + monthlyCents * months
        val yearly = mutableListOf<Long>()
        var balance = initialCents.toDouble()
        val monthlyRate = (1.0 + annualRate).pow(1.0 / 12.0) - 1.0
        repeat(months) { index ->
            if (frequency == CompoundFrequency.MONTHLY) {
                balance *= 1.0 + monthlyRate
            } else if ((index + 1) % 12 == 0) {
                balance *= 1.0 + annualRate
            }
            balance += monthlyCents
            if ((index + 1) % 12 == 0) yearly += balance.roundToLong()
        }
        val deterministic = balance.roundToLong()
        if (vol <= 0.0) {
            return CompoundResult(
                finalCents = deterministic,
                contributedCents = contributed,
                interestCents = deterministic - contributed,
                lowCents = deterministic,
                medianCents = deterministic,
                highCents = deterministic,
                yearlyBalances = yearly,
                simulated = false,
            )
        }

        val random = Random(seed)
        val results = LongArray(simulations.coerceAtLeast(100)) { path ->
            var value = initialCents.toDouble()
            repeat(months) { index ->
                if (frequency == CompoundFrequency.MONTHLY) {
                    val sigma = vol / sqrt(12.0)
                    val mu = ln(1.0 + annualRate) / 12.0
                    val gaussian = random.nextGaussian()
                    val monthlyReturn = exp(mu - 0.5 * sigma * sigma + sigma * gaussian) - 1.0
                    value *= 1.0 + monthlyReturn
                } else if ((index + 1) % 12 == 0) {
                    val gaussian = random.nextGaussian()
                    val annualReturn = exp(ln(1.0 + annualRate) - 0.5 * vol * vol + vol * gaussian) - 1.0
                    value *= 1.0 + annualReturn
                }
                value += monthlyCents
            }
            value.roundToLong()
        }.also { it.sort() }

        fun percentile(p: Double): Long = results[((results.size - 1) * p).roundToInt()]
        val median = percentile(0.5)
        return CompoundResult(
            finalCents = median,
            contributedCents = contributed,
            interestCents = median - contributed,
            lowCents = percentile(0.10),
            medianCents = median,
            highCents = percentile(0.90),
            yearlyBalances = yearly,
            simulated = true,
        )
    }

    private data class CashFlow(val dateEpochDay: Long, val amountCents: Long)
}
