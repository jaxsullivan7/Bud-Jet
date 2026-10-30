package com.budjet.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.LinearProgressBar
import com.budjet.app.ui.components.MiniBarChart
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.SectionHeader
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.ThinDivider
import com.budjet.app.ui.components.TransactionRow
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.capitalizeEs
import com.budjet.app.util.formatDayNumber
import com.budjet.app.util.formatMonth
import com.budjet.app.util.formatMonthShort
import com.budjet.app.util.formatMoney
import com.budjet.app.util.percentText
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun CategoryDetailScreen(
    data: AppData,
    category: Category,
    month: YearMonth,
    onClose: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val color = categoryColor(category.colorHex, colors.isDark)
    val monthTransactions = remember(data.transactions, category.id, month) {
        Finance.categoryTransactions(data, month, category.id)
    }
    val total = monthTransactions.sumOf { it.amountCents }
    val monthTotals = remember(data.transactions, month) {
        Finance.totals(Finance.transactionsOfMonth(data, month))
    }
    val referenceTotal = if (category.type == TxType.INCOME) monthTotals.income else monthTotals.expense
    val share = if (referenceTotal > 0) total.toFloat() / referenceTotal else 0f
    val previous = remember(data.transactions, category.id, month) {
        Finance.spentForCategory(data, month.minusMonths(1), category.id)
    }
    val delta = Finance.monthDelta(total, previous)
    val average = if (monthTransactions.isEmpty()) 0L else total / monthTransactions.size
    val biggest = monthTransactions.maxByOrNull { it.amountCents }
    val series = remember(data.transactions, category.id, month) {
        Finance.categorySeries(data, month, category.id, 6).map {
            formatMonthShort(it.first) to it.second
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SoftIconButton(
                    icon = Icons.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline("DETALLE DE CATEGORÍA")
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(category = category, size = 54.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.ink,
                    )
                    Text(
                        text = formatMonth(month).capitalizeEs(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatMoney(total, currency),
                    style = BudTheme.type.moneyHero.copy(fontSize = 38.sp, lineHeight = 44.sp),
                    color = colors.ink,
                    maxLines = 1,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.padding(bottom = 3.dp)) {
                    Text(
                        text = if (category.type == TxType.INCOME) "ingresos" else "gastos",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    if (referenceTotal > 0) {
                        Text(
                            text = "${percentText(share)} del total",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                }
            }
            if (delta != null && previous > 0 && abs(delta) >= 0.005f) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(
                        text = "${percentText(abs(delta))} ${if (delta > 0) "más" else "menos"}",
                        icon = if (delta > 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        container = if (delta > 0) colors.expense.copy(alpha = 0.1f) else colors.primarySoft,
                        content = if (delta > 0) colors.expense else colors.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "que el mes pasado",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            } else if (previous > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "En línea con el mes pasado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            BudCard(padding = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailStat(
                        label = "MOVIMIENTOS",
                        value = monthTransactions.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    DetailStat(
                        label = "MEDIA",
                        value = formatMoney(average, currency, decimals = false),
                        modifier = Modifier.weight(1f),
                    )
                    DetailStat(
                        label = "MAYOR",
                        value = biggest?.let { formatMoney(it.amountCents, currency, decimals = false) } ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Evolución", trailing = "6 meses")
            Spacer(Modifier.height(12.dp))
            BudCard {
                MiniBarChart(values = series, color = color)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Gasto medio del periodo",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    val avg = series.sumOf { it.second } / series.size.coerceAtLeast(1)
                    Text(
                        text = formatMoney(avg, currency, decimals = false),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.ink,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Movimientos",
                trailing = "${monthTransactions.size}",
            )
            Spacer(Modifier.height(8.dp))
        }

        if (monthTransactions.isEmpty()) {
            item {
                BudCard {
                    EmptyState(
                        icon = Icons.Rounded.ReceiptLong,
                        title = "Sin movimientos",
                        message = "No hay gastos de ${category.name.lowercase()} en este mes.",
                    )
                }
            }
        } else {
            item {
                BudCard(padding = 10.dp) {
                    monthTransactions.forEachIndexed { index, transaction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatDayNumber(java.time.LocalDate.ofEpochDay(transaction.dateEpochDay)),
                                modifier = Modifier.width(44.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = colors.inkMuted,
                            )
                            TransactionRow(
                                transaction = transaction,
                                category = category,
                                account = data.account(transaction.accountId),
                                currency = currency,
                                onClick = { onOpenTransaction(transaction) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (index != monthTransactions.lastIndex) {
                            ThinDivider(Modifier.padding(start = 50.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Este color representa ${category.name.lowercase()} en tus análisis.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun DetailStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
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
            text = value,
            style = BudTheme.type.moneySmall.copy(fontSize = 13.sp),
            color = BudTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
