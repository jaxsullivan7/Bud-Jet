package com.budjet.app

import com.budjet.app.data.AppData
import com.budjet.app.data.Backup
import com.budjet.app.data.DemoData
import com.budjet.app.data.Settings
import com.budjet.app.data.migratedToCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MigrationTest {

    private val demo = DemoData.build(LocalDate.of(2026, 10, 31))

    @Test
    fun `old empty data loses the hidden demo opening balance`() {
        val old = demo.copy(
            version = 3,
            transactions = emptyList(),
            settings = Settings(openingBalanceCents = 385_000L),
        )
        val migrated = old.migratedToCurrent()
        assertEquals(0L, migrated.settings.openingBalanceCents)
        assertEquals(Backup.CURRENT_VERSION, migrated.version)
    }

    @Test
    fun `old data with movements keeps its balance but updates version`() {
        val old = demo.copy(version = 3)
        val migrated = old.migratedToCurrent()
        assertEquals(385_000L, migrated.settings.openingBalanceCents)
        assertEquals(Backup.CURRENT_VERSION, migrated.version)
    }

    @Test
    fun `current data is never touched`() {
        val current = demo.copy(
            version = Backup.CURRENT_VERSION,
            transactions = emptyList(),
            settings = Settings(openingBalanceCents = 385_000L),
        )
        val migrated = current.migratedToCurrent()
        assertEquals(385_000L, migrated.settings.openingBalanceCents)
    }

    @Test
    fun `v4 data receives the new built-in categories`() {
        val old = demo.copy(
            version = 4,
            categories = demo.categories.filterNot { it.id in setOf("beauty", "refund", "reversal") },
        )
        val migrated = old.migratedToCurrent()
        assertEquals(Backup.CURRENT_VERSION, migrated.version)
        assertTrue(migrated.categories.any { it.id == "beauty" })
        assertTrue(migrated.categories.any { it.id == "refund" })
        assertTrue(migrated.categories.any { it.id == "reversal" })
    }

    @Test
    fun `an empty current app can still have a deliberate opening balance`() {
        val current = AppData().copy(
            version = Backup.CURRENT_VERSION,
            settings = Settings(openingBalanceCents = 1_000_00),
        )
        assertEquals(1_000_00L, current.migratedToCurrent().settings.openingBalanceCents)
    }
}
