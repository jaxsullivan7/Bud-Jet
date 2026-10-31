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
data class Settings(
    val name: String = "Jax",
    val currencyCode: String = "EUR",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val openingBalanceCents: Long = 385_000,
    val biometricLock: Boolean = false,
)

@Serializable
data class AppData(
    val version: Int = 3,
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
