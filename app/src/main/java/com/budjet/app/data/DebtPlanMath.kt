package com.budjet.app.data

import java.time.LocalDate
import kotlin.math.roundToLong

enum class DebtStrategy(val label: String) {
    SNOWBALL("Bola de nieve"),
    AVALANCHE("Avalancha"),
}

data class DebtPlanDebt(
    val id: String,
    val name: String,
    val balanceCents: Long,
    val annualRatePercent: Double,
    val minimumPaymentCents: Long,
)

data class DebtPlanItem(
    val debtId: String,
    val name: String,
    val payoffMonth: Int,
    val interestPaidCents: Long,
)

data class DebtPlanResult(
    val strategy: DebtStrategy,
    val monthlyBudgetCents: Long,
    val months: Int,
    val debtFreeDate: LocalDate?,
    val totalInterestCents: Long,
    val totalPaidCents: Long,
    val items: List<DebtPlanItem>,
    val feasible: Boolean,
    val message: String? = null,
)

object DebtPlanMath {

    fun fromData(data: AppData): List<DebtPlanDebt> = data.liabilities.map {
        DebtPlanDebt(
            id = it.id,
            name = it.name,
            balanceCents = it.balanceCents,
            annualRatePercent = it.annualRatePercent,
            minimumPaymentCents = it.minimumPaymentCents,
        )
    }

    fun minimumTotal(debts: List<DebtPlanDebt>): Long =
        debts.filter { it.balanceCents > 0L }.sumOf { it.minimumPaymentCents.coerceAtLeast(0L) }

    fun simulate(
        debts: List<DebtPlanDebt>,
        monthlyBudgetCents: Long,
        strategy: DebtStrategy,
        today: LocalDate = LocalDate.now(),
        maxMonths: Int = 600,
    ): DebtPlanResult {
        val active = debts.filter { it.balanceCents > 0L }
        if (active.isEmpty()) {
            return DebtPlanResult(
                strategy = strategy,
                monthlyBudgetCents = monthlyBudgetCents,
                months = 0,
                debtFreeDate = today,
                totalInterestCents = 0L,
                totalPaidCents = 0L,
                items = emptyList(),
                feasible = true,
            )
        }
        val minimums = minimumTotal(active)
        if (monthlyBudgetCents <= 0L) {
            return failed(strategy, monthlyBudgetCents, today, "Indica un pago mensual.")
        }
        if (monthlyBudgetCents < minimums) {
            return failed(
                strategy,
                monthlyBudgetCents,
                today,
                "El pago mensual no cubre las cuotas mínimas (${minimums / 100.0} €).",
            )
        }

        val balances = active.associate { it.id to it.balanceCents.toDouble() }.toMutableMap()
        val interestByDebt = active.associate { it.id to 0L }.toMutableMap()
        val payoff = active.associate { it.id to 0 }.toMutableMap()
        val order = when (strategy) {
            DebtStrategy.SNOWBALL -> active.sortedWith(compareBy({ it.balanceCents }, { -it.annualRatePercent }))
            DebtStrategy.AVALANCHE -> active.sortedWith(compareByDescending<DebtPlanDebt> { it.annualRatePercent }.thenBy { it.balanceCents })
        }
        var month = 0
        var totalInterest = 0L
        var totalPaid = 0L

        while (month < maxMonths && balances.values.any { it > 0.5 }) {
            month++
            var extra = monthlyBudgetCents
            for (debt in active) {
                val balance = balances[debt.id] ?: continue
                if (balance <= 0.0) continue
                val monthlyRate = (debt.annualRatePercent / 100.0) / 12.0
                val interest = (balance * monthlyRate).roundToLong().coerceAtLeast(0L)
                balances[debt.id] = balance + interest
                interestByDebt[debt.id] = (interestByDebt[debt.id] ?: 0L) + interest
                totalInterest += interest
                val payment = minOf(debt.minimumPaymentCents.coerceAtLeast(0L), balances[debt.id]!!.roundToLong())
                if (payment > 0L) {
                    balances[debt.id] = balances[debt.id]!! - payment
                    totalPaid += payment
                    extra -= payment
                }
                if (balances[debt.id]!! <= 0.5) {
                    balances[debt.id] = 0.0
                    if (payoff[debt.id] == 0) payoff[debt.id] = month
                }
            }
            if (extra > 0L) {
                for (debt in order) {
                    if (extra <= 0L) break
                    val balance = balances[debt.id] ?: continue
                    if (balance <= 0.5) continue
                    val payment = minOf(extra.toDouble(), balance).roundToLong()
                    balances[debt.id] = balance - payment
                    totalPaid += payment
                    extra -= payment
                    if (balances[debt.id]!! <= 0.5) {
                        balances[debt.id] = 0.0
                        if (payoff[debt.id] == 0) payoff[debt.id] = month
                    }
                }
            }
        }

        val feasible = balances.values.all { it <= 0.5 }
        return DebtPlanResult(
            strategy = strategy,
            monthlyBudgetCents = monthlyBudgetCents,
            months = month,
            debtFreeDate = if (feasible) today.plusMonths(month.toLong()) else null,
            totalInterestCents = totalInterest,
            totalPaidCents = totalPaid,
            items = order.map {
                DebtPlanItem(
                    debtId = it.id,
                    name = it.name,
                    payoffMonth = payoff[it.id] ?: month,
                    interestPaidCents = interestByDebt[it.id] ?: 0L,
                )
            },
            feasible = feasible,
            message = if (feasible) null else "Con este pago mensual la deuda no termina de bajar.",
        )
    }

    private fun failed(
        strategy: DebtStrategy,
        monthlyBudgetCents: Long,
        today: LocalDate,
        message: String,
    ) = DebtPlanResult(
        strategy = strategy,
        monthlyBudgetCents = monthlyBudgetCents,
        months = 0,
        debtFreeDate = null,
        totalInterestCents = 0L,
        totalPaidCents = 0L,
        items = emptyList(),
        feasible = false,
        message = message,
    )
}
