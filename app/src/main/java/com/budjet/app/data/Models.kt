package com.budjet.app.data

import kotlinx.serialization.Serializable

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
)

@Serializable
data class AppData(
    val version: Int = 4,
    val settings: Settings = Settings(),
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val recurringRules: List<RecurringRule> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
) {
    fun category(id: String?): Category? = categories.firstOrNull { it.id == id }
    fun account(id: String?): Account? = accounts.firstOrNull { it.id == id }
    fun goal(id: String?): SavingsGoal? = savingsGoals.firstOrNull { it.id == id }
    fun recurring(id: String?): RecurringRule? = recurringRules.firstOrNull { it.id == id }
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
    settings = settings.copy(openingBalanceCents = 0L),
)
