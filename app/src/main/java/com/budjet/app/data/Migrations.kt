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

    if (result.version < 6) {
        result = result.copy(
            version = 6,
            categoryRules = if (result.categoryRules.isEmpty()) DemoData.categoryRules else result.categoryRules,
        )
    }

    if (result.version < 7) {
        result = result.copy(version = 7)
    }

    if (result.version < 8) {
        result = result.copy(version = 8)
    }

    if (result.version < 9) {
        val ordered = result.transactions.sortedByDescending { it.dateEpochDay }
        val stamped = ordered.mapIndexed { index, tx ->
            if (tx.createdAtEpochMillis > 0L) tx else tx.copy(
                createdAtEpochMillis = tx.dateEpochDay * 86_400_000L + index,
            )
        }
        result = result.copy(version = 9, transactions = stamped)
    }

    return if (result.version >= Backup.CURRENT_VERSION) result else result.copy(version = Backup.CURRENT_VERSION)
}
