package com.budjet.app

import com.budjet.app.data.AppData
import com.budjet.app.data.DemoData
import com.budjet.app.data.Settings
import com.budjet.app.data.clearedFinancialData
import com.budjet.app.data.filteredFrom
import com.budjet.app.data.movementDateOrDefault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PreferencesTest {

    private val today = LocalDate.of(2026, 11, 1)

    @Test
    fun `new movements reuse the last selected date`() {
        val settings = Settings(lastMovementDateEpochDay = LocalDate.of(2026, 10, 14).toEpochDay())
        assertEquals(LocalDate.of(2026, 10, 14), settings.movementDateOrDefault(today))
    }

    @Test
    fun `new movements fall back to the provided date`() {
        assertEquals(today, Settings().movementDateOrDefault(today))
    }

    @Test
    fun `clearing all data resets remembered dates`() {
        val demo = DemoData.build(today).copy(
            settings = Settings(
                lastMovementDateEpochDay = today.toEpochDay(),
                analysisStartEpochDay = LocalDate.of(2026, 5, 1).toEpochDay(),
            ),
        )
        val cleared = demo.clearedFinancialData()
        assertNull(cleared.settings.lastMovementDateEpochDay)
        assertNull(cleared.settings.analysisStartEpochDay)
    }

    @Test
    fun `analysis range keeps only transactions from the start date`() {
        val demo = DemoData.build(today)
        val start = LocalDate.of(2026, 9, 1)
        val filtered = demo.filteredFrom(start)
        assertEquals(
            demo.transactions.count { it.dateEpochDay >= start.toEpochDay() },
            filtered.transactions.size,
        )
    }

    @Test
    fun `analysis range is inclusive`() {
        val demo = DemoData.build(today)
        val start = LocalDate.of(2026, 10, 31)
        val filtered = demo.filteredFrom(start)
        assertEquals(true, filtered.transactions.all { it.dateEpochDay >= start.toEpochDay() })
    }

    @Test
    fun `null range keeps all transactions`() {
        val demo = DemoData.build(today)
        assertEquals(demo.transactions.size, demo.filteredFrom(null).transactions.size)
    }
}
