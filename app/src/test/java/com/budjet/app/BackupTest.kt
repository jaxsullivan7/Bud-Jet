package com.budjet.app

import com.budjet.app.data.AppData
import com.budjet.app.data.Backup
import com.budjet.app.data.DemoData
import com.budjet.app.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupTest {

    private val today = LocalDate.of(2026, 10, 30)
    private val demo = DemoData.build(today)

    @Test
    fun `round trip keeps every collection`() {
        val restored = Backup.decode(Backup.encode(demo)).getOrThrow()
        assertEquals(demo.transactions.size, restored.transactions.size)
        assertEquals(demo.budgets.size, restored.budgets.size)
        assertEquals(demo.savingsGoals.size, restored.savingsGoals.size)
        assertEquals(demo.recurringRules.size, restored.recurringRules.size)
        assertEquals(demo.categories.size, restored.categories.size)
        assertEquals(demo.accounts.size, restored.accounts.size)
        assertEquals(Backup.CURRENT_VERSION, restored.version)
    }

    @Test
    fun `import never enables biometric lock`() {
        val withLock = demo.copy(settings = Settings(biometricLock = true))
        val restored = Backup.decode(Backup.encode(withLock)).getOrThrow()
        assertFalse(restored.settings.biometricLock)
    }

    @Test
    fun `rejects an unrelated json file`() {
        assertTrue(Backup.decode("""{"hello":"world"}""").isFailure)
        assertTrue(Backup.decode("not json").isFailure)
    }

    @Test
    fun `rejects movements with unknown categories`() {
        val broken = demo.copy(
            transactions = demo.transactions + demo.transactions.first().copy(
                id = "broken",
                categoryId = "does-not-exist",
            ),
        )
        assertTrue(Backup.decode(Backup.encode(broken)).isFailure)
    }

    @Test
    fun `rejects duplicate movement ids`() {
        val broken = demo.copy(
            transactions = demo.transactions + demo.transactions.first(),
        )
        assertTrue(Backup.decode(Backup.encode(broken)).isFailure)
    }

    @Test
    fun `rejects an empty data set`() {
        assertTrue(Backup.decode(Backup.encode(AppData())).isFailure)
    }

    @Test
    fun `accepts a raw AppData file`() {
        val raw = kotlinx.serialization.json.Json {
            encodeDefaults = true
        }.encodeToString(AppData.serializer(), demo)
        val restored = Backup.decode(raw).getOrThrow()
        assertEquals(demo.transactions.size, restored.transactions.size)
    }
}
