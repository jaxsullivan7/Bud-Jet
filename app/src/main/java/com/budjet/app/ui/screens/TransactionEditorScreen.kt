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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.TxType
import com.budjet.app.ui.EditorDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatFullDate
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
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

    var amountCents by remember { mutableLongStateOf(initial.amountCents) }
    var type by remember { mutableStateOf(initial.type) }
    var categoryId by remember { mutableStateOf(initial.categoryId) }
    var accountId by remember { mutableStateOf(initial.accountId) }
    var date by remember { mutableStateOf(initial.date) }
    var note by remember { mutableStateOf(initial.note) }

    var showAccounts by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val categories = data.categories.filter { it.type == type && !it.archived }
    val selectedCategory = data.category(categoryId)
        ?: categories.firstOrNull()
    val selectedAccount = data.account(accountId) ?: data.accounts.firstOrNull()
    val isEditing = initial.id != null

    fun pressDigit(digit: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        amountCents = (amountCents * 10 + digit).coerceAtMost(999_999_999_99L)
    }

    fun switchType(next: TxType) {
        if (type == next) return
        type = next
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
                contentDescription = "Cerrar",
                onClick = onClose,
                size = 38.dp,
                container = Color.Transparent,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Overline(if (isEditing) "EDITANDO" else "NUEVO MOVIMIENTO")
                Text(
                    text = if (isEditing) "Editar" else "Añadir",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(if (amountCents > 0) colors.primary else colors.surfaceMuted)
                    .clickable(enabled = amountCents > 0) {
                        onSave(
                            EditorDraft(
                                id = initial.id,
                                amountCents = amountCents,
                                type = type,
                                categoryId = selectedCategory?.id ?: categoryId,
                                accountId = selectedAccount?.id ?: accountId,
                                date = date,
                                note = note,
                            )
                        )
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Guardar",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (amountCents > 0) colors.onPrimary else colors.inkMuted,
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
                    text = "Gasto",
                    selected = type == TxType.EXPENSE,
                    onClick = { switchType(TxType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                )
                TypeButton(
                    text = "Ingreso",
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
                text = if (type == TxType.EXPENSE) "¿En qué se ha ido?" else "¿De dónde viene?",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(22.dp))
            Overline("Categoría")
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories, key = { it.id }) { category ->
                    CategoryOption(
                        category = category,
                        selected = selectedCategory?.id == category.id,
                        onClick = {
                            categoryId = category.id
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            BudCard(padding = 0.dp) {
                DetailRow(
                    icon = if (type == TxType.INCOME) Icons.Rounded.AccountBalance else Icons.Rounded.AccountBalance,
                    label = "Cuenta",
                    value = selectedAccount?.name ?: "Elige una cuenta",
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
                    label = "Fecha",
                    value = if (date == LocalDate.now()) "Hoy, ${formatShortDate(date)}" else formatFullDate(date),
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
                    label = "Nota",
                    value = note.ifBlank { "Añadir una nota" },
                    muted = note.isBlank(),
                    onClick = { showNote = true },
                )
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
                        text = "Eliminar movimiento",
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
            title = { Text("Cuenta", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
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
                TextButton(onClick = { showAccounts = false }) { Text("Cerrar", color = colors.primary) }
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
                ) { Text("Listo", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = colors.inkMuted) }
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
            title = { Text("Nota", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                OutlinedTextField(
                    value = noteValue,
                    onValueChange = { noteValue = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Cena con amigos") },
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
                ) { Text("Guardar", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showNote = false }) { Text("Cancelar", color = colors.inkMuted) }
            },
        )
    }

    if (confirmDelete && initial.id != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text("¿Eliminar movimiento?", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                Text(
                    "Podrás deshacerlo justo después desde el aviso.",
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
                ) { Text("Eliminar", color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar", color = colors.inkMuted) }
            },
        )
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
            text = category.name,
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
            account.name,
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
                        text = formatMoney(value, currency, decimals = false),
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
                                    contentDescription = "Borrar",
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
