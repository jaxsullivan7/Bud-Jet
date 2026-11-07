package com.budjet.app

import com.budjet.app.data.IrpfMath
import com.budjet.app.data.IrpfRegion
import com.budjet.app.data.PayrollKind
import com.budjet.app.data.Payslip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IrpfMathTest {

    @Test
    fun `net pay subtracts every deduction`() {
        val payslip = Payslip(
            id = "p1",
            dateEpochDay = 0,
            grossCents = 300_000,
            irpfCents = 42_000,
            socialSecurityCents = 19_000,
            otherDeductionsCents = 1_000,
            accountId = "main",
            kind = PayrollKind.MONTHLY,
        )
        assertEquals(238_000L, payslip.netCents)
    }

    @Test
    fun `madrid estimate applies state and regional brackets`() {
        val estimate = IrpfMath.estimate(
            grossCents = 3_000_000,
            socialSecurityCents = 190_000,
            withheldCents = 400_000,
            region = IrpfRegion.MADRID,
        )
        assertEquals(2_610_000L, estimate.taxableBaseCents)
        assertEquals(464_648L, estimate.estimatedTaxCents)
        assertEquals(64_648L, estimate.differenceCents)
        assertTrue((estimate.effectiveRate ?: 0f) > 0f)
    }

    @Test
    fun `withheld tax can produce a refund`() {
        val estimate = IrpfMath.estimate(
            grossCents = 3_000_000,
            socialSecurityCents = 190_000,
            withheldCents = 600_000,
            region = IrpfRegion.MADRID,
        )
        assertEquals(-135_352L, estimate.differenceCents)
    }

    @Test
    fun `foral regions do not produce a common scale estimate`() {
        val estimate = IrpfMath.estimate(
            grossCents = 3_000_000,
            socialSecurityCents = 190_000,
            withheldCents = 400_000,
            region = IrpfRegion.PAIS_VASCO,
        )
        assertNull(estimate.estimatedTaxCents)
        assertNull(estimate.differenceCents)
        assertNull(estimate.effectiveRate)
    }
}
