package com.budjet.app.data

/**
 * Migrations for data stored by older versions.
 *
 * v3 → v4: the old demo opening balance was hidden from the UI. If the user had
 * already cleared every movement, that 3.850 € was still showing as the total
 * balance. Reset it once.
 *
 * v4 → v5: new built-in categories (Belleza, Devolución, Anulación).
 */
fun AppData.migratedToCurrent(): AppData {
    var result = this

    if (result.version < 4) {
        val settings = if (result.transactions.isEmpty() && result.settings.openingBalanceCents == 385_000L) {
            result.settings.copy(openingBalanceCents = 0L)
        } else {
            result.settings
        }
        result = result.copy(version = 4, settings = settings)
    }

    if (result.version < 5) {
        val newIds = setOf("beauty", "refund", "reversal")
        val existing = result.categories.map { it.id }.toSet()
        val additions = DemoData.categories.filter { it.id in newIds && it.id !in existing }
        result = result.copy(version = 5, categories = result.categories + additions)
    }

    return if (result.version >= Backup.CURRENT_VERSION) result else result.copy(version = Backup.CURRENT_VERSION)
}
