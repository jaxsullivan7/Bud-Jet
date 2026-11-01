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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.AppData
import com.budjet.app.data.CategorySpend
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
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
import com.budjet.app.util.formatMonth
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
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
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
    val spends = remember(scopedData, month) { Finance.spendByCategory(scopedData, month) }
    val series = remember(scopedData, month) { Finance.series(scopedData, month, 6) }
    val average = remember(scopedData, month) { Finance.monthlyAverageExpense(scopedData, month, 3) }
    val deltaVsAverage = Finance.monthDelta(totals.expense, average)

    val shown = remember(spends) {
        if (spends.size <= 5) spends else {
            val head = spends.take(4)
            val rest = spends.drop(4)
            head + CategorySpend(
                category = rest.first().category.copy(
                    id = "others",
                    name = "Otros",
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            ScreenTitle(overline = "ANÁLISIS", title = "Tu dinero")
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
                    text = startDate?.let { "Histórico desde ${formatShortDate(it)}" } ?: "Todo el historial",
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
            BudCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Overline("EN QUÉ SE VA")
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Gastos de ${formatMonth(month).lowercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                    }
                    if (totals.income > 0) {
                        Pill(text = "${percentText(totals.expense.toFloat() / totals.income)} de ingresos")
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (spends.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.PieChartOutline,
                        title = "Sin gastos este mes",
                        message = "Cuando apuntes gastos, aquí verás el desglose por categorías.",
                    )
                    if (futureCount > 0) {
                        Text(
                            text = "$futureCount ${if (futureCount == 1) "movimiento programado" else "movimientos programados"} sin incluir todavía.",
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
                                        text = "gastos",
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
                            text = "$futureCount ${if (futureCount == 1) "movimiento programado" else "movimientos programados"} sin incluir todavía.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warning,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Tu mes en datos")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "GASTO MEDIO",
                    value = formatMoney(dailyAverage, currency),
                    hint = "al día",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "DÍAS SIN GASTAR",
                    value = noSpendDays.toString(),
                    hint = "de $daysElapsed días",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    label = "MAYOR GASTO",
                    value = biggestExpense?.let { formatMoney(it.amountCents, currency) } ?: "—",
                    hint = biggestExpense?.note?.ifBlank { "Sin datos" } ?: "Sin datos",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "AHORRO",
                    value = if (totals.income > 0) percentText(totals.savingsRate) else "—",
                    hint = if (totals.income > 0) formatMoney(max(0L, totals.balance), currency) else "sin ingresos",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Comparativa", trailing = "6 meses")
            Spacer(Modifier.height(12.dp))
            BudCard {
                CashflowChart(points = series, highlightMonth = month)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(colors.income, "Ingresos")
                    Spacer(Modifier.width(16.dp))
                    LegendDot(colors.expense, "Gastos")
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (average > 0) {
                        "Tu media de gasto de los últimos 3 meses es ${formatMoney(average, currency)}."
                    } else {
                        "Necesitas más historial para comparar meses."
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
                        Overline("UNA OBSERVACIÓN", color = colors.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when {
                                deltaVsAverage == null -> "Aún estamos conociendo tus hábitos. Sigue apuntando movimientos."
                                deltaVsAverage < -0.02f -> "Este mes gastas un ${percentText(abs(deltaVsAverage))} menos que tu media de los últimos 3 meses. Buen ritmo."
                                deltaVsAverage > 0.02f -> "Este mes gastas un ${percentText(abs(deltaVsAverage))} más que tu media de los últimos 3 meses."
                                else -> "Tu gasto va en línea con tu media de los últimos 3 meses."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.ink,
                        )
                        if (biggestExpense != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Tu mayor gasto fue el ${formatShortDate(LocalDate.ofEpochDay(biggestExpense.dateEpochDay))}: " +
                                    "${biggestExpense.note.ifBlank { data.category(biggestExpense.categoryId)?.name ?: "un gasto" }}.",
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
                ) { Text("Aplicar", color = colors.primary) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onSetStartDate(null)
                            showStartPicker = false
                        },
                    ) { Text("Todo", color = colors.inkMuted) }
                    TextButton(onClick = { showStartPicker = false }) {
                        Text("Cancelar", color = colors.inkMuted)
                    }
                }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
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
            Overline("BALANCE DEL MES")
            Pill(
                text = if (current) "En curso" else "Cerrado",
                container = colors.surfaceMuted,
                content = colors.inkMuted,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Saldo inicial",
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
            label = "Ingresos del mes",
            prefix = "+",
            value = income,
            color = colors.income,
            currency = currency,
        )
        Spacer(Modifier.height(5.dp))
        BalanceFlowRow(
            label = "Gastos del mes",
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
                    text = "Saldo final",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Text(
                    text = if (current) "Hasta hoy" else "Cierre del mes",
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
                text = "El saldo se calcula con todo el historial, no con el filtro de fechas.",
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
