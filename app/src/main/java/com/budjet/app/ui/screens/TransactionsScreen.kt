package com.budjet.app.ui.screens

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
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.time.LocalDate
import java.time.YearMonth

private enum class TxFilter(val label: String) {
    ALL("Todo"),
    EXPENSE("Gastos"),
    INCOME("Ingresos"),
}

@Composable
fun TransactionsScreen(
    data: AppData,
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    var filter by rememberSaveable { mutableStateOf(TxFilter.ALL) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val today = remember { LocalDate.now() }

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
    val grouped = remember(filtered) {
        filtered.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
            .toSortedMap(compareByDescending { it })
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
                overline = "HISTORIAL",
                title = "Movimientos",
                trailing = {
                    SoftIconButton(
                        icon = if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = if (showSearch) "Cerrar búsqueda" else "Buscar",
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) query = ""
                        },
                        size = 38.dp,
                        container = if (showSearch) colors.primarySoft else colors.surface,
                    )
                },
            )
        }

        item {
            Spacer(Modifier.height(18.dp))
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
                    text = "${allMonthTx.size} mov.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
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
                        label = "INGRESOS",
                        value = totals.income,
                        color = colors.income,
                        currency = currency,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCell(
                        label = "GASTOS",
                        value = totals.expense,
                        color = colors.expense,
                        currency = currency,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCell(
                        label = "BALANCE",
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
                    text = "Cifras de los resultados de búsqueda · ${summarySource.size} ${if (summarySource.size == 1) "movimiento" else "movimientos"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                )
            }
        }

        if (futureCount > 0) {
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "$futureCount ${if (futureCount == 1) "movimiento programado" else "movimientos programados"} · no afectan al saldo hasta su fecha.",
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
                        text = entry.label,
                        selected = filter == entry,
                        onClick = { filter = entry },
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        if (grouped.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.ReceiptLong,
                    title = if (query.isNotBlank()) "Sin resultados" else "Un mes sin movimientos",
                    message = if (query.isNotBlank()) {
                        "Prueba con otra palabra o cambia el filtro."
                    } else {
                        "Apunta tu primer gasto o ingreso con el botón +."
                    },
                )
            }
        } else {
            grouped.forEach { (date, transactions) ->
                item(key = "day-$date") {
                    DayGroup(
                        date = date,
                        transactions = transactions,
                        data = data,
                        currency = currency,
                        onOpenTransaction = onOpenTransaction,
                    )
                    Spacer(Modifier.height(12.dp))
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
    onOpenTransaction: (Transaction) -> Unit,
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
                onClick = { onOpenTransaction(tx) },
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
                        text = "Buscar por nota o categoría",
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
                contentDescription = "Borrar",
                tint = colors.inkMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") },
            )
        }
    }
}
