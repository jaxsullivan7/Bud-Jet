package com.budjet.app.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.BudRepository
import com.budjet.app.data.Budget
import com.budjet.app.data.Category
import com.budjet.app.data.DemoData
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

class AppViewModel(private val repository: BudRepository) : ViewModel() {

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
                val transaction = Transaction(
                    id = draft.id ?: UUID.randomUUID().toString(),
                    amountCents = cents,
                    type = draft.type,
                    categoryId = draft.categoryId,
                    accountId = draft.accountId,
                    dateEpochDay = draft.date.toEpochDay(),
                    note = draft.note.trim(),
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
