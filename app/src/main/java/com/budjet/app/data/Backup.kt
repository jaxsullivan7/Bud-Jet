package com.budjet.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
data class BackupEnvelope(
    val app: String = Backup.APP_NAME,
    val format: Int = Backup.FORMAT,
    val exportedAt: String = "",
    val data: AppData,
)

/**
 * Versioned, validated JSON backup. Import always replaces the whole local
 * dataset, so validation happens before anything touches the repository.
 */
object Backup {

    const val APP_NAME = "Bud Jet"
    const val FORMAT = 1
    const val CURRENT_VERSION = 5
    const val MAX_BYTES = 10 * 1024 * 1024

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(data: AppData, now: Instant = Instant.now()): String {
        val envelope = BackupEnvelope(
            exportedAt = now.toString(),
            data = data.copy(version = CURRENT_VERSION),
        )
        return json.encodeToString(BackupEnvelope.serializer(), envelope)
    }

    fun decode(text: String): Result<AppData> = runCatching {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) {
            "El archivo es demasiado grande para ser una copia de Bud Jet."
        }
        val fromEnvelope = runCatching {
            json.decodeFromString(BackupEnvelope.serializer(), text)
        }.getOrNull()
        val data = if (fromEnvelope != null) {
            require(fromEnvelope.app == APP_NAME) {
                "El archivo no parece una copia de Bud Jet."
            }
            require(fromEnvelope.format == FORMAT) {
                "La copia usa un formato que esta versión no puede leer."
            }
            require(fromEnvelope.data.version <= CURRENT_VERSION) {
                "La copia es de una versión más nueva de Bud Jet. Actualiza la app antes de restaurarla."
            }
            fromEnvelope.data
        } else {
            // Accept a raw AppData file as a fallback for hand-made backups.
            json.decodeFromString(AppData.serializer(), text)
        }
        val migrated = data.migratedToCurrent()
        validate(migrated)
        migrated.copy(
            version = CURRENT_VERSION,
            settings = migrated.settings.copy(
                name = migrated.settings.name.ifBlank { "Tú" },
                // Never enable a biometric lock just by importing a file.
                biometricLock = false,
            ),
            transactions = migrated.transactions.sortedByDescending { it.dateEpochDay },
        )
    }

    private fun validate(data: AppData) {
        require(data.categories.isNotEmpty()) { "La copia no contiene categorías." }
        require(data.accounts.isNotEmpty()) { "La copia no contiene cuentas." }
        require(data.categories.map { it.id }.distinct().size == data.categories.size) {
            "Hay categorías duplicadas en la copia."
        }
        require(data.accounts.map { it.id }.distinct().size == data.accounts.size) {
            "Hay cuentas duplicadas en la copia."
        }
        require(data.transactions.map { it.id }.distinct().size == data.transactions.size) {
            "Hay movimientos duplicados en la copia."
        }
        require(data.budgets.map { it.id }.distinct().size == data.budgets.size) {
            "Hay presupuestos duplicados en la copia."
        }
        require(data.savingsGoals.map { it.id }.distinct().size == data.savingsGoals.size) {
            "Hay objetivos duplicados en la copia."
        }
        require(data.recurringRules.map { it.id }.distinct().size == data.recurringRules.size) {
            "Hay reglas recurrentes duplicadas en la copia."
        }
        require(data.investments.map { it.id }.distinct().size == data.investments.size) {
            "Hay productos de inversión duplicados en la copia."
        }
        require(data.investmentEntries.map { it.id }.distinct().size == data.investmentEntries.size) {
            "Hay movimientos de inversión duplicados en la copia."
        }

        val categoryIds = data.categories.map { it.id }.toSet()
        val accountIds = data.accounts.map { it.id }.toSet()

        require(data.transactions.all { it.amountCents > 0L }) {
            "Hay movimientos con un importe no válido."
        }
        require(data.transactions.all { it.categoryId in categoryIds }) {
            "Hay movimientos con categorías que no existen."
        }
        require(data.transactions.all { it.accountId in accountIds }) {
            "Hay movimientos con cuentas que no existen."
        }
        require(data.budgets.all { it.limitCents > 0L && (it.categoryId == null || it.categoryId in categoryIds) }) {
            "Hay presupuestos con importes o categorías no válidos."
        }
        require(data.budgets.map { it.categoryId }.distinct().size == data.budgets.size) {
            "Hay más de un presupuesto para la misma categoría."
        }
        require(data.savingsGoals.all { it.targetCents > 0L && it.savedCents >= 0L }) {
            "Hay objetivos con importes no válidos."
        }
        require(data.recurringRules.all {
            it.amountCents > 0L &&
                it.dayOfMonth in 1..31 &&
                it.categoryId in categoryIds &&
                it.accountId in accountIds
        }) {
            "Hay reglas recurrentes con datos no válidos."
        }
        val investmentIds = data.investments.map { it.id }.toSet()
        require(data.investments.all { it.name.isNotBlank() }) {
            "Hay productos de inversión sin nombre."
        }
        require(data.investmentEntries.all {
            it.investmentId in investmentIds &&
                when (it.kind) {
                    InvestmentEntryKind.CONTRIBUTION,
                    InvestmentEntryKind.WITHDRAWAL,
                    -> it.amountCents > 0L
                    InvestmentEntryKind.RETURN,
                    InvestmentEntryKind.VALUATION,
                    -> it.amountCents != 0L
                }
        }) {
            "Hay movimientos de inversión con datos no válidos."
        }
        require(data.settings.currencyCode in Currency.entries.map { it.code }) {
            "La moneda de la copia no es compatible."
        }
        require(data.settings.name.isNotBlank()) {
            "La copia no contiene un nombre de perfil."
        }
    }
}
