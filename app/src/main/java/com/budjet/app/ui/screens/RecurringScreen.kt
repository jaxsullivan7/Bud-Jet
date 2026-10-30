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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.budjet.app.data.Recurring
import com.budjet.app.data.RecurringRule
import com.budjet.app.data.TxType
import com.budjet.app.ui.RecurringDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.BudSwitch
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.centsToInput
import com.budjet.app.util.formatDayNumber
import com.budjet.app.util.formatMoney
import com.budjet.app.util.parseMoneyInput
import java.time.LocalDate

@Composable
fun RecurringScreen(
    data: AppData,
    onClose: () -> Unit,
    onSave: (RecurringDraft) -> Unit,
    onDelete: (String) -> Unit,
    onToggle: (id: String, active: Boolean) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val rules = data.recurringRules
    var editor by remember { mutableStateOf<RecurringDraft?>(null) }
    var deleting by remember { mutableStateOf<String?>(null) }
    val today = remember { LocalDate.now() }

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
                Overline("AUTOMATIZACIÓN")
                SoftIconButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Nuevo movimiento recurrente",
                    onClick = {
                        editor = RecurringDraft(
                            type = TxType.EXPENSE,
                            categoryId = data.categories.firstOrNull { it.type == TxType.EXPENSE }?.id ?: "home",
                            accountId = data.accounts.firstOrNull()?.id ?: "main",
                            dayOfMonth = today.dayOfMonth,
                        )
                    },
                    size = 38.dp,
                    container = colors.primarySoft,
                    tint = colors.primary,
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Se repiten solos.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Nómina, alquiler o suscripciones: se apuntan automáticamente el día que toca, al abrir la app.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(22.dp))
        }

        if (rules.isEmpty()) {
            item {
                BudCard {
                    EmptyState(
                        icon = Icons.Rounded.EventRepeat,
                        title = "Nada repetido todavía",
                        message = "Crea tu primera regla y olvídate de apuntar la nómina o el alquiler.",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .clickable {
                                editor = RecurringDraft(
                                    categoryId = data.categories.firstOrNull { it.type == TxType.EXPENSE }?.id ?: "home",
                                    accountId = data.accounts.firstOrNull()?.id ?: "main",
                                    dayOfMonth = today.dayOfMonth,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Crear regla", style = MaterialTheme.typography.titleSmall, color = colors.onPrimary)
                    }
                }
            }
        } else {
            items(rules, key = { it.id }) { rule ->
                RecurringCard(
                    rule = rule,
                    data = data,
                    currency = currency,
                    today = today,
                    onToggle = { onToggle(rule.id, it) },
                    onClick = {
                        editor = RecurringDraft(
                            id = rule.id,
                            amountCents = rule.amountCents,
                            type = rule.type,
                            categoryId = rule.categoryId,
                            accountId = rule.accountId,
                            dayOfMonth = rule.dayOfMonth,
                            note = rule.note,
                            active = rule.active,
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                        .clickable {
                            editor = RecurringDraft(
                                categoryId = data.categories.firstOrNull { it.type == TxType.EXPENSE }?.id ?: "home",
                                accountId = data.accounts.firstOrNull()?.id ?: "main",
                                dayOfMonth = today.dayOfMonth,
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Nueva regla", style = MaterialTheme.typography.titleSmall, color = colors.ink)
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Los cargos ya generados no se borran si eliminas la regla.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }
    }

    editor?.let { draft ->
        RecurringEditorDialog(
            initial = draft,
            categories = data.categories.filter { !it.archived },
            accounts = data.accounts,
            currency = currency,
            onDismiss = { editor = null },
            onSave = {
                onSave(it)
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

    deleting?.let { id ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = colors.surface,
            shape = MaterialTheme.shapes.large,
            title = { Text("¿Eliminar la regla?", color = colors.ink) },
            text = {
                Text(
                    "Dejará de apuntarse cada mes. Los movimientos ya creados se mantienen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(id)
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
private fun RecurringCard(
    rule: RecurringRule,
    data: AppData,
    currency: Currency,
    today: LocalDate,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    val category = data.category(rule.categoryId)
    val account = data.account(rule.accountId)
    val next = Recurring.nextOccurrence(rule, today)

    BudCard(onClick = onClick, padding = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            CategoryBadge(category = category, size = 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = rule.note.ifBlank { category?.name ?: "Recurrente" },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(category?.name ?: "Sin categoría")
                        append(" · día ")
                        append(rule.dayOfMonth)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = (if (rule.type == TxType.INCOME) "+" else "−") +
                    formatMoney(rule.amountCents, currency, decimals = rule.amountCents % 100L != 0L),
                style = BudTheme.type.moneySmall,
                color = if (rule.type == TxType.INCOME) colors.income else colors.ink,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.EventRepeat,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = account?.name ?: "Cuenta",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (!rule.active) "Pausado" else next?.let { "Próximo ${formatDayNumber(it)}" } ?: "Pausado",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = if (rule.active) colors.inkMuted else colors.warning,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            BudSwitch(
                checked = rule.active,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun RecurringEditorDialog(
    initial: RecurringDraft,
    categories: List<Category>,
    accounts: List<com.budjet.app.data.Account>,
    currency: Currency,
    onDismiss: () -> Unit,
    onSave: (RecurringDraft) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val colors = BudTheme.colors
    var type by remember { mutableStateOf(initial.type) }
    var amount by remember { mutableStateOf(if (initial.amountCents > 0) centsToInput(initial.amountCents) else "") }
    var categoryId by remember { mutableStateOf(initial.categoryId) }
    var accountId by remember { mutableStateOf(initial.accountId) }
    var day by remember { mutableIntStateOf(initial.dayOfMonth) }
    var note by remember { mutableStateOf(initial.note) }
    val filtered = categories.filter { it.type == type }
    val selectedCategory = filtered.firstOrNull { it.id == categoryId } ?: filtered.firstOrNull()
    val cents = parseMoneyInput(amount)
    val dayValid = day in 1..31

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(if (initial.id == null) "NUEVA REGLA" else "EDITAR REGLA")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (initial.id == null) "Algo que se repite" else "Ajusta la regla",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            }
        },
        text = {
            Column(
                Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceMuted)
                        .padding(3.dp),
                ) {
                    TypeChoice("Gasto", type == TxType.EXPENSE, Modifier.weight(1f)) {
                        type = TxType.EXPENSE
                        filtered.let { if (it.none { c -> c.id == categoryId }) categoryId = it.firstOrNull()?.id ?: categoryId }
                    }
                    TypeChoice("Ingreso", type == TxType.INCOME, Modifier.weight(1f)) {
                        type = TxType.INCOME
                        categories.filter { it.type == TxType.INCOME }.let {
                            if (it.none { c -> c.id == categoryId }) categoryId = it.firstOrNull()?.id ?: categoryId
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { text ->
                        if (text.length <= 10 && text.all { it.isDigit() || it == ',' }) amount = text
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Importe") },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text("Categoría", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { category ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selectedCategory?.id == category.id) colors.primarySoft else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (selectedCategory?.id == category.id) colors.primary else colors.line,
                                    CircleShape,
                                )
                                .clickable { categoryId = category.id }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .background(categoryColor(category.colorHex, colors.isDark), CircleShape),
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedCategory?.id == category.id) colors.primary else colors.inkMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Cuenta", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.take(4).forEach { account ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (accountId == account.id) colors.primarySoft else Color.Transparent)
                                .border(1.dp, if (accountId == account.id) colors.primary else colors.line, CircleShape)
                                .clickable { accountId = account.id }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = categoryIcon(account.icon),
                                contentDescription = null,
                                tint = colors.inkMuted,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (accountId == account.id) colors.primary else colors.inkMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = { text ->
                            if (text.length <= 2 && text.all { it.isDigit() }) {
                                day = text.toIntOrNull() ?: 0
                            }
                        },
                        modifier = Modifier.width(110.dp),
                        label = { Text("Día") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "de cada mes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota") },
                    placeholder = { Text("Ej. Nómina") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                if (!dayValid) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "El día debe estar entre 1 y 31.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.expense,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        RecurringDraft(
                            id = initial.id,
                            amountCents = cents,
                            type = type,
                            categoryId = selectedCategory?.id ?: categoryId,
                            accountId = accountId,
                            dayOfMonth = day,
                            note = note,
                            active = initial.active,
                        ),
                    )
                },
                enabled = cents > 0 && dayValid && selectedCategory != null,
            ) { Text(if (initial.id == null) "Crear" else "Guardar", color = colors.primary) }
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
}

@Composable
private fun TypeChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
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
