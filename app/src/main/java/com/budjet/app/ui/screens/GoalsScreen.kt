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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.budjet.app.R
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
import com.budjet.app.ui.components.displayName
import com.budjet.app.ui.components.iconChoices
import com.budjet.app.ui.components.parseHex
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.ui.theme.CategoryPalette
import com.budjet.app.util.capitalizeEs
import com.budjet.app.util.centsToInput
import com.budjet.app.util.filterAmountInput
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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.goals_overline))
                SoftIconButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.action_new_goal),
                    onClick = { editor = GoalDraft() },
                    size = 38.dp,
                    container = colors.primarySoft,
                    tint = colors.primary,
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.goals_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.goals_subtitle),
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
                        title = stringResource(R.string.goals_empty_title),
                        message = stringResource(R.string.goals_empty_body),
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
                        Text(stringResource(R.string.goals_create), style = MaterialTheme.typography.titleSmall, color = colors.onPrimary)
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
                            Overline(stringResource(R.string.overline_in_progress))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = pluralStringResource(R.plurals.goals_count, goals.size, goals.size),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                            )
                        }
                        Pill(
                            text = if (overallProgress >= 1f) {
                            stringResource(R.string.goals_completed)
                        } else {
                            percentText(overallProgress)
                        },
                            container = colors.primarySoft,
                            content = colors.primary,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(
                        R.string.goals_progress,
                        formatMoney(totalSaved, currency),
                        formatMoney(totalTarget, currency),
                    ),
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
                    Text(stringResource(R.string.goals_new), style = MaterialTheme.typography.titleSmall, color = colors.ink)
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
            title = { Text(stringResource(R.string.goals_delete_title), color = colors.ink) },
            text = {
                Text(
                    stringResource(R.string.goals_delete_body),
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
                ) { Text(stringResource(R.string.action_delete), color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
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
                        text = goal.displayName(),
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
                        append(
                            deadline?.let {
                                stringResource(R.string.goals_deadline, formatShortDate(it))
                            } ?: stringResource(R.string.goals_no_deadline),
                        )
                        if (!complete && suggestion > 0) {
                            append(" · ≈ ")
                            append(formatMoney(suggestion, currency))
                            append(stringResource(R.string.goals_per_month))
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
                text = formatMoney(goal.savedCents, currency),
                style = BudTheme.type.moneyLarge.copy(fontSize = 26.sp),
                color = colors.ink,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(
                    R.string.home_goal_of,
                    formatMoney(goal.targetCents, currency),
                ),
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
                text = if (complete) {
                    stringResource(R.string.goals_done)
                } else {
                    stringResource(R.string.goals_remaining, formatMoney(remaining, currency))
                },
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
                    text = stringResource(R.string.action_contribute),
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
                Overline(
                    stringResource(
                        if (initial.id == null) R.string.goals_new_overline else R.string.goals_edit_overline,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        if (initial.id == null) R.string.goals_dialog_new else R.string.goals_dialog_edit,
                    ),
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
                    label = { Text(stringResource(R.string.label_name)) },
                    placeholder = { Text(stringResource(R.string.goals_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = filterAmountInput(it, 10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_goal)) },
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
                        text = deadline?.let {
                            stringResource(R.string.goals_deadline, formatShortDate(it))
                        } ?: stringResource(R.string.goals_no_deadline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (deadline == null) colors.inkMuted else colors.ink,
                        modifier = Modifier.weight(1f),
                    )
                    if (deadline != null) {
                        Text(
                            text = stringResource(R.string.action_remove),
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
                Text(stringResource(R.string.label_icon), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
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
                Text(stringResource(R.string.label_color), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
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
                Text(
                    stringResource(if (initial.id == null) R.string.action_create else R.string.action_save),
                    color = colors.primary,
                )
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete), color = colors.expense) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
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
                ) { Text(stringResource(R.string.action_done), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
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
                Overline(
                    stringResource(
                        if (withdraw) R.string.goals_withdraw_overline else R.string.goals_contribute_overline,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(goal.displayName(), style = MaterialTheme.typography.headlineSmall, color = colors.ink)
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
                    AmountMode(stringResource(R.string.action_contribute), !withdraw, Modifier.weight(1f)) { withdraw = false }
                    AmountMode(stringResource(R.string.action_withdraw), withdraw, Modifier.weight(1f)) { withdraw = true }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                    R.string.goals_saved_progress,
                    formatMoney(goal.savedCents, currency),
                    formatMoney(goal.targetCents, currency),
                ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = filterAmountInput(it, 10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            stringResource(
                                if (withdraw) R.string.goals_amount_withdraw else R.string.goals_amount_contribute,
                            ),
                        )
                    },
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
                                text = formatMoney(value, currency),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (parsed > 0) {
                    stringResource(R.string.goals_result, formatMoney(resulting, currency))
                } else {
                    " "
                },
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
                Text(
                stringResource(if (withdraw) R.string.action_withdraw else R.string.action_contribute),
                color = if (withdraw) colors.expense else colors.primary,
            )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
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
