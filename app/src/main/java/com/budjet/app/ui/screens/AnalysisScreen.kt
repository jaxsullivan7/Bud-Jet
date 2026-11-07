package com.budjet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PieChartOutline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.CategorySpend
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.InvestmentMath
import com.budjet.app.data.NetWorth
import com.budjet.app.data.PortfolioSummary
import com.budjet.app.data.TxType
import com.budjet.app.data.filteredFrom
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CashflowChart
import com.budjet.app.ui.components.DonutChart
import com.budjet.app.ui.components.DonutSegment
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.LegendDot
import com.budjet.app.ui.components.MonthSelector
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.ScreenTitle
import com.budjet.app.ui.components.SectionHeader
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMonthLower
import com.budjet.app.util.formatMoney
import com.budjet.app.util.percentText
import com.budjet.app.util.formatShortDate
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.math.max

@Composable
fun AnalysisScreen(
    data: AppData,
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenCategory: (com.budjet.app.data.Category) -> Unit,
    onSetStartDate: (LocalDate?) -> Unit,
    onOpenPortfolio: () -> Unit,
    onOpenPatrimonio: () -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val othersLabel = stringResource(R.string.analysis_others)
    val today = remember { LocalDate.now() }
    val startDate = remember(data.settings.analysisStartEpochDay) {
        data.settings.analysisStartEpochDay?.let(LocalDate::ofEpochDay)
    }
    val scopedData = remember(data, startDate) {
        data.filteredFrom(startDate)
    }
    var showStartPicker by remember { mutableStateOf(false) }
    val monthTx = remember(scopedData.transactions, month) { Finance.transactionsOfMonth(scopedData, month) }
    val postedTx = remember(scopedData.transactions, month) {
        Finance.postedTransactionsOfMonth(scopedData, month)
    }
    val futureCount = monthTx.size - postedTx.size
    val totals = remember(postedTx) { Finance.totals(postedTx) }
    val monthTotalsFull = remember(data.transactions, month) {
        Finance.totals(Finance.postedTransactionsOfMonth(data, month, today))
    }
    val initialBalance = remember(data, month) { Finance.initialBalance(data, month) }
    val finalBalance = remember(data, month, today) { Finance.finalBalance(data, month, today) }
    val portfolio = remember(data.investments, data.investmentEntries) {
        InvestmentMath.portfolio(data.investments, data.investmentEntries, today)
    }
    val netWorth = remember(data, today) { Finance.netWorth(data, today) }
    val spends = remember(scopedData, month) { Finance.spendByCategory(scopedData, month) }
    val series = remember(scopedData, month) { Finance.series(scopedData, month, 6) }
    val average = remember(scopedData, month) { Finance.monthlyAverageExpense(scopedData, month, 3) }
    val deltaVsAverage = Finance.monthDelta(totals.expense, average)

    val shown = remember(spends, othersLabel) {
        if (spends.size <= 5) spends else {
            val head = spends.take(4)
            val rest = spends.drop(4)
            head + CategorySpend(
                category = rest.first().category.copy(
                    id = "others",
                    name = othersLabel,
                    colorHex = "#8C8C84",
                ),
                spent = rest.sumOf { it.spent },
                share = rest.fold(0f) { acc, item -> acc + item.share },
            )
        }
    }
    val donutSegments = shown.map {
        DonutSegment(it.share, categoryColor(it.category.colorHex, colors.isDark))
    }

    val daysElapsed = run {
        val startDay = if (startDate != null && YearMonth.from(startDate) == month) {
            startDate.dayOfMonth
        } else {
            1
        }
        if (month == YearMonth.from(today)) {
            (today.dayOfMonth - startDay + 1).coerceAtLeast(1)
        } else {
            (month.lengthOfMonth() - startDay + 1).coerceAtLeast(1)
        }
    }
    val dailyAverage = if (daysElapsed > 0) totals.expense / daysElapsed else 0L
    val biggestExpense = postedTx.filter { it.type == TxType.EXPENSE }.maxByOrNull { it.amountCents }
    val spentDays = postedTx.filter { it.type == TxType.EXPENSE }
        .map { LocalDate.ofEpochDay(it.dateEpochDay) }
        .toSet()
        .size
    val noSpendDays = (daysElapsed - spentDays).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analysis-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            ScreenTitle(
                overline = stringResource(R.string.analysis_overline),
                title = stringResource(R.string.analysis_title),
            )
            Spacer(Modifier.height(18.dp))
            MonthSelector(
                month = month,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                canGoNext = month.isBefore(YearMonth.now()),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primarySoft.copy(alpha = 0.55f))
                    .clickable { showStartPicker = true }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = startDate?.let {
                        stringResource(R.string.analysis_history_since, formatShortDate(it))
                    } ?: stringResource(R.string.analysis_all_history),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(13.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            MonthBalanceCard(
                initial = initialBalance,
                income = monthTotalsFull.income,
                expense = monthTotalsFull.expense,
                final = finalBalance,
                month = month,
                today = today,
                currency = currency,
                filterActive = startDate != null,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            PatrimonioSummaryCard(
                netWorth = netWorth,
                currency = currency,
                onClick = onOpenPatrimonio,
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            PortfolioSummaryCard(
                summary = portfolio,
                currency = currency,
                onClick = onOpenPortfolio,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            BudCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Overline(stringResource(R.string.analysis_where_it_goes))
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = stringResource(
                                R.string.analysis_expenses_in_month,
                                formatMonthLower(month),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                    }
                    if (totals.income > 0) {
                        Pill(
                            text = stringResource(
                                R.string.analysis_of_income,
                                percentText(totals.expense.toFloat() / totals.income),
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (spends.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.PieChartOutline,
                        title = stringResource(R.string.analysis_no_expenses_title),
                        message = stringResource(R.string.analysis_no_expenses_body),
                    )
                    if (futureCount > 0) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.analysis_scheduled_note,
                                futureCount,
                                futureCount,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warning,
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(124.dp)
                                .pointerInput(shown) {
                                    detectTapGestures { offset ->
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val dx = offset.x - cx
                                        val dy = offset.y - cy
                                        val distance = kotlin.math.hypot(dx, dy)
                                        val outer = size.width / 2f
                                        val inner = outer - 22.dp.toPx()
                                        if (distance in inner..outer) {
                                            var angle = Math.toDegrees(
                                                kotlin.math.atan2(dy.toDouble(), dx.toDouble()),
                                            ).toFloat() + 90f
                                            if (angle < 0f) angle += 360f
                                            var accumulated = 0f
                                            for (segment in shown) {
                                                val sweep = segment.share * 360f
                                                if (angle <= accumulated + sweep) {
                                                    data.category(segment.category.id)?.let(onOpenCategory)
                                                    break
                                                }
                                                accumulated += sweep
                                            }
                                        }
                                    }
                                },
                        ) {
                            DonutChart(
                                segments = donutSegments,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 16.dp,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatMoney(totals.expense, currency),
                                        style = BudTheme.type.moneyMedium.copy(fontSize = 16.sp),
                                        color = colors.ink,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = stringResource(R.string.analysis_expenses_total),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkMuted,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            shown.forEach { item ->
                                val target = data.category(item.category.id)
                                LegendRow(
                                    item = item,
                                    currency = currency,
                                    onClick = target?.let { { onOpenCategory(it) } },
                                )
                            }
                        }
                    }
                    if (futureCount > 0) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.analysis_scheduled_note,
                                futureCount,
                                futureCount,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warning,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.analysis_month_in_data))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = stringResource(R.string.analysis_average_spend),
                    value = formatMoney(dailyAverage, currency),
                    hint = stringResource(R.string.analysis_per_day),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.analysis_no_spend_days),
                    value = noSpendDays.toString(),
                    hint = stringResource(R.string.analysis_of_days, daysElapsed),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = stringResource(R.string.analysis_biggest_expense),
                    value = biggestExpense?.let { formatMoney(it.amountCents, currency) } ?: "—",
                    hint = biggestExpense?.note?.ifBlank {
                        stringResource(R.string.analysis_no_data)
                    } ?: stringResource(R.string.analysis_no_data),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.analysis_savings),
                    value = if (totals.income > 0) percentText(totals.savingsRate) else "—",
                    hint = if (totals.income > 0) {
                        formatMoney(max(0L, totals.balance), currency)
                    } else {
                        stringResource(R.string.analysis_no_income)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = stringResource(R.string.analysis_comparison),
                trailing = stringResource(R.string.analysis_six_months),
            )
            Spacer(Modifier.height(12.dp))
            BudCard {
                CashflowChart(points = series, highlightMonth = month)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(colors.income, stringResource(R.string.legend_income))
                    Spacer(Modifier.width(16.dp))
                    LegendDot(colors.expense, stringResource(R.string.legend_expenses))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (average > 0) {
                        stringResource(
                            R.string.analysis_average_note,
                            formatMoney(average, currency),
                        )
                    } else {
                        stringResource(R.string.analysis_need_history)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            BudCard(color = colors.primarySoft, border = false) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Overline(
                            stringResource(R.string.analysis_insight),
                            color = colors.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when {
                                deltaVsAverage == null -> stringResource(R.string.analysis_insight_learning)
                                deltaVsAverage < -0.02f -> stringResource(
                                    R.string.analysis_insight_less,
                                    percentText(abs(deltaVsAverage)),
                                )
                                deltaVsAverage > 0.02f -> stringResource(
                                    R.string.analysis_insight_more,
                                    percentText(abs(deltaVsAverage)),
                                )
                                else -> stringResource(R.string.analysis_insight_in_line)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.ink,
                        )
                        if (biggestExpense != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(
                                    R.string.analysis_biggest_expense_note,
                                    formatShortDate(LocalDate.ofEpochDay(biggestExpense.dateEpochDay)),
                                    biggestExpense.note.ifBlank {
                                        data.category(biggestExpense.categoryId)?.name
                                            ?: stringResource(R.string.analysis_an_expense)
                                    },
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (startDate ?: today)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val chosen = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onSetStartDate(if (chosen.isAfter(today)) today else chosen)
                        }
                        showStartPicker = false
                    },
                ) { Text(stringResource(R.string.action_apply), color = colors.primary) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onSetStartDate(null)
                            showStartPicker = false
                        },
                    ) { Text(stringResource(R.string.filter_all), color = colors.inkMuted) }
                    TextButton(onClick = { showStartPicker = false }) {
                        Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                    }
                }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

@Composable
private fun PatrimonioSummaryCard(
    netWorth: NetWorth,
    currency: Currency,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    BudCard(
        modifier = Modifier.testTag("patrimonio-card"),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Overline(stringResource(R.string.patrimonio_net_worth))
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = formatMoney(netWorth.totalCents, currency),
            style = BudTheme.type.moneyLarge,
            color = colors.ink,
            maxLines = 1,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column {
                Text(
                    stringResource(R.string.label_cash),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Text(formatMoney(netWorth.cashCents, currency), style = BudTheme.type.moneySmall, color = colors.ink)
            }
            Column {
                Text(
                    stringResource(R.string.label_investments),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Text(formatMoney(netWorth.investmentsCents, currency), style = BudTheme.type.moneySmall, color = colors.ink)
            }
            Column {
                Text(
                    stringResource(R.string.label_debts),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Text(formatMoney(netWorth.liabilitiesCents, currency), style = BudTheme.type.moneySmall, color = colors.expense)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.analysis_net_worth_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun PortfolioSummaryCard(
    summary: PortfolioSummary,
    currency: Currency,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    val percent = summary.returnPercent
    BudCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Overline(stringResource(R.string.analysis_portfolio_overline))
            if (percent != null) {
                val positive = percent >= 0f
                Pill(
                    text = (if (positive) "+" else "−") + percentText(abs(percent), decimals = 1),
                    container = if (positive) colors.primarySoft else colors.expense.copy(alpha = 0.12f),
                    content = if (positive) colors.primary else colors.expense,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (summary.products.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.analysis_add_first_product),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.analysis_portfolio_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        } else {
            Text(
                text = formatMoney(summary.valueCents, currency),
                style = BudTheme.type.moneyLarge,
                color = colors.ink,
                maxLines = 1,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text(
                        stringResource(R.string.analysis_contributed),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Text(
                        text = formatMoney(summary.contributedCents, currency),
                        style = BudTheme.type.moneySmall,
                        color = colors.ink,
                    )
                }
                Column {
                    Text(
                        stringResource(R.string.analysis_return),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Text(
                        text = (if (summary.returnsCents >= 0) "+" else "−") +
                            formatMoney(abs(summary.returnsCents), currency),
                        style = BudTheme.type.moneySmall,
                        color = if (summary.returnsCents >= 0) colors.income else colors.expense,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.analysis_portfolio_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun MonthBalanceCard(
    initial: Long,
    income: Long,
    expense: Long,
    final: Long,
    month: YearMonth,
    today: LocalDate,
    currency: Currency,
    filterActive: Boolean,
) {
    val colors = BudTheme.colors
    val current = month == YearMonth.from(today)
    BudCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Overline(stringResource(R.string.analysis_month_balance))
            Pill(
                text = stringResource(
                    if (current) R.string.analysis_in_progress else R.string.analysis_closed,
                ),
                container = colors.surfaceMuted,
                content = colors.inkMuted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.analysis_opening_balance),
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
        )
        Text(
            text = formatMoney(initial, currency),
            style = BudTheme.type.moneyMedium.copy(fontSize = 24.sp),
            color = colors.ink,
            maxLines = 1,
        )
        Spacer(Modifier.height(12.dp))
        BalanceFlowRow(
            label = stringResource(R.string.analysis_income_month),
            prefix = "+",
            value = income,
            color = colors.income,
            currency = currency,
        )
        Spacer(Modifier.height(5.dp))
        BalanceFlowRow(
            label = stringResource(R.string.analysis_expenses_month),
            prefix = "−",
            value = expense,
            color = colors.expense,
            currency = currency,
        )
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.HorizontalDivider(color = colors.line)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.analysis_closing_balance),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(
                        if (current) R.string.analysis_up_to_today else R.string.analysis_end_of_month,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Text(
                text = formatMoney(final, currency),
                style = BudTheme.type.moneyLarge,
                color = colors.ink,
                maxLines = 1,
            )
        }
        if (filterActive) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.analysis_balance_filter_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun BalanceFlowRow(
    label: String,
    prefix: String,
    value: Long,
    color: Color,
    currency: Currency,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = BudTheme.colors.inkMuted,
        )
        Text(
            text = prefix + formatMoney(value, currency),
            style = BudTheme.type.moneySmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun LegendRow(
    item: CategorySpend,
    currency: Currency,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = BudTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(categoryColor(item.category.colorHex, colors.isDark), CircleShape),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = item.category.name,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onClick != null) {
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = formatMoney(item.spent, currency),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
            color = colors.ink,
            maxLines = 1,
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = percentText(item.share),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
            color = colors.inkMuted,
            modifier = Modifier.width(25.dp),
        )
        if (onClick != null) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.55f),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    BudCard(modifier = modifier, padding = 14.dp) {
        Overline(label)
        Spacer(Modifier.height(7.dp))
        Text(
            text = value,
            style = BudTheme.type.moneyMedium.copy(fontSize = 17.sp),
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
