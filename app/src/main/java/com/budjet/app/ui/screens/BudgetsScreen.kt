package com.budjet.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PieChartOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.LinearProgressBar
import com.budjet.app.ui.components.MonthSelector
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.RingProgress
import com.budjet.app.ui.components.ScreenTitle
import com.budjet.app.ui.components.SectionHeader
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMoney
import com.budjet.app.util.percentText
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun BudgetsScreen(
    data: AppData,
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetBudget: (categoryId: String?, limitCents: Long) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val today = remember { LocalDate.now() }
    val monthTx = remember(data.transactions, month) { Finance.transactionsOfMonth(data, month) }
    val totals = remember(monthTx) { Finance.totals(monthTx) }
    val overall = Finance.overallBudget(data)
    val overallProgress = overall?.let { Finance.budgetProgress(totals.expense, it.limitCents) } ?: 0f
    val remaining = overall?.let { it.limitCents - totals.expense } ?: 0L
    val dailyAllowance = overall?.let { Finance.dailyAllowance(it.limitCents - totals.expense, today, month) } ?: 0L

    var editingCategory by remember { mutableStateOf<String?>(null) }
    var editingLimit by remember { mutableLongStateOf(0L) }
    var dialogOpen by remember { mutableStateOf(false) }
    var dialogLocked by remember { mutableStateOf(false) }

    val budgeted = data.budgets
        .filter { it.categoryId != null }
        .mapNotNull { budget ->
            val category = data.category(budget.categoryId) ?: return@mapNotNull null
            Triple(category, budget.limitCents, Finance.spentForCategory(data, month, category.id))
        }
        .sortedByDescending { it.third.toFloat() / it.second.coerceAtLeast(1) }
    val unbudgeted = data.categories.filter { it.type == TxType.EXPENSE && data.budgets.none { b -> b.categoryId == it.id } }

    fun open(categoryId: String?, limit: Long, locked: Boolean) {
        editingCategory = categoryId
        editingLimit = limit
        dialogLocked = locked
        dialogOpen = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            ScreenTitle(
                overline = "PLANIFICACIÓN",
                title = "Presupuestos",
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.primarySoft)
                            .clickable { open(null, overall?.limitCents ?: 0L, true) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Editar presupuesto general",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
            )
            Spacer(Modifier.height(18.dp))
            MonthSelector(
                month = month,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                canGoNext = month.isBefore(YearMonth.now()),
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            BudCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RingProgress(
                        progress = overallProgress,
                        color = when {
                            overallProgress >= 1f -> colors.expense
                            overallProgress >= 0.8f -> colors.warning
                            else -> colors.primary
                        },
                        modifier = Modifier.size(96.dp),
                        strokeWidth = 7.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = percentText(overallProgress.coerceAtMost(1f)),
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.ink,
                            )
                            Text(
                                text = "usado",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = colors.inkMuted,
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Overline("PRESUPUESTO GENERAL")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (overall == null) "Sin definir" else formatMoney(overall.limitCents, currency, decimals = false),
                            style = BudTheme.type.moneyLarge,
                            color = colors.ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (overall == null) {
                                "Pon un límite mensual para controlar el ritmo."
                            } else if (remaining >= 0) {
                                "${formatMoney(remaining, currency, decimals = false)} disponibles"
                            } else {
                                "${formatMoney(abs(remaining), currency, decimals = false)} por encima"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (remaining < 0) colors.expense else colors.inkMuted,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressBar(
                    progress = overallProgress,
                    color = when {
                        overallProgress >= 1f -> colors.expense
                        overallProgress >= 0.8f -> colors.warning
                        else -> colors.primary
                    },
                    height = 6.dp,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${formatMoney(totals.expense, currency, decimals = false)} gastados",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Text(
                        text = if (remaining >= 0) {
                            "≈ ${formatMoney(dailyAllowance, currency, decimals = false)}/día"
                        } else {
                            "Presupuesto superado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining < 0) colors.expense else colors.inkMuted,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = "Por categoría",
                trailing = if (unbudgeted.isNotEmpty()) "Añadir" else null,
                onTrailingClick = {
                    unbudgeted.firstOrNull()?.let { open(it.id, 0L, false) }
                },
            )
            Spacer(Modifier.height(10.dp))
        }

        if (budgeted.isEmpty()) {
            item {
                BudCard {
                    EmptyState(
                        icon = Icons.Rounded.PieChartOutline,
                        title = "Sin presupuestos por categoría",
                        message = "Asigna un límite a las categorías que más te importan.",
                    )
                }
            }
        } else {
            items(budgeted, key = { it.first.id }) { (category, limit, spent) ->
                BudgetCategoryCard(
                    category = category,
                    limit = limit,
                    spent = spent,
                    currency = currency,
                    onClick = { open(category.id, limit, true) },
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                    .clickable { open(unbudgeted.firstOrNull()?.id, 0L, false) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Añadir presupuesto",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Los presupuestos se reinician cada mes. Tus movimientos no se tocan.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }

    if (dialogOpen) {
        BudgetEditorDialog(
            categories = if (dialogLocked) {
                listOfNotNull(data.category(editingCategory))
            } else {
                data.categories.filter { it.type == TxType.EXPENSE }
            },
            initialCategoryId = editingCategory,
            initialLimitCents = editingLimit,
            locked = dialogLocked,
            currency = currency,
            existingBudgets = data.budgets.associate { it.categoryId to it.limitCents },
            onDismiss = { dialogOpen = false },
            onSave = { categoryId, limit ->
                onSetBudget(categoryId, limit)
                dialogOpen = false
            },
        )
    }
}

@Composable
private fun BudgetCategoryCard(
    category: Category,
    limit: Long,
    spent: Long,
    currency: Currency,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    val progress = Finance.budgetProgress(spent, limit)
    val over = spent > limit
    val warn = progress >= 0.8f && !over
    val color = when {
        over -> colors.expense
        warn -> colors.warning
        else -> colors.primary
    }
    val remaining = limit - spent

    BudCard(onClick = onClick, padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge(category = category, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                )
                Text(
                    text = "${formatMoney(spent, currency, decimals = false)} de ${formatMoney(limit, currency, decimals = false)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (over) {
                        "+${formatMoney(abs(remaining), currency, decimals = false)}"
                    } else {
                        "${formatMoney(remaining, currency, decimals = false)}"
                    },
                    style = BudTheme.type.moneySmall,
                    color = if (over) colors.expense else colors.ink,
                )
                Text(
                    text = if (over) "de más" else "quedan",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.inkMuted,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressBar(progress = progress, color = color, height = 5.dp)
    }
}

@Composable
private fun BudgetEditorDialog(
    categories: List<Category>,
    initialCategoryId: String?,
    initialLimitCents: Long,
    locked: Boolean,
    currency: Currency,
    existingBudgets: Map<String?, Long>,
    onDismiss: () -> Unit,
    onSave: (String?, Long) -> Unit,
) {
    val colors = BudTheme.colors
    var selected by remember { mutableStateOf(initialCategoryId) }
    var amount by remember {
        mutableStateOf(
            if (initialLimitCents == 0L) "" else {
                val unit = initialLimitCents / 100
                val cents = (initialLimitCents % 100).toString().padStart(2, '0')
                "$unit,$cents"
            }
        )
    }
    val preview = parseAmount(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(if (initialCategoryId == null) "PRESUPUESTO GENERAL" else "PRESUPUESTO DE CATEGORÍA")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (initialCategoryId == null) "¿Cuánto quieres gastar?" else "Ajusta el límite",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            }
        },
        text = {
            Column {
                if (!locked) {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            CategoryChoice(
                                label = "General",
                                category = null,
                                selected = selected == null,
                                onClick = { selected = null },
                            )
                        }
                        items(categories) { category ->
                            CategoryChoice(
                                label = category.name,
                                category = category,
                                selected = selected == category.id,
                                onClick = { selected = category.id },
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
                val current = if (selected == null && initialLimitCents > 0) initialLimitCents else existingBudgets[selected] ?: 0L
                Text(
                    text = if (current > 0) {
                        "Límite actual: ${formatMoney(current, currency, decimals = false)}"
                    } else {
                        "Sin límite definido"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { text ->
                        if (text.length <= 10 && text.all { it.isDigit() || it == ',' }) {
                            amount = text
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Límite mensual") },
                    placeholder = { Text("0,00") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = colors.ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(selected, preview) },
                enabled = preview > 0,
            ) {
                Text("Guardar", color = if (preview > 0) colors.primary else colors.inkMuted)
            }
        },
        dismissButton = {
            Row {
                if (initialLimitCents > 0 && locked) {
                    TextButton(onClick = { onSave(selected, 0L) }) {
                        Text("Eliminar", color = colors.expense)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = colors.inkMuted)
                }
            }
        },
    )
}

@Composable
private fun CategoryChoice(
    label: String,
    category: Category?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primarySoft else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.line, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (category != null) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(com.budjet.app.ui.components.categoryColor(category.colorHex, colors.isDark), CircleShape),
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.inkMuted,
            maxLines = 1,
        )
    }
}

private fun parseAmount(text: String): Long {
    if (text.isBlank()) return 0L
    val normalized = text.replace('.', ',').replace(',', '.')
    return normalized.toDoubleOrNull()?.times(100)?.toLong() ?: 0L
}
