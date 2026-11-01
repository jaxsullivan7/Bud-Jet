package com.budjet.app.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
enum class TxType { EXPENSE, INCOME }

@Serializable
data class Transaction(
    val id: String,
    val amountCents: Long,
    val type: TxType,
    val categoryId: String,
    val accountId: String,
    val dateEpochDay: Long,
    val note: String = "",
    val recurringId: String? = null,
    /** If this is an annulment, the original expense it compensates. */
    val reversalOfId: String? = null,
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val type: TxType,
    val archived: Boolean = false,
)

@Serializable
data class Account(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
)

@Serializable
data class Budget(
    val id: String,
    val categoryId: String?,
    val limitCents: Long,
)

@Serializable
data class RecurringRule(
    val id: String,
    val amountCents: Long,
    val type: TxType,
    val categoryId: String,
    val accountId: String,
    val dayOfMonth: Int = 1,
    val note: String = "",
    val startEpochDay: Long = 0,
    /** YearMonth (2026-10) of the last automatic post, to avoid duplicates. */
    val lastPostedMonth: String? = null,
    val active: Boolean = true,
)

@Serializable
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetCents: Long,
    val savedCents: Long = 0,
    val deadlineEpochDay: Long? = null,
    val icon: String = "savings",
    val colorHex: String = "#2F6B54",
)

@Serializable
enum class InvestmentKind(
    val label: String,
    val icon: String,
    val colorHex: String,
) {
    INDEX_FUND("Fondo indexado", "fund", "#6E7F3D"),
    STOCK("Acciones", "stock", "#4E6E81"),
    ETF("ETF", "etf", "#3D6B6B"),
    CRYPTO("Cripto", "crypto", "#B08B4F"),
    DEPOSIT("Depósito", "deposit", "#2F6B54"),
    PENSION("Plan de pensiones", "pension", "#6B6A9E"),
    REAL_ESTATE("Inmueble", "real_estate", "#8A6A4F"),
    OTHER("Otro", "more", "#8C8C84"),
}

@Serializable
enum class InvestmentEntryKind(val label: String) {
    CONTRIBUTION("Aportación"),
    WITHDRAWAL("Retirada"),
    RETURN("Rendimiento"),
    VALUATION("Ajuste de valor"),
}

@Serializable
data class Investment(
    val id: String,
    val name: String,
    val kind: InvestmentKind = InvestmentKind.INDEX_FUND,
    val createdAtEpochDay: Long = 0,
)

@Serializable
data class InvestmentEntry(
    val id: String,
    val investmentId: String,
    val kind: InvestmentEntryKind,
    /** Contributions and withdrawals are positive; returns and valuations are signed. */
    val amountCents: Long,
    val dateEpochDay: Long,
    val note: String = "",
)

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
enum class ThemePalette(val label: String) {
    BUSINESS("Business"),
    NAVY("Navy"),
    BOSS("Boss"),
}

@Serializable
data class Settings(
    val name: String = "Jax",
    val currencyCode: String = "EUR",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val palette: ThemePalette = ThemePalette.BUSINESS,
    val openingBalanceCents: Long = 0L,
    val biometricLock: Boolean = false,
    val lastMovementDateEpochDay: Long? = null,
    val analysisStartEpochDay: Long? = null,
)

@Serializable
data class AppData(
    val version: Int = 5,
    val settings: Settings = Settings(),
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val recurringRules: List<RecurringRule> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val investments: List<Investment> = emptyList(),
    val investmentEntries: List<InvestmentEntry> = emptyList(),
) {
    fun category(id: String?): Category? = categories.firstOrNull { it.id == id }
    fun account(id: String?): Account? = accounts.firstOrNull { it.id == id }
    fun goal(id: String?): SavingsGoal? = savingsGoals.firstOrNull { it.id == id }
    fun recurring(id: String?): RecurringRule? = recurringRules.firstOrNull { it.id == id }
    fun investment(id: String?): Investment? = investments.firstOrNull { it.id == id }
    fun entriesFor(investmentId: String): List<InvestmentEntry> =
        investmentEntries.filter { it.investmentId == investmentId }
}

/**
 * Leaves the app ready for the user's own data: all money-related content is
 * removed and the opening balance is reset to zero. Categories, accounts and
 * visual preferences are kept so the app remains usable.
 */
fun AppData.clearedFinancialData(): AppData = copy(
    transactions = emptyList(),
    budgets = emptyList(),
    savingsGoals = emptyList(),
    recurringRules = emptyList(),
    investments = emptyList(),
    investmentEntries = emptyList(),
    settings = settings.copy(
        openingBalanceCents = 0L,
        lastMovementDateEpochDay = null,
        analysisStartEpochDay = null,
    ),
)

/** Keeps only transactions on or after [start], for historical analysis ranges. */
fun AppData.filteredFrom(start: LocalDate?): AppData =
    if (start == null) this else copy(
        transactions = transactions.filter { it.dateEpochDay >= start.toEpochDay() },
    )

/** Date to prefill in a new movement: the last one used, or [fallback]. */
fun Settings.movementDateOrDefault(fallback: LocalDate): LocalDate =
    lastMovementDateEpochDay?.let(LocalDate::ofEpochDay) ?: fallback
