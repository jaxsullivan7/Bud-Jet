package com.budjet.app.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.AutoBackup
import com.budjet.app.data.AutoBackupInfo
import com.budjet.app.data.Backup
import com.budjet.app.data.BudRepository
import com.budjet.app.data.Budget
import com.budjet.app.data.Category
import com.budjet.app.data.CategoryRule
import com.budjet.app.data.CryptoBackup
import com.budjet.app.data.DemoData
import com.budjet.app.data.Finance
import com.budjet.app.data.ImportRow
import com.budjet.app.data.Investment
import com.budjet.app.data.InvestmentEntry
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.data.InvestmentKind
import com.budjet.app.data.InvestmentMath
import com.budjet.app.data.IrpfRegion
import com.budjet.app.data.Liability
import com.budjet.app.data.LiabilityKind
import com.budjet.app.data.LiabilityPayment
import com.budjet.app.data.PayrollKind
import com.budjet.app.data.Payslip
import com.budjet.app.data.Recurring
import com.budjet.app.data.RecurringRule
import com.budjet.app.data.RecurringFrequency
import com.budjet.app.data.SavingsGoal
import com.budjet.app.data.Settings
import com.budjet.app.data.ThemeMode
import com.budjet.app.data.ThemePalette
import com.budjet.app.data.Transaction
import com.budjet.app.data.Transfer
import com.budjet.app.data.TxType
import com.budjet.app.data.clearedFinancialData
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.localizedName
import com.budjet.app.util.localizedNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.UUID

data class EditorDraft(
    val id: String? = null,
    val amountCents: Long = 0,
    val type: TxType = TxType.EXPENSE,
    val categoryId: String = "food",
    val accountId: String = "main",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val reversalOfId: String? = null,
)

data class RecurringDraft(
    val id: String? = null,
    val amountCents: Long = 0,
    val type: TxType = TxType.EXPENSE,
    val categoryId: String = "home",
    val accountId: String = "main",
    val dayOfMonth: Int = 1,
    val monthOfYear: Int = 1,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
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

data class InvestmentEntryDraft(
    val investmentId: String,
    val kind: InvestmentEntryKind,
    val amountCents: Long,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    /** Optional cash account linked to the contribution/withdrawal. */
    val accountId: String? = null,
)

data class TransferDraft(
    val fromAccountId: String? = null,
    val fromInvestmentId: String? = null,
    val toAccountId: String? = null,
    val toInvestmentId: String? = null,
    val amountCents: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
)

data class LiabilityDraft(
    val id: String? = null,
    val name: String = "",
    val kind: LiabilityKind = LiabilityKind.OTHER,
    val balanceCents: Long = 0L,
    val annualRatePercent: Double = 0.0,
    val minimumPaymentCents: Long = 0L,
)

data class LiabilityPaymentDraft(
    val liabilityId: String,
    val accountId: String,
    val amountCents: Long,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
)

data class PayrollDraft(
    val id: String? = null,
    val date: LocalDate = LocalDate.now(),
    val employer: String = "",
    val kind: PayrollKind = PayrollKind.MONTHLY,
    val grossCents: Long = 0L,
    val irpfCents: Long = 0L,
    val socialSecurityCents: Long = 0L,
    val otherDeductionsCents: Long = 0L,
    val accountId: String = "",
    val note: String = "",
) {
    val netCents: Long
        get() = grossCents - irpfCents - socialSecurityCents - otherDeductionsCents
}

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

    fun saveTransaction(draft: EditorDraft): kotlinx.coroutines.Job? {
        val cents = draft.amountCents
        if (cents <= 0L) return null
        return viewModelScope.launch {
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
                    reversalOfId = draft.reversalOfId,
                    createdAtEpochMillis = previous?.createdAtEpochMillis?.takeIf { it > 0L }
                        ?: System.currentTimeMillis(),
                )
                val list = if (draft.id == null) {
                    current.transactions + transaction
                } else {
                    current.transactions.map { if (it.id == draft.id) transaction else it }
                }
                current.copy(
                    transactions = list.sortedByDescending { it.dateEpochDay },
                    settings = current.settings.copy(lastMovementDateEpochDay = draft.date.toEpochDay()),
                )
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
                    monthOfYear = draft.monthOfYear.coerceIn(1, 12),
                    frequency = draft.frequency,
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
                    val key = Recurring.periodKey(due, rule.frequency)
                    val txId = "rec-${rule.id}-$key"
                    if (current.transactions.none { it.id == txId }) {
                        generated += Transaction(
                            id = txId,
                            amountCents = rule.amountCents,
                            type = rule.type,
                            categoryId = rule.categoryId,
                            accountId = rule.accountId,
                            dateEpochDay = due.toEpochDay(),
                            note = rule.note.localizedNote(repository.context).ifBlank {
                                current.category(rule.categoryId)?.localizedName(repository.context)
                                    ?: repository.context.getString(R.string.generated_recurring_fallback)
                            },
                            recurringId = rule.id,
                            createdAtEpochMillis = System.currentTimeMillis(),
                        )
                    }
                    rule.copy(lastPostedMonth = key)
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
                    customName = existing?.let {
                        it.customName || it.name != draft.name.trim()
                    } ?: true,
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

    // ---------------------------------------------------------------- cartera

    fun addInvestment(name: String, kind: InvestmentKind) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    investments = current.investments + Investment(
                        id = "inv_" + UUID.randomUUID().toString().take(8),
                        name = clean,
                        kind = kind,
                        createdAtEpochDay = LocalDate.now().toEpochDay(),
                        customName = true,
                    ),
                )
            }
        }
    }

    fun updateInvestment(id: String, name: String, kind: InvestmentKind) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    investments = current.investments.map {
                        if (it.id == id) {
                            it.copy(
                                name = clean,
                                kind = kind,
                                customName = it.customName || it.name != clean,
                            )
                        } else {
                            it
                        }
                    },
                )
            }
        }
    }

    fun deleteInvestment(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                val linkedTransfers = current.investmentEntries
                    .filter { it.investmentId == id }
                    .mapNotNull { it.transferId }
                    .toSet()
                current.copy(
                    investments = current.investments.filterNot { it.id == id },
                    investmentEntries = current.investmentEntries.filterNot { it.investmentId == id },
                    transfers = current.transfers.filterNot { it.id in linkedTransfers },
                )
            }
        }
    }

    fun addInvestmentEntry(draft: InvestmentEntryDraft) {
        val signed = when (draft.kind) {
            InvestmentEntryKind.CONTRIBUTION,
            InvestmentEntryKind.WITHDRAWAL,
            -> draft.amountCents.coerceAtLeast(0L)
            InvestmentEntryKind.RETURN,
            InvestmentEntryKind.VALUATION,
            -> draft.amountCents
        }
        if (signed == 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val accountId = draft.accountId
                if (accountId != null && draft.kind in setOf(
                        InvestmentEntryKind.CONTRIBUTION,
                        InvestmentEntryKind.WITHDRAWAL,
                    )
                ) {
                    val available = when (draft.kind) {
                        InvestmentEntryKind.CONTRIBUTION ->
                            Finance.accountBalanceAt(current, accountId, draft.date)
                        else ->
                            InvestmentMath.summary(
                                current.entriesFor(draft.investmentId),
                                draft.date,
                            ).valueCents
                    }
                    if (signed > available) return@update current
                }
                val entryId = "ie_" + UUID.randomUUID().toString().take(8)
                val transfer = if (accountId != null) {
                    when (draft.kind) {
                        InvestmentEntryKind.CONTRIBUTION -> Transfer(
                            id = "tr_" + UUID.randomUUID().toString().take(8),
                            amountCents = signed,
                            dateEpochDay = draft.date.toEpochDay(),
                            fromAccountId = accountId,
                            toInvestmentId = draft.investmentId,
                            note = draft.note.trim().ifBlank {
                                repository.context.getString(
                                    R.string.generated_investment_contribution,
                                    current.investment(draft.investmentId)?.localizedName(repository.context)
                                        ?: repository.context.getString(R.string.generated_investment_fallback),
                                )
                            },
                        )
                        InvestmentEntryKind.WITHDRAWAL -> Transfer(
                            id = "tr_" + UUID.randomUUID().toString().take(8),
                            amountCents = signed,
                            dateEpochDay = draft.date.toEpochDay(),
                            fromInvestmentId = draft.investmentId,
                            toAccountId = accountId,
                            note = draft.note.trim().ifBlank {
                                repository.context.getString(
                                    R.string.generated_investment_withdrawal,
                                    current.investment(draft.investmentId)?.localizedName(repository.context)
                                        ?: repository.context.getString(R.string.generated_investment_fallback),
                                )
                            },
                        )
                        else -> null
                    }
                } else null
                current.copy(
                    investmentEntries = current.investmentEntries + InvestmentEntry(
                        id = entryId,
                        investmentId = draft.investmentId,
                        kind = draft.kind,
                        amountCents = signed,
                        dateEpochDay = draft.date.toEpochDay(),
                        note = draft.note.trim(),
                        transferId = transfer?.id,
                    ),
                    transfers = if (transfer == null) current.transfers else current.transfers + transfer,
                )
            }
        }
    }

    fun addTransfer(draft: TransferDraft) {
        val amount = draft.amountCents
        if (amount <= 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val hasSource = (draft.fromAccountId != null) != (draft.fromInvestmentId != null)
                val hasTarget = (draft.toAccountId != null) != (draft.toInvestmentId != null)
                if (!hasSource || !hasTarget) return@update current
                if (draft.fromInvestmentId != null && draft.toInvestmentId != null) return@update current
                if (draft.fromAccountId != null) {
                    if (Finance.accountBalanceAt(current, draft.fromAccountId, draft.date) < amount) return@update current
                }
                if (draft.fromInvestmentId != null) {
                    val value = InvestmentMath.summary(
                        current.entriesFor(draft.fromInvestmentId),
                        draft.date,
                    ).valueCents
                    if (value < amount) return@update current
                }
                val transferId = "tr_" + UUID.randomUUID().toString().take(8)
                val transfer = Transfer(
                    id = transferId,
                    amountCents = amount,
                    dateEpochDay = draft.date.toEpochDay(),
                    fromAccountId = draft.fromAccountId,
                    fromInvestmentId = draft.fromInvestmentId,
                    toAccountId = draft.toAccountId,
                    toInvestmentId = draft.toInvestmentId,
                    note = draft.note.trim(),
                )
                val linkedEntry = when {
                    draft.toInvestmentId != null && draft.fromAccountId != null -> InvestmentEntry(
                        id = "ie_" + UUID.randomUUID().toString().take(8),
                        investmentId = draft.toInvestmentId,
                        kind = InvestmentEntryKind.CONTRIBUTION,
                        amountCents = amount,
                        dateEpochDay = draft.date.toEpochDay(),
                        note = draft.note.trim().ifBlank {
                            repository.context.getString(R.string.generated_transfer_from_cash)
                        },
                        transferId = transferId,
                    )
                    draft.fromInvestmentId != null && draft.toAccountId != null -> InvestmentEntry(
                        id = "ie_" + UUID.randomUUID().toString().take(8),
                        investmentId = draft.fromInvestmentId,
                        kind = InvestmentEntryKind.WITHDRAWAL,
                        amountCents = amount,
                        dateEpochDay = draft.date.toEpochDay(),
                        note = draft.note.trim().ifBlank {
                            repository.context.getString(R.string.generated_transfer_to_cash)
                        },
                        transferId = transferId,
                    )
                    else -> null
                }
                current.copy(
                    transfers = current.transfers + transfer,
                    investmentEntries = if (linkedEntry == null) {
                        current.investmentEntries
                    } else {
                        current.investmentEntries + linkedEntry
                    },
                )
            }
        }
    }

    fun saveLiability(draft: LiabilityDraft) {
        if (draft.name.isBlank() || draft.balanceCents < 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val existing = current.liability(draft.id)
                val liability = Liability(
                    id = existing?.id ?: "l_" + UUID.randomUUID().toString().take(8),
                    name = draft.name.trim(),
                    kind = draft.kind,
                    balanceCents = draft.balanceCents,
                    annualRatePercent = draft.annualRatePercent.coerceIn(0.0, 100.0),
                    minimumPaymentCents = draft.minimumPaymentCents.coerceAtLeast(0L),
                    createdAtEpochDay = existing?.createdAtEpochDay ?: LocalDate.now().toEpochDay(),
                    customName = existing?.let {
                        it.customName || it.name != draft.name.trim()
                    } ?: true,
                )
                current.copy(
                    liabilities = if (existing == null) {
                        current.liabilities + liability
                    } else {
                        current.liabilities.map { if (it.id == existing.id) liability else it }
                    },
                )
            }
        }
    }

    fun deleteLiability(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    liabilities = current.liabilities.filterNot { it.id == id },
                    liabilityPayments = current.liabilityPayments.filterNot { it.liabilityId == id },
                )
            }
        }
    }

    fun payLiability(draft: LiabilityPaymentDraft) {
        if (draft.amountCents <= 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val liability = current.liability(draft.liabilityId) ?: return@update current
                if (draft.amountCents > liability.balanceCents) return@update current
                if (Finance.accountBalanceAt(current, draft.accountId, draft.date) < draft.amountCents) {
                    return@update current
                }
                current.copy(
                    liabilities = current.liabilities.map {
                        if (it.id == liability.id) {
                            it.copy(balanceCents = (it.balanceCents - draft.amountCents).coerceAtLeast(0L))
                        } else {
                            it
                        }
                    },
                    liabilityPayments = current.liabilityPayments + LiabilityPayment(
                        id = "lp_" + UUID.randomUUID().toString().take(8),
                        liabilityId = liability.id,
                        accountId = draft.accountId,
                        amountCents = draft.amountCents,
                        dateEpochDay = draft.date.toEpochDay(),
                        note = draft.note.trim(),
                    ),
                )
            }
        }
    }

    fun updateTransactionsDate(ids: Set<String>, date: LocalDate) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    transactions = current.transactions.map {
                        if (it.id in ids) it.copy(dateEpochDay = date.toEpochDay()) else it
                    }.sortedByDescending { it.dateEpochDay },
                    settings = current.settings.copy(lastMovementDateEpochDay = date.toEpochDay()),
                )
            }
        }
    }

    fun deleteTransactions(ids: Set<String>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(transactions = current.transactions.filterNot { it.id in ids })
            }
        }
    }

    fun addCategoryRule(pattern: String, categoryId: String) {
        val clean = pattern.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    categoryRules = current.categoryRules + CategoryRule(
                        id = "cr_" + UUID.randomUUID().toString().take(8),
                        pattern = clean,
                        categoryId = categoryId,
                    ),
                )
            }
        }
    }

    fun deleteCategoryRule(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(categoryRules = current.categoryRules.filterNot { it.id == id })
            }
        }
    }

    fun importTransactions(rows: List<ImportRow>, accountId: String) {
        if (rows.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                val existing = current.transactions.mapNotNull { it.importHash }.toSet()
                val fresh = rows.filter { !it.duplicate && it.hash !in existing }.mapIndexed { index, row ->
                    Transaction(
                        id = "imp_" + UUID.randomUUID().toString().take(10),
                        amountCents = row.amountCents,
                        type = row.type,
                        categoryId = row.categoryId,
                        accountId = accountId,
                        dateEpochDay = row.date.toEpochDay(),
                        note = row.note,
                        importHash = row.hash,
                        createdAtEpochMillis = System.currentTimeMillis() + index,
                    )
                }
                if (fresh.isEmpty()) current else current.copy(
                    transactions = (current.transactions + fresh).sortedByDescending { it.dateEpochDay },
                    settings = current.settings.copy(
                        lastMovementDateEpochDay = fresh.maxOf { it.dateEpochDay },
                    ),
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

    fun setName(name: String) = updateSettings {
        it.copy(name = name.ifBlank { repository.context.getString(R.string.generated_default_name) })
    }
    fun setCurrency(code: String) = updateSettings { it.copy(currencyCode = code) }
    fun setTheme(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setPalette(palette: ThemePalette) = updateSettings { it.copy(palette = palette) }
    fun setBiometricLock(enabled: Boolean) = updateSettings { it.copy(biometricLock = enabled) }
    fun setOpeningBalance(cents: Long) = updateSettings { it.copy(openingBalanceCents = cents.coerceAtLeast(0L)) }
    fun setAnalysisStart(date: LocalDate?) = updateSettings { it.copy(analysisStartEpochDay = date?.toEpochDay()) }
    fun setIrpfRegion(region: IrpfRegion) = updateSettings { it.copy(irpfRegion = region) }

    fun savePayroll(draft: PayrollDraft) {
        if (draft.grossCents <= 0L || draft.accountId.isBlank() || draft.netCents < 0L) return
        viewModelScope.launch {
            repository.update { current ->
                val existing = current.payslips.firstOrNull { it.id == draft.id }
                val transactionId = existing?.transactionId
                    ?: "pay_" + UUID.randomUUID().toString().take(8)
                val employer = draft.employer.trim()
                val note = draft.note.trim().ifBlank {
                    employer.ifBlank { repository.context.getString(R.string.payroll_default_note) }
                }
                val salaryCategory = if (current.category("salary") != null) {
                    "salary"
                } else {
                    current.categories.firstOrNull { it.type == TxType.INCOME }?.id.orEmpty()
                }
                val transaction = Transaction(
                    id = transactionId,
                    amountCents = draft.netCents,
                    type = TxType.INCOME,
                    categoryId = salaryCategory,
                    accountId = draft.accountId,
                    dateEpochDay = draft.date.toEpochDay(),
                    note = note,
                    createdAtEpochMillis = System.currentTimeMillis(),
                )
                val payslip = Payslip(
                    id = existing?.id ?: "p_" + UUID.randomUUID().toString().take(8),
                    dateEpochDay = draft.date.toEpochDay(),
                    employer = employer,
                    kind = draft.kind,
                    grossCents = draft.grossCents,
                    irpfCents = draft.irpfCents,
                    socialSecurityCents = draft.socialSecurityCents,
                    otherDeductionsCents = draft.otherDeductionsCents,
                    accountId = draft.accountId,
                    transactionId = transactionId,
                    note = draft.note.trim(),
                )
                current.copy(
                    payslips = if (existing == null) {
                        current.payslips + payslip
                    } else {
                        current.payslips.map { if (it.id == existing.id) payslip else it }
                    },
                    transactions = if (current.transactions.any { it.id == transactionId }) {
                        current.transactions.map {
                            if (it.id == transactionId) transaction else it
                        }
                    } else {
                        current.transactions + transaction
                    },
                )
            }
        }
    }

    fun deletePayroll(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                val payslip = current.payslips.firstOrNull { it.id == id }
                current.copy(
                    payslips = current.payslips.filterNot { it.id == id },
                    transactions = current.transactions.filterNot {
                        it.id == payslip?.transactionId
                    },
                )
            }
        }
    }

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
                    customName = true,
                )
                current.copy(categories = current.categories + category)
            }
        }
    }

    fun addAccount(name: String, icon: String, colorHex: String, initialBalanceCents: Long) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                val account = Account(
                    id = "a_" + UUID.randomUUID().toString().take(8),
                    name = clean,
                    icon = icon,
                    colorHex = colorHex,
                    initialBalanceCents = initialBalanceCents.coerceAtLeast(0L),
                    customName = true,
                )
                current.copy(accounts = current.accounts + account)
            }
        }
    }

    fun updateAccount(
        id: String,
        name: String,
        icon: String,
        colorHex: String,
        initialBalanceCents: Long,
    ) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    accounts = current.accounts.map {
                        if (it.id == id) {
                            it.copy(
                                name = clean,
                                icon = icon,
                                colorHex = colorHex,
                                initialBalanceCents = initialBalanceCents.coerceAtLeast(0L),
                                customName = it.customName || clean != it.name,
                            )
                        } else {
                            it
                        }
                    },
                )
            }
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                val first = current.accounts.firstOrNull()?.id
                val inUse = current.transactions.any { it.accountId == id } ||
                    current.transfers.any { it.fromAccountId == id || it.toAccountId == id } ||
                    current.recurringRules.any { it.accountId == id } ||
                    current.liabilityPayments.any { it.accountId == id }
                if (id == first || inUse) {
                    current
                } else {
                    current.copy(accounts = current.accounts.filterNot { it.id == id })
                }
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.replaceAll(DemoData.build(context = repository.context))
            _month.value = YearMonth.now()
            processRecurring()
        }
    }

    // ---------------------------------------------------------------- copia de seguridad

    fun buildBackup(): String = Backup.encode(data.value)

    fun parseBackup(text: String): Result<AppData> = Backup.decode(text)

    fun buildEncryptedBackup(password: String): ByteArray =
        CryptoBackup.encrypt(Backup.encode(data.value).toByteArray(Charsets.UTF_8), password.toCharArray())

    fun parseEncryptedBackup(bytes: ByteArray, password: String): Result<AppData> =
        CryptoBackup.decrypt(bytes, password.toCharArray())
            .mapCatching { Backup.decode(String(it, Charsets.UTF_8)).getOrThrow() }

    fun autoBackups(context: Context): List<AutoBackupInfo> = AutoBackup.list(context)

    fun restoreAutoBackup(context: Context, name: String): Result<AppData> =
        AutoBackup.restore(context, name)

    fun createAutoBackupNow(context: Context): Result<java.io.File> =
        AutoBackup.maybeCreate(context, data.value)

    fun restoreBackup(backup: AppData) {
        viewModelScope.launch {
            repository.replaceAll(
                backup.copy(settings = backup.settings.copy(biometricLock = false)),
            )
            _month.value = YearMonth.now()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) = updateSettings {
        it.copy(notificationsEnabled = enabled, notificationPermissionAsked = true)
    }
    fun setBudgetAlerts(enabled: Boolean) = updateSettings { it.copy(budgetAlerts = enabled) }
    fun setRecurringAlerts(enabled: Boolean) = updateSettings { it.copy(recurringAlerts = enabled) }
    fun setMonthlyReviewAlerts(enabled: Boolean) = updateSettings { it.copy(monthlyReviewAlerts = enabled) }
    fun setValuationAlerts(enabled: Boolean) = updateSettings { it.copy(valuationAlerts = enabled) }
    fun setAutoBackupEnabled(enabled: Boolean) = updateSettings { it.copy(autoBackupEnabled = enabled) }
    fun markNotificationPermissionAsked() = updateSettings { it.copy(notificationPermissionAsked = true) }

    fun startFresh() {
        viewModelScope.launch {
            repository.update { current -> current.clearedFinancialData() }
        }
    }

    /** Writes a CSV to the cache dir and returns a share intent for it. */
    fun buildExport(context: Context, data: AppData): Intent? {
        return runCatching {
            val folder = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(folder, "budjet-${LocalDate.now()}.csv")
            val header = context.getString(R.string.export_csv_header) + "\n"
            val rows = data.transactions.sortedBy { it.dateEpochDay }.joinToString("\n") { tx ->
                val category = data.category(tx.categoryId)?.localizedName(context) ?: ""
                val account = data.account(tx.accountId)?.localizedName(context) ?: ""
                val amount = (if (tx.type == TxType.INCOME) "" else "-") +
                    String.format(Locale.getDefault(), "%.2f", tx.amountCents / 100.0)
                val note = tx.note.localizedNote(context).replace(";", ",")
                "${formatShortDate(LocalDate.ofEpochDay(tx.dateEpochDay))};" +
                    "${context.getString(if (tx.type == TxType.INCOME) R.string.type_income else R.string.type_expense)};" +
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
