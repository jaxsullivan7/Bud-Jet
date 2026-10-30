package com.budjet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.SavingsGoal
import com.budjet.app.ui.GoalDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.LinearProgressBar
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.components.iconChoices
import com.budjet.app.ui.components.parseHex
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.ui.theme.CategoryPalette
import com.budjet.app.util.capitalizeEs
import com.budjet.app.util.centsToInput
import com.budjet.app.util.formatMonth
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.parseMoneyInput
import com.budjet.app.util.percentText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val goalIcons = listOf(
    "savings", "flight", "bag", "home", "car", "gift", "health", "school", "trending",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalsScreen(
    data: AppData,
    onClose: () -> Unit,
    onSaveGoal: (GoalDraft) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onAdjustGoal: (id: String, deltaCents: Long) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    var editor by remember { mutableStateOf<GoalDraft?>(null) }
    var contributing by remember { mutableStateOf<SavingsGoal?>(null) }
    var deleting by remember { mutableStateOf<String?>(null) }

    val goals = data.savingsGoals
    val totalSaved = goals.sumOf { it.savedCents }
    val totalTarget = goals.sumOf { it.targetCents }
    val overallProgress = if (totalTarget > 0) (totalSaved.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
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
                Overline("AHORRO")
                SoftIconButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Nuevo objetivo",
                    onClick = { editor = GoalDraft() },
                    size = 38.dp,
                    container = colors.primarySoft,
                    tint = colors.primary,
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Objetivos.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Dinero apartado con un propósito. No mueve tu saldo, solo lo organiza.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(20.dp))
        }

        if (goals.isEmpty()) {
            item {
                BudCard {
                    EmptyState(
                        icon = Icons.Rounded.Savings,
                        title = "Tu primer objetivo",
                        message = "Un viaje, un colchón de emergencia o ese capricho que te rondaba.",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .clickable { editor = GoalDraft() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Crear objetivo", style = MaterialTheme.typography.titleSmall, color = colors.onPrimary)
                    }
                }
            }
        } else {
            item {
                BudCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column {
                            Overline("EN MARCHA")
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "${goals.size} ${if (goals.size == 1) "objetivo" else "objetivos"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                            )
                        }
                        Pill(
                            text = if (overallProgress >= 1f) "Completado" else percentText(overallProgress),
                            container = colors.primarySoft,
                            content = colors.primary,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${formatMoney(totalSaved, currency, decimals = false)} de ${formatMoney(totalTarget, currency, decimals = false)}",
                        style = BudTheme.type.moneyMedium,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressBar(progress = overallProgress, color = colors.primary, height = 6.dp)
                }
                Spacer(Modifier.height(14.dp))
            }

            items(goals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    currency = currency,
                    onClick = {
                        editor = GoalDraft(
                            id = goal.id,
                            name = goal.name,
                            targetCents = goal.targetCents,
                            deadline = goal.deadlineEpochDay?.let(LocalDate::ofEpochDay),
                            icon = goal.icon,
                            colorHex = goal.colorHex,
                        )
                    },
                    onContribute = { contributing = goal },
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                        .clickable { editor = GoalDraft() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Nuevo objetivo", style = MaterialTheme.typography.titleSmall, color = colors.ink)
                }
            }
        }
    }

    editor?.let { draft ->
        GoalEditorDialog(
            initial = draft,
            currency = currency,
            onDismiss = { editor = null },
            onSave = { updated ->
                onSaveGoal(updated)
                editor = null
            },
            onDelete = if (draft.id != null) {
                {
                    editor = null
                    deleting = draft.id
                }
            } else null,
        )
    }

    contributing?.let { goal ->
        GoalAmountDialog(
            goal = goal,
            currency = currency,
            onDismiss = { contributing = null },
            onConfirm = { delta ->
                onAdjustGoal(goal.id, delta)
                contributing = null
            },
        )
    }

    deleting?.let { id ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = colors.surface,
            shape = MaterialTheme.shapes.large,
            title = { Text("¿Eliminar objetivo?", color = colors.ink) },
            text = {
                Text(
                    "Se borrará el objetivo y su progreso. Los movimientos de tu cuenta no cambian.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGoal(id)
                        deleting = null
                    },
                ) { Text("Eliminar", color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancelar", color = colors.inkMuted) }
            },
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    currency: Currency,
    onClick: () -> Unit,
    onContribute: () -> Unit,
) {
    val colors = BudTheme.colors
    val color = categoryColor(goal.colorHex, colors.isDark)
    val progress = Finance.goalProgress(goal)
    val remaining = Finance.goalRemaining(goal)
    val complete = remaining == 0L
    val deadline = goal.deadlineEpochDay?.let(LocalDate::ofEpochDay)
    val suggestion = Finance.goalMonthlySuggestion(goal)

    BudCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (colors.isDark) 0.2f else 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon(goal.icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (complete) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.income, modifier = Modifier.size(15.dp))
                    }
                }
                Text(
                    text = buildString {
                        append(deadline?.let { "Para ${formatShortDate(it)}" } ?: "Sin fecha límite")
                        if (!complete && suggestion > 0) {
                            append(" · ≈ ")
                            append(formatMoney(suggestion, currency, decimals = false))
                            append("/mes")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatMoney(goal.savedCents, currency, decimals = false),
                style = BudTheme.type.moneyLarge.copy(fontSize = 26.sp),
                color = colors.ink,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "de ${formatMoney(goal.targetCents, currency, decimals = false)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = percentText(progress),
                style = MaterialTheme.typography.labelLarge,
                color = color,
            )
        }
        Spacer(Modifier.height(9.dp))
        LinearProgressBar(progress = progress, color = color, height = 6.dp)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (complete) "Objetivo conseguido. ¡Bien hecho!" else "Faltan ${formatMoney(remaining, currency, decimals = false)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.primarySoft)
                    .clickable(onClick = onContribute)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Aportar",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalEditorDialog(
    initial: GoalDraft,
    currency: Currency,
    onDismiss: () -> Unit,
    onSave: (GoalDraft) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val colors = BudTheme.colors
    var name by remember { mutableStateOf(initial.name) }
    var target by remember { mutableStateOf(if (initial.targetCents > 0) centsToInput(initial.targetCents) else "") }
    var deadline by remember { mutableStateOf(initial.deadline) }
    var icon by remember { mutableStateOf(initial.icon) }
    var color by remember { mutableStateOf(initial.colorHex) }
    var showDatePicker by remember { mutableStateOf(false) }
    val parsedTarget = parseMoneyInput(target)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(if (initial.id == null) "NUEVO OBJETIVO" else "EDITAR OBJETIVO")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (initial.id == null) "¿Qué quieres conseguir?" else "Ajusta tu objetivo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(28) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Viaje a Japón") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { text ->
                        if (text.length <= 10 && text.all { it.isDigit() || it == ',' }) target = text
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Meta") },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceMuted.copy(alpha = 0.5f))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = deadline?.let { "Para ${formatShortDate(it)}" } ?: "Sin fecha límite",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (deadline == null) colors.inkMuted else colors.ink,
                        modifier = Modifier.weight(1f),
                    )
                    if (deadline != null) {
                        Text(
                            text = "Quitar",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { deadline = null }
                                .padding(4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Icono", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    goalIcons.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (icon == key) colors.primarySoft else colors.surfaceMuted)
                                .border(1.dp, if (icon == key) colors.primary else Color.Transparent, CircleShape)
                                .clickable { icon = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = categoryIcon(key),
                                contentDescription = null,
                                tint = if (icon == key) colors.primary else colors.inkMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Color", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryPalette.swatches.take(10).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseHex(hex))
                                .border(
                                    width = if (color == hex) 2.dp else 0.dp,
                                    color = colors.ink,
                                    shape = CircleShape,
                                )
                                .clickable { color = hex },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        GoalDraft(
                            id = initial.id,
                            name = name,
                            targetCents = parsedTarget,
                            deadline = deadline,
                            icon = icon,
                            colorHex = color,
                        ),
                    )
                },
                enabled = name.isNotBlank() && parsedTarget > 0,
            ) {
                Text(if (initial.id == null) "Crear" else "Guardar", color = colors.primary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Eliminar", color = colors.expense) }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
            }
        },
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (deadline ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            deadline = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) { Text("Listo", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = colors.inkMuted) }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

@Composable
private fun GoalAmountDialog(
    goal: SavingsGoal,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val colors = BudTheme.colors
    var amount by remember { mutableStateOf("") }
    var withdraw by remember { mutableStateOf(false) }
    val parsed = parseMoneyInput(amount)
    val resulting = (goal.savedCents + if (withdraw) -parsed else parsed).coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(if (withdraw) "RETIRAR DEL OBJETIVO" else "APORTAR AL OBJETIVO")
                Spacer(Modifier.height(4.dp))
                Text(goal.name, style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            }
        },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceMuted)
                        .padding(3.dp),
                ) {
                    AmountMode("Aportar", !withdraw, Modifier.weight(1f)) { withdraw = false }
                    AmountMode("Retirar", withdraw, Modifier.weight(1f)) { withdraw = true }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Tienes ${formatMoney(goal.savedCents, currency)} de ${formatMoney(goal.targetCents, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { text ->
                        if (text.length <= 10 && text.all { it.isDigit() || it == ',' }) amount = text
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (withdraw) "Importe a retirar" else "Importe a aportar") },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2_000L, 5_000L, 10_000L, 20_000L).forEach { value ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, colors.line, CircleShape)
                                .clickable { amount = centsToInput(value) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = formatMoney(value, currency, decimals = false),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (parsed > 0) "Quedará en ${formatMoney(resulting, currency)}" else " ",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(if (withdraw) -parsed else parsed) },
                enabled = parsed > 0,
            ) {
                Text(if (withdraw) "Retirar" else "Aportar", color = if (withdraw) colors.expense else colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
        },
    )
}

@Composable
private fun AmountMode(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = BudTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) colors.surface else Color.Transparent)
            .border(1.dp, if (selected) colors.line else Color.Transparent, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.ink else colors.inkMuted,
        )
    }
}
