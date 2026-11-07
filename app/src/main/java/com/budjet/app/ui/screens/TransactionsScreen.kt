package com.budjet.app.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.MonthSelector
import com.budjet.app.ui.components.ScreenTitle
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.ThinDivider
import com.budjet.app.ui.components.TransactionRow
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatDayLabel
import com.budjet.app.util.formatDayNumber
import com.budjet.app.util.formatMoney
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

private enum class TxFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    EXPENSE(R.string.filter_expenses),
    INCOME(R.string.filter_income),
}

private enum class MovementSort(
    @StringRes val labelRes: Int,
    @StringRes val shortRes: Int,
) {
    DATE(R.string.sort_date, R.string.sort_date_short),
    REGISTERED(R.string.sort_registered, R.string.sort_registered_short),
    AMOUNT_DESC(R.string.sort_amount_desc, R.string.sort_amount_desc_short),
    AMOUNT_ASC(R.string.sort_amount_asc, R.string.sort_amount_asc_short),
}

@Composable
fun TransactionsScreen(
    data: AppData,
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
    onChangeTransactionsDate: (Set<String>, LocalDate) -> Unit,
    onDeleteTransactions: (Set<String>) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    var filter by rememberSaveable { mutableStateOf(TxFilter.ALL) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkDate by rememberSaveable { mutableStateOf(false) }
    var confirmBulkDelete by rememberSaveable { mutableStateOf(false) }
    var sort by remember { mutableStateOf(MovementSort.DATE) }
    var sortMenu by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val selectionMode = selectedIds.isNotEmpty()

    LaunchedEffect(month, filter, query, sort) {
        selectedIds = emptySet()
    }

    val allMonthTx = remember(data.transactions, month) { Finance.transactionsOfMonth(data, month) }
    val postedMonthTx = remember(data.transactions, month) {
        Finance.postedTransactionsOfMonth(data, month)
    }
    val futureCount = allMonthTx.size - postedMonthTx.size
    val filtered = remember(allMonthTx, filter, query) {
        allMonthTx.filter { tx ->
            val matchesType = when (filter) {
                TxFilter.ALL -> true
                TxFilter.EXPENSE -> tx.type == TxType.EXPENSE
                TxFilter.INCOME -> tx.type == TxType.INCOME
            }
            val categoryName = data.category(tx.categoryId)?.name.orEmpty()
            val accountName = data.account(tx.accountId)?.name.orEmpty()
            val matchesQuery = query.isBlank() ||
                tx.note.contains(query, ignoreCase = true) ||
                categoryName.contains(query, ignoreCase = true) ||
                accountName.contains(query, ignoreCase = true)
            matchesType && matchesQuery
        }
    }
    val sorted = remember(filtered, sort) {
        when (sort) {
            MovementSort.DATE -> filtered.sortedWith(
                compareByDescending<Transaction> { it.dateEpochDay }
                    .thenByDescending { it.createdAtEpochMillis }
                    .thenByDescending { it.id },
            )
            MovementSort.REGISTERED -> filtered.sortedWith(
                compareByDescending<Transaction> { it.createdAtEpochMillis }
                    .thenByDescending { it.dateEpochDay }
                    .thenByDescending { it.id },
            )
            MovementSort.AMOUNT_DESC -> filtered.sortedWith(
                compareByDescending<Transaction> { it.amountCents }
                    .thenByDescending { it.dateEpochDay }
                    .thenByDescending { it.createdAtEpochMillis },
            )
            MovementSort.AMOUNT_ASC -> filtered.sortedWith(
                compareBy<Transaction> { it.amountCents }
                    .thenByDescending { it.dateEpochDay }
                    .thenByDescending { it.createdAtEpochMillis },
            )
        }
    }
    val groupedForDate = remember(sorted, sort) {
        if (sort == MovementSort.DATE) {
            sorted.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
                .toSortedMap(compareByDescending { it })
        } else {
            emptyMap()
        }
    }
    val summarySource = remember(filtered, query, postedMonthTx, today) {
        if (query.isNotBlank()) {
            filtered.filter { it.dateEpochDay <= today.toEpochDay() }
        } else {
            postedMonthTx
        }
    }
    val totals = remember(summarySource) { Finance.totals(summarySource) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            ScreenTitle(
                overline = stringResource(R.string.transactions_overline),
                title = stringResource(R.string.nav_transactions),
                trailing = {
                    if (selectionMode) {
                        SoftIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_cancel_selection),
                            onClick = { selectedIds = emptySet() },
                            size = 38.dp,
                            container = colors.primarySoft,
                        )
                    } else {
                        SoftIconButton(
                            icon = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = stringResource(
                                if (showSearch) R.string.action_close_search else R.string.action_search,
                            ),
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) query = ""
                            },
                            size = 38.dp,
                            container = if (showSearch) colors.primarySoft else colors.surface,
                        )
                    }
                },
            )
        }

        item {
            Spacer(Modifier.height(18.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MonthSelector(
                        month = month,
                        onPrevious = onPreviousMonth,
                        onNext = onNextMonth,
                        canGoNext = month.isBefore(YearMonth.now()),
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.transactions_count,
                            allMonthTx.size,
                            allMonthTx.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.transactions_sort_by),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    SortControl(
                        sort = sort,
                        expanded = sortMenu,
                        onExpandedChange = { sortMenu = it },
                        onSelect = { sort = it; sortMenu = false },
                    )
                }
            }
        }

        if (selectionMode) {
            item {
                Spacer(Modifier.height(12.dp))
                BulkSelectionBar(
                    count = selectedIds.size,
                    onDate = { showBulkDate = true },
                    onDelete = { confirmBulkDelete = true },
                    onCancel = { selectedIds = emptySet() },
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = showSearch,
                enter = slideInVertically { -it / 2 } + fadeIn(),
                exit = slideOutVertically { -it / 2 } + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    SearchField(query = query, onQueryChange = { query = it })
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            BudCard(padding = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SummaryCell(
                        label = stringResource(R.string.overline_income),
                        value = totals.income,
                        color = colors.income,
                        currency = currency,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCell(
                        label = stringResource(R.string.overline_expenses),
                        value = totals.expense,
                        color = colors.expense,
                        currency = currency,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCell(
                        label = stringResource(R.string.overline_balance),
                        value = totals.balance,
                        color = if (totals.balance >= 0) colors.income else colors.expense,
                        currency = currency,
                        signed = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (query.isNotBlank()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.transactions_search_summary,
                        summarySource.size,
                        summarySource.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                )
            }
        }

        if (futureCount > 0) {
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.transactions_scheduled_note,
                        futureCount,
                        futureCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warning,
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TxFilter.entries.forEach { entry ->
                    FilterPill(
                        text = stringResource(entry.labelRes),
                        selected = filter == entry,
                        onClick = { filter = entry },
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        if (sorted.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.ReceiptLong,
                    title = stringResource(
                        if (query.isNotBlank()) {
                            R.string.transactions_no_results
                        } else {
                            R.string.transactions_empty_month
                        },
                    ),
                    message = stringResource(
                        if (query.isNotBlank()) {
                            R.string.transactions_no_results_hint
                        } else {
                            R.string.transactions_empty_hint
                        },
                    ),
                )
            }
        } else if (sort == MovementSort.DATE) {
            groupedForDate.forEach { (date, transactions) ->
                item(key = "day-$date") {
                    DayGroup(
                        date = date,
                        transactions = transactions,
                        data = data,
                        currency = currency,
                        selectedIds = selectedIds,
                        selectionMode = selectionMode,
                        onOpenTransaction = onOpenTransaction,
                        onToggleSelection = { id ->
                            selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
            item {
                BudCard(padding = 10.dp) {
                    sorted.forEachIndexed { index, tx ->
                        TransactionRow(
                            transaction = tx,
                            category = data.category(tx.categoryId),
                            account = data.account(tx.accountId),
                            currency = currency,
                            selected = tx.id in selectedIds,
                            selectionMode = selectionMode,
                            showDate = true,
                            onClick = {
                                if (selectionMode) {
                                    selectedIds = if (tx.id in selectedIds) selectedIds - tx.id else selectedIds + tx.id
                                } else {
                                    onOpenTransaction(tx)
                                }
                            },
                            onLongClick = {
                                selectedIds = if (tx.id in selectedIds) selectedIds - tx.id else selectedIds + tx.id
                            },
                        )
                        if (index != sorted.lastIndex) {
                            ThinDivider(Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showBulkDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showBulkDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            onChangeTransactionsDate(
                                selectedIds,
                                Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        selectedIds = emptySet()
                        showBulkDate = false
                    },
                ) { Text(stringResource(R.string.action_change_date), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDate = false }) {
                    Text(stringResource(R.string.action_close_calendar), color = colors.inkMuted)
                }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }

    if (confirmBulkDelete) {
        AlertDialog(
            onDismissRequest = { confirmBulkDelete = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    text = pluralStringResource(
                        R.plurals.transactions_delete_title,
                        selectedIds.size,
                        selectedIds.size,
                    ),
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.transactions_delete_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransactions(selectedIds)
                        selectedIds = emptySet()
                        confirmBulkDelete = false
                    },
                ) { Text(stringResource(R.string.action_delete), color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { confirmBulkDelete = false }) {
                    Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                }
            },
        )
    }
}

@Composable
private fun BulkSelectionBar(
    count: Int,
    onDate: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = BudTheme.colors
    BudCard(padding = 12.dp, color = colors.primarySoft, border = false) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = pluralStringResource(
                        R.plurals.transactions_selected,
                        count,
                        count,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(R.string.transactions_selection_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onDate)
                        .testTag("bulk-date")
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Event, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_date), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = colors.expense, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.labelMedium, color = colors.expense)
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                }
            }
        }
    }
}

@Composable
private fun DayGroup(
    date: LocalDate,
    transactions: List<Transaction>,
    data: AppData,
    currency: Currency,
    selectedIds: Set<String>,
    selectionMode: Boolean,
    onOpenTransaction: (Transaction) -> Unit,
    onToggleSelection: (String) -> Unit,
) {
    val colors = BudTheme.colors
    val net = transactions.sumOf { if (it.type == TxType.INCOME) it.amountCents else -it.amountCents }
    BudCard(padding = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatDayLabel(date),
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
            )
            if (date == LocalDate.now() || date == LocalDate.now().minusDays(1)) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatDayNumber(date),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.inkMuted,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(colors.line.copy(alpha = 0.7f)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = (if (net >= 0) "+" else "−") + formatMoney(kotlin.math.abs(net), currency),
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
                maxLines = 1,
            )
        }
        transactions.forEachIndexed { index, tx ->
            TransactionRow(
                transaction = tx,
                category = data.category(tx.categoryId),
                account = data.account(tx.accountId),
                currency = currency,
                selected = tx.id in selectedIds,
                selectionMode = selectionMode,
                onClick = {
                    if (selectionMode) onToggleSelection(tx.id) else onOpenTransaction(tx)
                },
                onLongClick = { onToggleSelection(tx.id) },
            )
            if (index != transactions.lastIndex) {
                ThinDivider(Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: Long,
    color: androidx.compose.ui.graphics.Color,
    currency: Currency,
    modifier: Modifier = Modifier,
    signed: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            color = BudTheme.colors.inkMuted,
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = if (signed) {
                (if (value >= 0) "+" else "−") + formatMoney(kotlin.math.abs(value), currency)
            } else {
                formatMoney(value, currency)
            },
            style = BudTheme.type.moneySmall.copy(fontSize = 12.5.sp),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SortControl(
    sort: MovementSort,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (MovementSort) -> Unit,
) {
    val colors = BudTheme.colors
    Box {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.line, CircleShape)
                .clickable { onExpandedChange(true) }
                .testTag("sort-control")
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.SwapVert,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = stringResource(sort.shortRes),
                style = MaterialTheme.typography.labelMedium,
                color = colors.ink,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            MovementSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (option == sort) colors.primary else colors.ink,
                        )
                    },
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = BudTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primary else colors.surface)
            .border(
                width = 1.dp,
                color = if (selected) colors.primary else colors.line,
                shape = CircleShape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.onPrimary else colors.inkMuted,
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = BudTheme.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            tint = colors.inkMuted,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focus)
                .testTag("search-field"),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
            cursorBrush = SolidColor(colors.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.transactions_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
                inner()
            },
        )
        if (query.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.action_clear),
                tint = colors.inkMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") },
            )
        }
    }
}
