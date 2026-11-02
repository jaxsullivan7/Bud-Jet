package com.budjet.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.CsvMapping
import com.budjet.app.data.Currency
import com.budjet.app.data.ImportRow
import com.budjet.app.data.ParsedStatement
import com.budjet.app.data.StatementImport
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import java.time.LocalDate

@Composable
fun ImportScreen(
    data: AppData,
    onClose: () -> Unit,
    onImport: (rows: List<ImportRow>, accountId: String) -> Unit,
) {
    val colors = BudTheme.colors
    val context = LocalContext.current
    val currency = Currency.from(data.settings.currencyCode)
    val fallback = data.categories.firstOrNull { it.type == TxType.EXPENSE && !it.archived }?.id ?: ""
    var accountId by remember { mutableStateOf(data.accounts.firstOrNull()?.id ?: "") }
    var rawText by remember { mutableStateOf<String?>(null) }
    var parsed by remember { mutableStateOf<ParsedStatement?>(null) }
    var rows by remember { mutableStateOf<List<ImportRow>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var error by remember { mutableStateOf<String?>(null) }
    var mappingOpen by remember { mutableStateOf(false) }
    var categoryTarget by remember { mutableStateOf<Int?>(null) }

    fun applyRows(list: List<ImportRow>) {
        rows = list
        selected = list.indices.filter { !list[it].duplicate }.toSet()
    }

    fun parse(text: String, mapping: CsvMapping? = null) {
        val result = StatementImport.parse(
            text = text,
            accountId = accountId,
            fallbackCategoryId = fallback,
            rules = data.categoryRules,
            existingHashes = data.transactions.mapNotNull { it.importHash }.toSet(),
            mapping = mapping,
        )
        result.onSuccess {
            parsed = it
            applyRows(it.rows)
            error = null
        }.onFailure {
            val headers = StatementImport.csvHeaders(text)
            if (headers.size >= 2) {
                parsed = ParsedStatement(emptyList(), headers, null, isOfx = false)
                rows = emptyList()
                selected = emptySet()
                error = "No se han detectado las columnas. Elige fecha, concepto e importe."
            } else {
                error = it.message ?: "No se pudo leer el archivo."
                parsed = null
                rows = emptyList()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("No se pudo leer el archivo.")
            if (bytes.size > 10 * 1024 * 1024) error("El archivo es demasiado grande.")
            String(bytes, Charsets.UTF_8)
        }.getOrElse {
            error = it.message ?: "No se pudo leer el archivo."
            null
        }
        if (text != null) {
            rawText = text
            parse(text)
        }
    }

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
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline("DATOS · IMPORTAR")
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Trae tu extracto.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "CSV u OFX de tu banco. Se procesa en el dispositivo; no se envía nada a ningún servidor.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(18.dp))
        }

        item {
            BudCard(padding = 14.dp) {
                Text("Cuenta de destino", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(data.accounts) { _, account ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (accountId == account.id) colors.primarySoft else Color.Transparent)
                                .clickable {
                                    accountId = account.id
                                    rawText?.let { parse(it, parsed?.mapping) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (accountId == account.id) colors.primary else colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = if (parsed == null) "Elegir archivo" else "Elegir otro archivo",
                    onClick = { launcher.launch(arrayOf("text/*", "application/*", "application/octet-stream")) },
                    icon = Icons.Rounded.FileOpen,
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error.orEmpty(), style = MaterialTheme.typography.bodySmall, color = colors.expense)
                }
            }
        }

        parsed?.let { preview ->
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "${preview.rows.size} movimientos detectados",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                        Text(
                            text = "${preview.rows.count { it.duplicate }} duplicados ya están en Bud Jet",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                    if (preview.headers.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { mappingOpen = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Columnas", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Revisa la categoría de cada movimiento antes de importar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(6.dp))
            }

            itemsIndexed(rows) { index, row ->
                ImportRowCard(
                    row = row,
                    index = index,
                    selected = index in selected,
                    category = data.category(row.categoryId),
                    currency = currency,
                    onToggle = {
                        selected = if (index in selected) selected - index else selected + index
                    },
                    onCategory = { categoryTarget = index },
                    onToggleType = {
                        rows = rows.toMutableList().also { list ->
                            val newType = if (list[index].type == TxType.INCOME) TxType.EXPENSE else TxType.INCOME
                            list[index] = list[index].copy(
                                type = newType,
                                categoryId = data.categories
                                    .firstOrNull { it.type == newType && !it.archived }
                                    ?.id ?: list[index].categoryId,
                            )
                        }
                    },
                )
                Spacer(Modifier.height(6.dp))
            }

            item {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Importar ${selected.size} movimientos",
                    onClick = {
                        onImport(rows.filterIndexed { index, _ -> index in selected }, accountId)
                        onClose()
                    },
                    icon = Icons.Rounded.Check,
                    enabled = selected.isNotEmpty() && accountId.isNotBlank(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Los duplicados detectados no se importan dos veces. Puedes cambiar la categoría de cada fila.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }

        if (parsed == null && rawText == null) {
            item {
                Spacer(Modifier.height(18.dp))
                BudCard(color = colors.primarySoft, border = false) {
                    Text(
                        text = "Consejo: si tu banco exporta un CSV con columnas raras, podrás elegir cuál es la fecha, el concepto y el importe después de abrirlo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                }
            }
        }
    }

    if (mappingOpen && parsed != null && rawText != null && parsed!!.headers.isNotEmpty()) {
        MappingDialog(
            headers = parsed!!.headers,
            initial = parsed!!.mapping ?: CsvMapping(
                dateIndex = 0,
                descriptionIndex = minOf(1, parsed!!.headers.lastIndex),
                amountIndex = null,
                debitIndex = null,
                creditIndex = null,
                typeIndex = null,
            ),
            onDismiss = { mappingOpen = false },
            onApply = {
                mappingOpen = false
                rawText?.let { text -> parse(text, it) }
            },
        )
    }

    categoryTarget?.let { index ->
        val row = rows.getOrNull(index)
        if (row != null) {
            CategoryPickerDialog(
                categories = data.categories.filter { it.type == row.type && !it.archived },
                selected = row.categoryId,
                onDismiss = { categoryTarget = null },
                onSelect = { categoryId ->
                    rows = rows.toMutableList().also { list ->
                        list[index] = list[index].copy(categoryId = categoryId)
                    }
                    categoryTarget = null
                },
            )
        }
    }
}

@Composable
private fun ImportRowCard(
    row: ImportRow,
    index: Int,
    selected: Boolean,
    category: Category?,
    currency: Currency,
    onToggle: () -> Unit,
    onCategory: () -> Unit,
    onToggleType: () -> Unit,
) {
    val colors = BudTheme.colors
    BudCard(
        padding = 12.dp,
        color = if (row.duplicate) colors.surfaceMuted.copy(alpha = 0.45f) else colors.surface,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
                enabled = !row.duplicate,
            )
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (row.duplicate) colors.inkMuted else colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatShortDate(row.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    if (row.duplicate) {
                        Spacer(Modifier.width(6.dp))
                        Text("Duplicado", style = MaterialTheme.typography.bodySmall, color = colors.expense)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (row.type == TxType.INCOME) "+" else "−") + formatMoney(row.amountCents, currency),
                    style = BudTheme.type.moneySmall,
                    color = if (row.type == TxType.INCOME) colors.income else colors.expense,
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onToggleType)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (row.type == TxType.INCOME) "Ingreso" else "Gasto",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onCategory)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (category != null) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MappingDialog(
    headers: List<String>,
    initial: CsvMapping,
    onDismiss: () -> Unit,
    onApply: (CsvMapping) -> Unit,
) {
    val colors = BudTheme.colors
    var date by remember { mutableStateOf<Int?>(initial.dateIndex) }
    var description by remember { mutableStateOf<Int?>(initial.descriptionIndex) }
    var amount by remember { mutableStateOf(initial.amountIndex) }
    var debit by remember { mutableStateOf(initial.debitIndex) }
    var credit by remember { mutableStateOf(initial.creditIndex) }
    var type by remember { mutableStateOf(initial.typeIndex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Columnas del CSV", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
        text = {
            Column {
                Text(
                    text = "Indica qué columna contiene cada dato. El importe puede ser una columna única o separarse en cargo y abono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                MappingPicker("Fecha", headers, date) { date = it }
                MappingPicker("Concepto", headers, description) { description = it }
                MappingPicker("Importe", headers, amount, nullable = true) { amount = it }
                MappingPicker("Cargo (gasto)", headers, debit, nullable = true) { debit = it }
                MappingPicker("Abono (ingreso)", headers, credit, nullable = true) { credit = it }
                MappingPicker("Tipo", headers, type, nullable = true) { type = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(
                        CsvMapping(
                            dateIndex = date ?: initial.dateIndex,
                            descriptionIndex = description ?: initial.descriptionIndex,
                            amountIndex = amount,
                            debitIndex = debit,
                            creditIndex = credit,
                            typeIndex = type,
                        ),
                    )
                },
                enabled = (amount != null || (debit != null && credit != null)),
            ) { Text("Aplicar", color = colors.primary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) } },
    )
}

@Composable
private fun MappingPicker(
    label: String,
    headers: List<String>,
    selected: Int?,
    nullable: Boolean = false,
    onSelect: (Int?) -> Unit,
) {
    val colors = BudTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.ink, modifier = Modifier.weight(1f))
            Text(
                text = selected?.let { headers.getOrNull(it) } ?: "—",
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (nullable) {
                DropdownMenuItem(
                    text = { Text("— Sin columna —") },
                    onClick = { onSelect(null); expanded = false },
                )
            }
            headers.forEachIndexed { index, header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = { onSelect(index); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<Category>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val colors = BudTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text("Categoría", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
        text = {
            LazyColumn {
                itemsIndexed(categories) { _, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(category.id) }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        if (category.id == selected) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = colors.primary) } },
    )
}
