package com.budjet.app.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
enum class IrpfRegion {
    ANDALUCIA,
    ARAGON,
    ASTURIAS,
    BALEARES,
    CANARIAS,
    CANTABRIA,
    CASTILLA_LA_MANCHA,
    CASTILLA_Y_LEON,
    CATALUNA,
    MADRID,
    EXTREMADURA,
    GALICIA,
    LA_RIOJA,
    MURCIA,
    COMUNIDAD_VALENCIANA,
    CEUTA,
    MELILLA,
    PAIS_VASCO,
    NAVARRA,
    ;

    val isForal: Boolean get() = this == PAIS_VASCO || this == NAVARRA
}

data class TaxBracket(
    /** Cumulative upper bound in cents. */
    val upToCents: Long,
    val rate: Double,
)

data class IrpfEstimate(
    val grossCents: Long,
    val socialSecurityCents: Long,
    val withheldCents: Long,
    val taxableBaseCents: Long,
    /** Null for the foral regimes, where the common-scale estimate is not valid. */
    val estimatedTaxCents: Long?,
    val differenceCents: Long?,
    val effectiveRate: Float?,
)

object IrpfMath {

    /** Work expense used in the simplified annual estimate. */
    const val WORK_EXPENSE_CENTS = 2_000_00L

    /** General personal minimum. */
    const val PERSONAL_MINIMUM_CENTS = 5_550_00L

    private fun bracket(upToEuros: Double, ratePercent: Double) = TaxBracket(
        upToCents = (upToEuros * 100.0).roundToLong(),
        rate = ratePercent / 100.0,
    )

    private val STATE_BRACKETS = listOf(
        bracket(12_450.0, 9.5),
        bracket(20_200.0, 12.0),
        bracket(35_200.0, 15.0),
        bracket(60_000.0, 18.5),
        bracket(300_000.0, 22.5),
        bracket(Double.MAX_VALUE, 24.5),
    )

    private val ANDALUCIA = listOf(
        bracket(13_000.0, 8.5),
        bracket(21_000.0, 11.0),
        bracket(35_200.0, 14.5),
        bracket(60_000.0, 18.0),
        bracket(90_000.0, 22.5),
        bracket(Double.MAX_VALUE, 24.5),
    )

    private val ARAGON = listOf(
        bracket(13_072.5, 9.5),
        bracket(21_210.0, 12.0),
        bracket(36_959.9, 15.0),
        bracket(52_500.0, 18.5),
        bracket(60_000.0, 20.5),
        bracket(80_000.0, 23.0),
        bracket(90_000.0, 24.0),
        bracket(130_000.0, 25.0),
        bracket(Double.MAX_VALUE, 25.5),
    )

    private val ASTURIAS = listOf(
        bracket(12_450.0, 9.0),
        bracket(17_707.2, 12.0),
        bracket(33_007.2, 14.0),
        bracket(53_407.2, 19.2),
        bracket(70_000.0, 21.5),
        bracket(90_000.0, 22.5),
        bracket(175_000.0, 25.0),
        bracket(Double.MAX_VALUE, 26.0),
    )

    private val BALEARES = listOf(
        bracket(10_000.0, 9.0),
        bracket(18_000.0, 11.25),
        bracket(30_000.0, 14.25),
        bracket(48_000.0, 17.5),
        bracket(70_000.0, 19.0),
        bracket(90_000.0, 21.75),
        bracket(120_000.0, 22.75),
        bracket(175_000.0, 23.75),
        bracket(Double.MAX_VALUE, 24.75),
    )

    private val CANARIAS = listOf(
        bracket(13_748.0, 9.0),
        bracket(19_422.0, 11.5),
        bracket(35_924.0, 14.0),
        bracket(57_566.0, 18.5),
        bracket(93_268.0, 23.5),
        bracket(123_745.0, 25.0),
        bracket(Double.MAX_VALUE, 26.0),
    )

    private val CANTABRIA = listOf(
        bracket(13_000.0, 8.5),
        bracket(21_000.0, 11.0),
        bracket(35_200.0, 14.5),
        bracket(60_000.0, 18.0),
        bracket(90_000.0, 22.5),
        bracket(Double.MAX_VALUE, 24.5),
    )

    private val CASTILLA_LA_MANCHA = listOf(
        bracket(12_450.0, 9.5),
        bracket(20_200.0, 12.0),
        bracket(35_200.0, 15.0),
        bracket(60_000.0, 18.5),
        bracket(Double.MAX_VALUE, 22.5),
    )

    private val CASTILLA_Y_LEON = listOf(
        bracket(12_450.0, 9.0),
        bracket(20_200.0, 12.0),
        bracket(35_200.0, 14.0),
        bracket(53_407.2, 18.5),
        bracket(Double.MAX_VALUE, 21.5),
    )

    private val CATALUNA = listOf(
        bracket(12_500.0, 9.5),
        bracket(22_000.0, 12.5),
        bracket(33_000.0, 16.0),
        bracket(53_000.0, 19.0),
        bracket(90_000.0, 21.5),
        bracket(120_000.0, 23.5),
        bracket(175_000.0, 24.5),
        bracket(Double.MAX_VALUE, 25.5),
    )

    private val MADRID = listOf(
        bracket(13_362.22, 8.5),
        bracket(19_004.63, 10.7),
        bracket(35_425.68, 12.8),
        bracket(57_320.40, 17.4),
        bracket(Double.MAX_VALUE, 20.5),
    )

    private val EXTREMADURA = listOf(
        bracket(12_450.0, 8.0),
        bracket(20_200.0, 10.0),
        bracket(24_200.0, 16.0),
        bracket(35_200.0, 17.5),
        bracket(60_000.0, 21.0),
        bracket(80_200.0, 23.5),
        bracket(99_200.0, 24.0),
        bracket(120_200.0, 24.5),
        bracket(Double.MAX_VALUE, 25.0),
    )

    private val GALICIA = listOf(
        bracket(12_985.35, 9.0),
        bracket(21_068.60, 11.65),
        bracket(35_200.0, 14.9),
        bracket(47_600.0, 18.4),
        bracket(Double.MAX_VALUE, 22.5),
    )

    private val LA_RIOJA = listOf(
        bracket(12_450.0, 8.0),
        bracket(20_200.0, 10.6),
        bracket(35_200.0, 13.6),
        bracket(40_000.0, 17.8),
        bracket(50_000.0, 18.3),
        bracket(60_000.0, 19.0),
        bracket(120_000.0, 24.5),
        bracket(Double.MAX_VALUE, 27.0),
    )

    private val MURCIA = listOf(
        bracket(12_450.0, 9.5),
        bracket(20_200.0, 11.2),
        bracket(34_000.0, 13.3),
        bracket(60_000.0, 17.9),
        bracket(Double.MAX_VALUE, 22.5),
    )

    private val COMUNIDAD_VALENCIANA = listOf(
        bracket(12_000.0, 9.0),
        bracket(22_000.0, 12.0),
        bracket(32_000.0, 15.0),
        bracket(42_000.0, 17.5),
        bracket(52_000.0, 20.0),
        bracket(62_000.0, 22.5),
        bracket(300_000.0, 24.5),
        bracket(Double.MAX_VALUE, 25.0),
    )

    private val CEUTA_MELILLA = listOf(
        bracket(12_450.0, 9.5),
        bracket(20_200.0, 12.0),
        bracket(35_200.0, 15.0),
        bracket(60_000.0, 18.5),
        bracket(300_000.0, 22.5),
        bracket(Double.MAX_VALUE, 22.5),
    )

    fun regionalBrackets(region: IrpfRegion): List<TaxBracket>? = when (region) {
        IrpfRegion.ANDALUCIA -> ANDALUCIA
        IrpfRegion.ARAGON -> ARAGON
        IrpfRegion.ASTURIAS -> ASTURIAS
        IrpfRegion.BALEARES -> BALEARES
        IrpfRegion.CANARIAS -> CANARIAS
        IrpfRegion.CANTABRIA -> CANTABRIA
        IrpfRegion.CASTILLA_LA_MANCHA -> CASTILLA_LA_MANCHA
        IrpfRegion.CASTILLA_Y_LEON -> CASTILLA_Y_LEON
        IrpfRegion.CATALUNA -> CATALUNA
        IrpfRegion.MADRID -> MADRID
        IrpfRegion.EXTREMADURA -> EXTREMADURA
        IrpfRegion.GALICIA -> GALICIA
        IrpfRegion.LA_RIOJA -> LA_RIOJA
        IrpfRegion.MURCIA -> MURCIA
        IrpfRegion.COMUNIDAD_VALENCIANA -> COMUNIDAD_VALENCIANA
        IrpfRegion.CEUTA, IrpfRegion.MELILLA -> CEUTA_MELILLA
        IrpfRegion.PAIS_VASCO, IrpfRegion.NAVARRA -> null
    }

    fun progressiveTax(baseCents: Long, brackets: List<TaxBracket>): Long {
        if (baseCents <= 0L) return 0L
        var tax = 0.0
        var lower = 0L
        for (bracket in brackets) {
            if (baseCents <= lower) break
            val upper = minOf(baseCents, bracket.upToCents)
            tax += (upper - lower) * bracket.rate
            lower = bracket.upToCents
        }
        return tax.roundToLong()
    }

    fun estimate(
        grossCents: Long,
        socialSecurityCents: Long,
        withheldCents: Long,
        region: IrpfRegion,
    ): IrpfEstimate {
        val taxableBase = (grossCents - socialSecurityCents - WORK_EXPENSE_CENTS).coerceAtLeast(0L)
        val regional = regionalBrackets(region)
        if (regional == null) {
            return IrpfEstimate(
                grossCents = grossCents,
                socialSecurityCents = socialSecurityCents,
                withheldCents = withheldCents,
                taxableBaseCents = taxableBase,
                estimatedTaxCents = null,
                differenceCents = null,
                effectiveRate = null,
            )
        }
        val grossTax = progressiveTax(taxableBase, STATE_BRACKETS) +
            progressiveTax(taxableBase, regional)
        val minimumTax = progressiveTax(PERSONAL_MINIMUM_CENTS, STATE_BRACKETS) +
            progressiveTax(PERSONAL_MINIMUM_CENTS, regional)
        val estimated = (grossTax - minimumTax).coerceAtLeast(0L)
        val difference = estimated - withheldCents
        val effectiveRate = if (grossCents > 0L) estimated.toFloat() / grossCents else null
        return IrpfEstimate(
            grossCents = grossCents,
            socialSecurityCents = socialSecurityCents,
            withheldCents = withheldCents,
            taxableBaseCents = taxableBase,
            estimatedTaxCents = estimated,
            differenceCents = difference,
            effectiveRate = effectiveRate,
        )
    }
}
