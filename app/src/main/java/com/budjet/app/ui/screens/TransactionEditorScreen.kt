package com.budjet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.KeyboardBackspace
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.EditorDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.ThinDivider
import com.budjet.app.ui.components.TransactionRow
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.components.displayName
import com.budjet.app.ui.components.displayNote
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatFullDate
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.localizedName
import com.budjet.app.util.localizedNote
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TransactionEditorScreen(
    data: AppData,
    initial: EditorDraft,
    onClose: () -> Unit,
    onSave: (EditorDraft) -> Unit,
    onDelete: ((String) -> Unit)? = null,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val reversalPrefix = stringResource(R.string.reversal_note_prefix)
    val paymentFallback = stringResource(R.string.editor_payment)

    var amountCents by remember { mutableLongStateOf(initial.amountCents) }
    var type by remember { mutableStateOf(initial.type) }
    var categoryId by remember { mutableStateOf(initial.categoryId) }
    var accountId by remember { mutableStateOf(initial.accountId) }
    var date by remember { mutableStateOf(initial.date) }
    var note by remember { mutableStateOf(initial.note) }
    var reversalId by remember { mutableStateOf(initial.reversalOfId) }

    var showAccounts by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }
    var showReversalPicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val categories = data.categories
        .filter { it.type == type && !it.archived }
        .sortedBy { category ->
            val order = when (type) {
                TxType.INCOME -> listOf("salary", "refund", "reversal", "freelance", "invest", "gifts")
                TxType.EXPENSE -> listOf(
                    "food", "dining", "transport", "home", "bills", "leisure",
                    "shopping", "health", "beauty", "subs", "travel", "education", "other",
                )
            }
            order.indexOf(category.id).let { if (it < 0) 100 else it }
        }
    val selectedCategory = data.category(categoryId)
        ?: categories.firstOrNull()
    val selectedAccount = data.account(accountId) ?: data.accounts.firstOrNull()
    val reversalPayment = data.transactions.firstOrNull { it.id == reversalId }
    val isReversal = selectedCategory?.id == "reversal"
    val canSave = amountCents > 0 && (!isReversal || reversalId != null)
    val isEditing = initial.id != null

    fun pressDigit(digit: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        amountCents = (amountCents * 10 + digit).coerceAtMost(999_999_999_99L)
    }

    fun switchType(next: TxType) {
        if (type == next) return
        type = next
        reversalId = null
        val nextCategories = data.categories.filter { it.type == next && !it.archived }
        if (nextCategories.none { it.id == categoryId }) {
            nextCategories.firstOrNull()?.let { categoryId = it.id }
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SoftIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose,
                size = 38.dp,
                container = Color.Transparent,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Overline(
                    stringResource(if (isEditing) R.string.editor_editing else R.string.editor_new),
                )
                Text(
                    text = stringResource(if (isEditing) R.string.editor_edit else R.string.action_add),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(if (canSave) colors.primary else colors.surfaceMuted)
                    .clickable(enabled = canSave) {
                        onSave(
                            EditorDraft(
                                id = initial.id,
                                amountCents = amountCents,
                                type = type,
                                categoryId = selectedCategory?.id ?: categoryId,
                                accountId = selectedAccount?.id ?: accountId,
                                date = date,
                                note = note,
                                reversalOfId = reversalId.takeIf { isReversal },
                            )
                        )
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canSave) colors.onPrimary else colors.inkMuted,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceMuted)
                    .padding(4.dp),
            ) {
                TypeButton(
                    text = stringResource(R.string.type_expense),
                    selected = type == TxType.EXPENSE,
                    onClick = { switchType(TxType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                )
                TypeButton(
                    text = stringResource(R.string.type_income),
                    selected = type == TxType.INCOME,
                    onClick = { switchType(TxType.INCOME) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = formatMoney(amountCents, currency),
                modifier = Modifier.fillMaxWidth(),
                style = BudTheme.type.moneyHero.copy(fontSize = 46.sp, lineHeight = 52.sp),
                color = if (amountCents == 0L) colors.inkMuted.copy(alpha = 0.45f)
                else if (type == TxType.INCOME) colors.income else colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (type == TxType.EXPENSE) R.string.editor_expense_question else R.string.editor_income_question,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(22.dp))
            Overline(stringResource(R.string.label_category))
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("editor-categories"),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryOption(
                        category = category,
                        selected = selectedCategory?.id == category.id,
                        onClick = {
                            categoryId = category.id
                            if (category.id != "reversal") {
                                reversalId = null
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            BudCard(padding = 0.dp) {
                if (isReversal) {
                    DetailRow(
                        icon = Icons.Rounded.Block,
                        label = stringResource(R.string.editor_payment),
                        value = reversalPayment?.let { payment ->
                            val paymentCategory = data.category(payment.categoryId)?.displayName()
                                ?: stringResource(R.string.type_expense)
                            "${payment.note.displayNote().ifBlank { paymentCategory }} · ${formatMoney(payment.amountCents, currency)}"
                        } ?: stringResource(R.string.editor_search_payment),
                        muted = reversalPayment == null,
                        onClick = { showReversalPicker = true },
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 54.dp)
                            .height(1.dp)
                            .background(colors.line.copy(alpha = 0.7f)),
                    )
                }
                DetailRow(
                    icon = if (type == TxType.INCOME) Icons.Rounded.AccountBalance else Icons.Rounded.AccountBalance,
                    label = stringResource(R.string.label_account),
                    value = selectedAccount?.displayName() ?: stringResource(R.string.editor_choose_account),
                    onClick = { showAccounts = true },
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 54.dp)
                        .height(1.dp)
                        .background(colors.line.copy(alpha = 0.7f)),
                )
                DetailRow(
                    icon = Icons.Rounded.CalendarToday,
                    label = stringResource(R.string.action_date),
                    value = if (date == LocalDate.now()) {
                        stringResource(R.string.today_prefix, formatShortDate(date))
                    } else {
                        formatFullDate(date)
                    },
                    onClick = { showDatePicker = true },
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 54.dp)
                        .height(1.dp)
                        .background(colors.line.copy(alpha = 0.7f)),
                )
                DetailRow(
                    icon = Icons.Rounded.EditNote,
                    label = stringResource(R.string.label_note),
                    value = note.ifBlank { stringResource(R.string.editor_add_note) },
                    muted = note.isBlank(),
                    onClick = { showNote = true },
                )
            }

            if (isReversal && reversalPayment == null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.editor_reversal_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warning,
                )
            }

            if (date.isAfter(LocalDate.now())) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.warning.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.editor_scheduled_note, formatShortDate(date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.warning,
                    )
                }
            }

            if (isEditing) {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { confirmDelete = true }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = colors.expense,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_delete_transaction),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.expense,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Keypad(
            currency = currency,
            onQuickAmount = { value ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                amountCents = value
            },
            onDigit = { pressDigit(it) },
            onDoubleZero = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                amountCents = (amountCents * 100).coerceAtMost(999_999_999_99L)
            },
            onBackspace = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                amountCents /= 10
            },
        )
    }

    if (showAccounts) {
        AlertDialog(
            onDismissRequest = { showAccounts = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.label_account), style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                Column {
                    data.accounts.forEach { account ->
                        AccountChoice(
                            account = account,
                            selected = account.id == selectedAccount?.id,
                            onClick = {
                                accountId = account.id
                                showAccounts = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccounts = false }) { Text(stringResource(R.string.action_close), color = colors.primary) }
            },
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_done), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }

    if (showNote) {
        var noteValue by remember { mutableStateOf(note) }
        AlertDialog(
            onDismissRequest = { showNote = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.label_note), style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                OutlinedTextField(
                    value = noteValue,
                    onValueChange = { noteValue = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.editor_note_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions.Default,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        note = noteValue.trim()
                        showNote = false
                    },
                ) { Text(stringResource(R.string.action_save), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showNote = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
            },
        )
    }

    if (showReversalPicker) {
        ReversalPickerDialog(
            data = data,
            currency = currency,
            excludeTransactionId = initial.id,
            onDismiss = { showReversalPicker = false },
            onSelect = { payment ->
                type = TxType.INCOME
                categoryId = "reversal"
                reversalId = payment.id
                amountCents = payment.amountCents
                accountId = payment.accountId
                if (note.isBlank() || note.startsWith(reversalPrefix)) {
                    note = reversalPrefix +
                        payment.note.localizedNote(context).ifBlank {
                            data.category(payment.categoryId)?.localizedName(context) ?: paymentFallback
                        }
                }
                showReversalPicker = false
            },
        )
    }

    if (confirmDelete && initial.id != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    stringResource(R.string.editor_delete_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    stringResource(R.string.editor_delete_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete?.invoke(initial.id)
                    },
                ) { Text(stringResource(R.string.action_delete), color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
            },
        )
    }
}

@Composable
private fun ReversalPickerDialog(
    data: AppData,
    currency: Currency,
    excludeTransactionId: String?,
    onDismiss: () -> Unit,
    onSelect: (Transaction) -> Unit,
) {
    val colors = BudTheme.colors
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val today = remember { LocalDate.now() }
    val alreadyReversed = remember(data.transactions, excludeTransactionId) {
        data.transactions
            .filter { it.reversalOfId != null && it.id != excludeTransactionId }
            .mapNotNull { it.reversalOfId }
            .toSet()
    }
    val candidates = remember(data.transactions, alreadyReversed, query, today, context) {
        data.transactions
            .filter {
                it.type == TxType.EXPENSE &&
                    it.dateEpochDay <= today.toEpochDay() &&
                    it.id !in alreadyReversed
            }
            .filter { tx ->
                val category = data.category(tx.categoryId)?.localizedName(context).orEmpty()
                val account = data.account(tx.accountId)?.localizedName(context).orEmpty()
                val note = tx.note.localizedNote(context)
                query.isBlank() ||
                    note.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true) ||
                    category.contains(query, ignoreCase = true) ||
                    account.contains(query, ignoreCase = true) ||
                    formatMoney(tx.amountCents, currency).contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.dateEpochDay }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
        ) {
            Column(Modifier.padding(20.dp)) {
                Overline(stringResource(R.string.reversal_overline))
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.reversal_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.reversal_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.reversal_search_hint)) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = colors.inkMuted,
                            modifier = Modifier.size(17.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(8.dp))
                if (candidates.isEmpty()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.reversal_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkMuted,
                        )
                    }
                } else {
                    LazyColumn(Modifier.weight(1f).testTag("reversal-list")) {
                        items(candidates, key = { it.id }) { payment ->
                            TransactionRow(
                                transaction = payment,
                                category = data.category(payment.categoryId),
                                account = data.account(payment.accountId),
                                currency = currency,
                                onClick = { onSelect(payment) },
                                showDate = true,
                            )
                            if (payment.id != candidates.last().id) {
                                ThinDivider(Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) colors.surface else Color.Transparent)
            .border(
                1.dp,
                if (selected) colors.line else Color.Transparent,
                RoundedCornerShape(13.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.ink else colors.inkMuted,
        )
    }
}

@Composable
private fun CategoryOption(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    val color = categoryColor(category.colorHex, colors.isDark)
    Column(
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = if (colors.isDark) 0.18f else 0.13f))
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) color else Color.Transparent,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(category.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.displayName(),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = if (selected) colors.ink else colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    muted: Boolean = false,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.inkMuted,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkMuted,
            modifier = Modifier.width(52.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (muted) colors.inkMuted.copy(alpha = 0.8f) else colors.ink,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun AccountChoice(account: Account, selected: Boolean, onClick: () -> Unit) {
    val colors = BudTheme.colors
    val color = categoryColor(account.colorHex, colors.isDark)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(account.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            account.displayName(),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun Keypad(
    currency: Currency,
    onQuickAmount: (Long) -> Unit,
    onDigit: (Int) -> Unit,
    onDoubleZero: () -> Unit,
    onBackspace: () -> Unit,
) {
    val colors = BudTheme.colors
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "⌫"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(500L, 1_000L, 2_000L, 5_000L).forEach { value ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(CircleShape)
                        .border(1.dp, colors.line, CircleShape)
                        .clickable { onQuickAmount(value) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = formatMoney(value, currency),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkMuted,
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    when (key) {
                                        "⌫" -> onBackspace()
                                        "00" -> onDoubleZero()
                                        else -> onDigit(key.toInt())
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    Icons.Rounded.KeyboardBackspace,
                                    contentDescription = stringResource(R.string.action_clear),
                                    tint = colors.inkMuted,
                                    modifier = Modifier.size(21.dp),
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                    color = colors.ink,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
