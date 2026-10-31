package com.budjet.app.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.Backup
import com.budjet.app.data.BudRepository
import com.budjet.app.data.Budget
import com.budjet.app.data.Category
import com.budjet.app.data.DemoData
import com.budjet.app.data.Recurring
import com.budjet.app.data.RecurringRule
import com.budjet.app.data.SavingsGoal
import com.budjet.app.data.Settings
import com.budjet.app.data.ThemeMode
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.util.formatShortDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class EditorDraft(
    val id: String? = null,
    val amountCents: Long = 0,
    val type: TxType = TxType.EXPENSE,
    val categoryId: String = "food",
    val accountId: String = "main",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
)

data class RecurringDraft(
    val id: String? = null,
    val amountCents: Long = 0,
    val type: TxType = TxType.EXPENSE,
    val categoryId: String = "home",
    val accountId: String = "main",
    val dayOfMonth: Int = 1,
    val note: String = "",
    val active: Boolean = true,
)

data class GoalDraft(
    val id: String? = null,
    val name: String = "",
    val targetCents: Long = 0,
    val deadline: LocalDate? = null,
    val icon: String = "savings",
    val colorHex: String = "#2F6B54",
)

class AppViewModel(private val repository: BudRepository) : ViewModel() {

    /** True once the user has unlocked the current app session. Not persisted. */
    var sessionUnlocked: Boolean = false

    init {
        processRecurring()
    }

    val data: StateFlow<AppData> = repository.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = repository.data.value,
    )

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    fun setMonth(value: YearMonth) {
        _month.value = value
    }

    fun previousMonth() = _month.value.let { _month.value = it.minusMonths(1) }
    fun nextMonth() {
        val next = _month.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now())) _month.value = next
    }

    fun saveTransaction(draft: EditorDraft) {
        val cents = draft.amountCents
        if (cents <= 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val previous = current.transactions.firstOrNull { it.id == draft.id }
                val transaction = Transaction(
                    id = draft.id ?: UUID.randomUUID().toString(),
                    amountCents = cents,
                    type = draft.type,
                    categoryId = draft.categoryId,
                    accountId = draft.accountId,
                    dateEpochDay = draft.date.toEpochDay(),
                    note = draft.note.trim(),
                    recurringId = previous?.recurringId,
                )
                val list = if (draft.id == null) {
                    current.transactions + transaction
                } else {
                    current.transactions.map { if (it.id == draft.id) transaction else it }
                }
                current.copy(transactions = list.sortedByDescending { it.dateEpochDay })
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(transactions = current.transactions.filterNot { it.id == transaction.id })
            }
        }
    }

    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update { current ->
                if (current.transactions.any { it.id == transaction.id }) current
                else current.copy(transactions = (current.transactions + transaction).sortedByDescending { it.dateEpochDay })
            }
        }
    }

    // ---------------------------------------------------------------- recurrentes

    fun saveRecurring(draft: RecurringDraft) {
        if (draft.amountCents <= 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val existing = current.recurring(draft.id)
                val rule = RecurringRule(
                    id = existing?.id ?: "r_" + UUID.randomUUID().toString().take(8),
                    amountCents = draft.amountCents,
                    type = draft.type,
                    categoryId = draft.categoryId,
                    accountId = draft.accountId,
                    dayOfMonth = draft.dayOfMonth.coerceIn(1, 31),
                    note = draft.note.trim(),
                    startEpochDay = existing?.startEpochDay ?: LocalDate.now().toEpochDay(),
                    lastPostedMonth = existing?.lastPostedMonth,
                    active = draft.active,
                )
                val list = if (existing == null) {
                    current.recurringRules + rule
                } else {
                    current.recurringRules.map { if (it.id == existing.id) rule else it }
                }
                current.copy(recurringRules = list)
            }
            processRecurring()
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(recurringRules = current.recurringRules.filterNot { it.id == id })
            }
        }
    }

    fun setRecurringActive(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    recurringRules = current.recurringRules.map {
                        if (it.id == id) it.copy(active = active) else it
                    },
                )
            }
            if (active) processRecurring()
        }
    }

    /** Posts any rule that is due today. Safe to call repeatedly. */
    fun processRecurring() {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                val generated = mutableListOf<Transaction>()
                val updatedRules = current.recurringRules.map { rule ->
                    val due = Recurring.dueToday(rule, today) ?: return@map rule
                    val key = YearMonth.from(due)
                    val txId = "rec-${rule.id}-$key"
                    if (current.transactions.none { it.id == txId }) {
                        generated += Transaction(
                            id = txId,
                            amountCents = rule.amountCents,
                            type = rule.type,
                            categoryId = rule.categoryId,
                            accountId = rule.accountId,
                            dateEpochDay = due.toEpochDay(),
                            note = rule.note.ifBlank {
                                current.category(rule.categoryId)?.name ?: "Movimiento recurrente"
                            },
                            recurringId = rule.id,
                        )
                    }
                    rule.copy(lastPostedMonth = key.toString())
                }
                if (generated.isEmpty()) current
                else current.copy(
                    recurringRules = updatedRules,
                    transactions = (current.transactions + generated).sortedByDescending { it.dateEpochDay },
                )
            }
        }
    }

    // ---------------------------------------------------------------- objetivos

    fun saveGoal(draft: GoalDraft) {
        if (draft.targetCents <= 0L || draft.name.isBlank()) return
        viewModelScope.launch {
            repository.update { current ->
                val existing = current.goal(draft.id)
                val goal = SavingsGoal(
                    id = existing?.id ?: "g_" + UUID.randomUUID().toString().take(8),
                    name = draft.name.trim(),
                    targetCents = draft.targetCents,
                    savedCents = existing?.savedCents ?: 0L,
                    deadlineEpochDay = draft.deadline?.toEpochDay(),
                    icon = draft.icon,
                    colorHex = draft.colorHex,
                )
                val list = if (existing == null) {
                    current.savingsGoals + goal
                } else {
                    current.savingsGoals.map { if (it.id == existing.id) goal else it }
                }
                current.copy(savingsGoals = list)
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(savingsGoals = current.savingsGoals.filterNot { it.id == id })
            }
        }
    }

    /** Positive delta adds money, negative withdraws it (never below zero). */
    fun adjustGoal(id: String, deltaCents: Long) {
        if (deltaCents == 0L) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    savingsGoals = current.savingsGoals.map {
                        if (it.id == id) it.copy(savedCents = (it.savedCents + deltaCents).coerceAtLeast(0L)) else it
                    },
                )
            }
        }
    }

    fun setBudget(categoryId: String?, limitCents: Long) {
        viewModelScope.launch {
            repository.update { current ->
                val without = current.budgets.filterNot { it.categoryId == categoryId }
                val updated = if (limitCents <= 0L) without else without + Budget(
                    id = "b_" + (categoryId ?: "overall"),
                    categoryId = categoryId,
                    limitCents = limitCents,
                )
                current.copy(budgets = updated)
            }
        }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            repository.update { current -> current.copy(settings = transform(current.settings)) }
        }
    }

    fun setName(name: String) = updateSettings { it.copy(name = name.ifBlank { "Tú" }) }
    fun setCurrency(code: String) = updateSettings { it.copy(currencyCode = code) }
    fun setTheme(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setBiometricLock(enabled: Boolean) = updateSettings { it.copy(biometricLock = enabled) }

    fun addCategory(name: String, icon: String, colorHex: String, type: TxType) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                val category = Category(
                    id = "c_" + UUID.randomUUID().toString().take(8),
                    name = clean,
                    icon = icon,
                    colorHex = colorHex,
                    type = type,
                )
                current.copy(categories = current.categories + category)
            }
        }
    }

    fun addAccount(name: String, icon: String, colorHex: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                val account = Account(
                    id = "a_" + UUID.randomUUID().toString().take(8),
                    name = clean,
                    icon = icon,
                    colorHex = colorHex,
                )
                current.copy(accounts = current.accounts + account)
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.replaceAll(DemoData.build())
            _month.value = YearMonth.now()
            processRecurring()
        }
    }

    // ---------------------------------------------------------------- copia de seguridad

    fun buildBackup(): String = Backup.encode(data.value)

    fun parseBackup(text: String): Result<AppData> = Backup.decode(text)

    fun restoreBackup(backup: AppData) {
        viewModelScope.launch {
            repository.replaceAll(
                backup.copy(settings = backup.settings.copy(biometricLock = false)),
            )
            _month.value = YearMonth.now()
        }
    }

    fun clearEverything() {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(transactions = emptyList())
            }
        }
    }

    /** Writes a CSV to the cache dir and returns a share intent for it. */
    fun buildExport(context: Context, data: AppData): Intent? {
        return runCatching {
            val folder = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(folder, "budjet-${LocalDate.now()}.csv")
            val header = "Fecha;Tipo;Categoría;Cuenta;Importe;Nota\n"
            val rows = data.transactions.sortedBy { it.dateEpochDay }.joinToString("\n") { tx ->
                val category = data.category(tx.categoryId)?.name ?: ""
                val account = data.account(tx.accountId)?.name ?: ""
                val amount = (if (tx.type == TxType.INCOME) "" else "-") +
                    (tx.amountCents / 100) + "," + (tx.amountCents % 100).toString().padStart(2, '0')
                val note = tx.note.replace(";", ",")
                "${formatShortDate(LocalDate.ofEpochDay(tx.dateEpochDay))};" +
                    "${if (tx.type == TxType.INCOME) "Ingreso" else "Gasto"};" +
                    "$category;$account;$amount;$note"
            }
            file.writeText(header + rows)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.getOrNull()
    }

    companion object {
        fun factory(repository: BudRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AppViewModel(repository) as T
            }
    }
}
