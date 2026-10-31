package com.budjet.app.data

/**
 * Migrations for data stored by older versions. The current one fixes the
 * hidden 3.850 € opening balance from the first demo when the user had already
 * cleared every movement.
 */
fun AppData.migratedToCurrent(): AppData {
    if (version >= Backup.CURRENT_VERSION) return this
    val settings = if (transactions.isEmpty() && settings.openingBalanceCents == 385_000L) {
        settings.copy(openingBalanceCents = 0L)
    } else {
        settings
    }
    return copy(version = Backup.CURRENT_VERSION, settings = settings)
}
