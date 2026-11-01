package com.budjet.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Investment
import com.budjet.app.data.InvestmentEntry
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.data.InvestmentKind
import com.budjet.app.data.InvestmentMath
import com.budjet.app.data.InvestmentSummary
import com.budjet.app.data.PortfolioSummary
import com.budjet.app.data.ProductSummary
import com.budjet.app.ui.InvestmentEntryDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.DonutChart
import com.budjet.app.ui.components.DonutSegment
import com.budjet.app.ui.components.EmptyState
import com.budjet.app.ui.components.MiniBarChart
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.ThinDivider
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMonthShort
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.parseMoneyInput
import com.budjet.app.util.percentText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

@Composable
fun PortfolioScreen(
    data: AppData,
    onClose: () -> Unit,
    onAddInvestment: (name: String, kind: InvestmentKind) -> Unit,
    onUpdateInvestment: (id: String, name: String, kind: InvestmentKind) -> Unit,
    onDeleteInvestment: (id: String) -> Unit,
    onAddEntry: (InvestmentEntryDraft) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val today = remember { LocalDate.now() }
    val summary = remember(data.investments, data.investmentEntries) {
        InvestmentMath.portfolio(data.investments, data.investmentEntries, today)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showCalculator by remember { mutableStateOf(false) }
    var productDialog by remember { mutableStateOf<ProductDialogState?>(null) }
    var entryAction by remember { mutableStateOf<EntryAction?>(null) }
    var deleteTarget by remember { mutableStateOf<Investment?>(null) }

    val selected = data.investment(selectedId)
    LaunchedEffect(selected) {
        if (selectedId != null && selected == null) selectedId = null
    }

    BackHandler(enabled = showCalculator || selectedId != null) {
        if (showCalculator) showCalculator = false else selectedId = null
    }

    if (showCalculator) {
        CompoundCalculatorScreen(
            initialAnnualRate = summary.annualizedReturn?.times(100.0),
            currency = currency,
            onBack = { showCalculator = false },
        )
        return
    }

    if (selected != null) {
        ProductDetail(
            data = data,
            product = selected,
            currency = currency,
            today = today,
            onBack = { selectedId = null },
            onEdit = { productDialog = ProductDialogState(existing = selected) },
            onAction = { kind -> entryAction = EntryAction(selected, kind) },
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("portfolio-list"),
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
                    Overline("ANÁLISIS · CARTERA")
                    SoftIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = "Nuevo producto",
                        onClick = { productDialog = ProductDialogState() },
                        size = 38.dp,
                        container = colors.primarySoft,
                        tint = colors.primary,
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Tu cartera.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Inversión con seguimiento propio. No se mezcla con el saldo disponible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(20.dp))
                PortfolioHero(summary = summary, currency = currency)
                Spacer(Modifier.height(14.dp))
            }

            if (summary.products.isEmpty()) {
                item {
                    BudCard {
                        EmptyState(
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            title = "Tu cartera está vacía",
                            message = "Crea un producto, registra aportaciones y sigue su rentabilidad en el tiempo.",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primary)
                                .clickable { productDialog = ProductDialogState() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Crear producto",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onPrimary,
                            )
                        }
                    }
                }
            } else {
                item {
                    AllocationCard(summary = summary, currency = currency)
                    Spacer(Modifier.height(14.dp))
                }
                item {
                    Overline("PRODUCTOS")
                    Spacer(Modifier.height(8.dp))
                }
                items(summary.products, key = { it.investment.id }) { product ->
                    ProductCard(
                        summary = product,
                        currency = currency,
                        onClick = { selectedId = product.investment.id },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                BudCard(
                    modifier = Modifier.testTag("calculator-card"),
                    onClick = { showCalculator = true },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.primarySoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Calculate,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Calculadora de interés compuesto",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.ink,
                            )
                            Text(
                                text = "Simula tu inversión con rentabilidad y volatilidad opcional.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted,
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.inkMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "La cartera es un seguimiento separado: las aportaciones no modifican tu saldo disponible en esta versión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }
    }

    productDialog?.let { state ->
        ProductEditorDialog(
            state = state,
            onDismiss = { productDialog = null },
            onSave = { name, kind ->
                if (state.existing == null) {
                    onAddInvestment(name, kind)
                } else {
                    onUpdateInvestment(state.existing.id, name, kind)
                }
                productDialog = null
            },
            onDelete = state.existing?.let {
                {
                    productDialog = null
                    deleteTarget = state.existing
                }
            },
        )
    }

    entryAction?.let { action ->
        InvestmentEntryDialog(
            product = action.product,
            action = action.kind,
            summary = summary.products.firstOrNull { it.investment.id == action.product.id }?.summary,
            currency = currency,
            onDismiss = { entryAction = null },
            onSave = { amount, date, note ->
                onAddEntry(
                    InvestmentEntryDraft(
                        investmentId = action.product.id,
                        kind = action.kind,
                        amountCents = amount,
                        date = date,
                        note = note,
                    ),
                )
                entryAction = null
            },
        )
    }

    deleteTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text("¿Eliminar producto?", color = colors.ink) },
            text = {
                Text(
                    text = "Se borrarán «${product.name}» y todo su historial. No afecta a tus movimientos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteInvestment(product.id)
                        selectedId = null
                        deleteTarget = null
                    },
                ) { Text("Eliminar", color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar", color = colors.inkMuted) }
            },
        )
    }
}

private data class ProductDialogState(val existing: Investment? = null)
private data class EntryAction(val product: Investment, val kind: InvestmentEntryKind)

@Composable
private fun PortfolioHero(summary: PortfolioSummary, currency: Currency) {
    val colors = BudTheme.colors
    val positive = summary.returnsCents >= 0
    BudCard(color = colors.heroStart, border = false) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Overline("VALOR DE LA CARTERA", color = colors.heroMuted)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatMoney(summary.valueCents, currency),
                    style = BudTheme.type.moneyHero.copy(fontSize = 34.sp, lineHeight = 40.sp),
                    color = colors.heroText,
                    maxLines = 1,
                )
            }
            if (summary.returnPercent != null) {
                Pill(
                    text = (if (positive) "+" else "−") +
                        percentText(abs(summary.returnPercent), decimals = 1),
                    container = Color.White.copy(alpha = 0.12f),
                    content = colors.heroText,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            Column {
                Text("Aportado", style = MaterialTheme.typography.bodySmall, color = colors.heroMuted)
                Text(
                    text = formatMoney(summary.contributedCents, currency),
                    style = BudTheme.type.moneySmall,
                    color = colors.heroText,
                )
            }
            Column {
                Text("Rentabilidad", style = MaterialTheme.typography.bodySmall, color = colors.heroMuted)
                Text(
                    text = (if (positive) "+" else "−") + formatMoney(abs(summary.returnsCents), currency),
                    style = BudTheme.type.moneySmall,
                    color = if (positive) colors.heroText else colors.expense,
                )
            }
            Column {
                Text("Anualizada", style = MaterialTheme.typography.bodySmall, color = colors.heroMuted)
                Text(
                    text = summary.annualizedReturn?.let { percentText(it.toFloat(), decimals = 1) } ?: "—",
                    style = BudTheme.type.moneySmall,
                    color = colors.heroText,
                )
            }
        }
    }
}

@Composable
private fun AllocationCard(summary: PortfolioSummary, currency: Currency) {
    val colors = BudTheme.colors
    val products = summary.products.filter { it.summary.valueCents > 0 }.sortedByDescending {
        it.summary.valueCents
    }
    if (products.isEmpty()) return
    val segments = products.map {
        DonutSegment(
            value = it.summary.valueCents.toFloat(),
            color = categoryColor(it.investment.kind.colorHex, colors.isDark),
        )
    }
    BudCard {
        Overline("DISTRIBUCIÓN")
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DonutChart(
                segments = segments,
                modifier = Modifier.size(112.dp),
                strokeWidth = 14.dp,
            ) {
                Text(
                    text = "${products.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                products.take(4).forEach { product ->
                    val share = if (summary.valueCents > 0) {
                        product.summary.valueCents.toFloat() / summary.valueCents
                    } else 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(7.dp)
                                .background(
                                    categoryColor(product.investment.kind.colorHex, colors.isDark),
                                    CircleShape,
                                ),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = product.investment.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = percentText(share, decimals = 0),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(summary: ProductSummary, currency: Currency, onClick: () -> Unit) {
    val colors = BudTheme.colors
    val color = categoryColor(summary.investment.kind.colorHex, colors.isDark)
    val positive = summary.summary.returnsCents >= 0
    BudCard(
        modifier = Modifier.testTag("product-${summary.investment.id}"),
        onClick = onClick,
        padding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InvestmentAvatar(kind = summary.investment.kind, size = 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = summary.investment.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary.investment.kind.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(summary.summary.valueCents, currency),
                    style = BudTheme.type.moneySmall,
                    color = colors.ink,
                )
                Text(
                    text = summary.summary.returnPercent?.let {
                        (if (positive) "+" else "−") + percentText(abs(it), decimals = 1)
                    } ?: "—",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (positive) colors.income else colors.expense,
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row {
            Text(
                text = "Aportado ${formatMoney(summary.summary.contributedCents, currency)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun ProductDetail(
    data: AppData,
    product: Investment,
    currency: Currency,
    today: LocalDate,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAction: (InvestmentEntryKind) -> Unit,
) {
    val colors = BudTheme.colors
    val entries = remember(data.investmentEntries, product.id) { data.entriesFor(product.id) }
    val summary = remember(entries) { InvestmentMath.summary(entries, today) }
    val series = remember(entries, today) {
        InvestmentMath.valueSeries(entries, 12, today).map {
            formatMonthShort(it.first) to it.second
        }
    }
    val color = categoryColor(product.kind.colorHex, colors.isDark)

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
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline("PRODUCTO")
                SoftIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = "Editar producto",
                    onClick = onEdit,
                    size = 38.dp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InvestmentAvatar(kind = product.kind, size = 52.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.ink,
                    )
                    Text(
                        text = product.kind.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = formatMoney(summary.valueCents, currency),
                style = BudTheme.type.moneyHero.copy(fontSize = 38.sp, lineHeight = 44.sp),
                color = colors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(
                    text = (if (summary.returnsCents >= 0) "+" else "−") +
                        formatMoney(abs(summary.returnsCents), currency),
                    container = if (summary.returnsCents >= 0) colors.primarySoft else colors.expense.copy(alpha = 0.12f),
                    content = if (summary.returnsCents >= 0) colors.income else colors.expense,
                    icon = if (summary.returnsCents >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                )
                if (summary.returnPercent != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = percentText(abs(summary.returnPercent), decimals = 1),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            BudCard(padding = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                ) {
                    ProductMetric("APORTADO", formatMoney(summary.contributedCents, currency), Modifier.weight(1f))
                    ProductMetric("RENDIMIENTO", formatMoney(summary.returnsCents, currency), Modifier.weight(1f))
                    ProductMetric(
                        "ANUALIZADA",
                        summary.annualizedReturn?.let { percentText(it.toFloat(), decimals = 1) } ?: "—",
                        Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Aportar", Icons.Rounded.Add, Modifier.weight(1f)) {
                    onAction(InvestmentEntryKind.CONTRIBUTION)
                }
                ActionButton("Retirar", Icons.AutoMirrored.Rounded.TrendingDown, Modifier.weight(1f)) {
                    onAction(InvestmentEntryKind.WITHDRAWAL)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Rendimiento", Icons.AutoMirrored.Rounded.TrendingUp, Modifier.weight(1f)) {
                    onAction(InvestmentEntryKind.RETURN)
                }
                ActionButton("Actualizar valor", Icons.Rounded.Edit, Modifier.weight(1f)) {
                    onAction(InvestmentEntryKind.VALUATION)
                }
            }
            Spacer(Modifier.height(26.dp))
            BudCard {
                Overline("EVOLUCIÓN · 12 MESES")
                Spacer(Modifier.height(12.dp))
                MiniBarChart(values = series, color = color)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = colors.line)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Valor actual",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Text(
                        text = formatMoney(summary.valueCents, currency),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.ink,
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Historial",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    Text(
                        text = "${entries.size} ${if (entries.size == 1) "movimiento" else "movimientos"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (entries.isEmpty()) {
                BudCard {
                    Text(
                        text = "Aún no hay aportaciones. Empieza con el botón Aportar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            } else {
                entries.sortedByDescending { it.dateEpochDay }.forEachIndexed { index, entry ->
                    InvestmentEntryRow(
                        entry = entry,
                        currency = currency,
                    )
                    if (index != entries.lastIndex) ThinDivider()
                }
            }
        }
    }
}

@Composable
private fun ProductMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            color = BudTheme.colors.inkMuted,
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            style = BudTheme.type.moneySmall.copy(fontSize = 12.sp),
            color = BudTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InvestmentEntryRow(entry: InvestmentEntry, currency: Currency) {
    val colors = BudTheme.colors
    val positive = entry.amountCents >= 0
    val amount = when (entry.kind) {
        InvestmentEntryKind.CONTRIBUTION -> "+" + formatMoney(entry.amountCents, currency)
        InvestmentEntryKind.WITHDRAWAL -> "−" + formatMoney(entry.amountCents, currency)
        InvestmentEntryKind.RETURN,
        InvestmentEntryKind.VALUATION,
        -> (if (positive) "+" else "−") + formatMoney(abs(entry.amountCents), currency)
    }
    val amountColor = when {
        entry.kind == InvestmentEntryKind.WITHDRAWAL -> colors.expense
        entry.kind == InvestmentEntryKind.RETURN || entry.kind == InvestmentEntryKind.VALUATION ->
            if (positive) colors.income else colors.expense
        else -> colors.ink
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.surfaceMuted),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = when (entry.kind) {
                    InvestmentEntryKind.CONTRIBUTION -> Icons.Rounded.Add
                    InvestmentEntryKind.WITHDRAWAL -> Icons.AutoMirrored.Rounded.TrendingDown
                    InvestmentEntryKind.RETURN, InvestmentEntryKind.VALUATION -> Icons.AutoMirrored.Rounded.TrendingUp
                },
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.note.ifBlank { entry.kind.label },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatShortDate(LocalDate.ofEpochDay(entry.dateEpochDay)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
        Text(
            text = amount,
            style = BudTheme.type.moneySmall,
            color = amountColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun InvestmentAvatar(kind: InvestmentKind, size: androidx.compose.ui.unit.Dp) {
    val colors = BudTheme.colors
    val color = categoryColor(kind.colorHex, colors.isDark)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = if (colors.isDark) 0.2f else 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(kind.icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

@Composable
private fun ProductEditorDialog(
    state: ProductDialogState,
    onDismiss: () -> Unit,
    onSave: (name: String, kind: InvestmentKind) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val colors = BudTheme.colors
    var name by remember { mutableStateOf(state.existing?.name.orEmpty()) }
    var kind by remember { mutableStateOf(state.existing?.kind ?: InvestmentKind.INDEX_FUND) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(if (state.existing == null) "NUEVO PRODUCTO" else "EDITAR PRODUCTO")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (state.existing == null) "¿Qué vas a seguir?" else "Ajusta el producto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(32) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Fondo indexado global") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text("Tipo", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(InvestmentKind.entries) { option ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (kind == option) colors.primarySoft else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (kind == option) colors.primary else colors.line,
                                    CircleShape,
                                )
                                .clickable { kind = option }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                categoryIcon(option.icon),
                                contentDescription = null,
                                tint = categoryColor(option.colorHex, colors.isDark),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (kind == option) colors.primary else colors.inkMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, kind) },
                enabled = name.isNotBlank(),
            ) { Text(if (state.existing == null) "Crear" else "Guardar", color = colors.primary) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Eliminar", color = colors.expense)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
            }
        },
    )
}

@Composable
private fun InvestmentEntryDialog(
    product: Investment,
    action: InvestmentEntryKind,
    summary: InvestmentSummary?,
    currency: Currency,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, date: LocalDate, note: String) -> Unit,
) {
    val colors = BudTheme.colors
    val currentValue = summary?.valueCents ?: 0L
    var amount by remember { mutableStateOf("") }
    var isLoss by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsed = parseMoneyInput(amount)
    val signedAmount = when (action) {
        InvestmentEntryKind.CONTRIBUTION,
        InvestmentEntryKind.WITHDRAWAL,
        -> parsed
        InvestmentEntryKind.RETURN -> if (isLoss) -parsed else parsed
        InvestmentEntryKind.VALUATION -> parsed - currentValue
    }
    val valid = when (action) {
        InvestmentEntryKind.CONTRIBUTION -> parsed > 0
        InvestmentEntryKind.WITHDRAWAL -> parsed > 0 && parsed <= currentValue
        InvestmentEntryKind.RETURN -> parsed > 0
        InvestmentEntryKind.VALUATION -> parsed >= 0 && signedAmount != 0L
    }

    val title = when (action) {
        InvestmentEntryKind.CONTRIBUTION -> "Aportar"
        InvestmentEntryKind.WITHDRAWAL -> "Retirar"
        InvestmentEntryKind.RETURN -> "Registrar rendimiento"
        InvestmentEntryKind.VALUATION -> "Actualizar valor"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(product.name.uppercase())
                Spacer(Modifier.height(4.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            }
        },
        text = {
            Column {
                if (action == InvestmentEntryKind.VALUATION) {
                    Text(
                        text = "Valor actual registrado: ${formatMoney(currentValue, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (action == InvestmentEntryKind.RETURN) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceMuted)
                            .padding(3.dp),
                    ) {
                        EntryTypeButton("Ganancia", !isLoss, Modifier.weight(1f)) { isLoss = false }
                        EntryTypeButton("Pérdida", isLoss, Modifier.weight(1f)) { isLoss = true }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { text ->
                        if (text.length <= 11 && text.all { it.isDigit() || it == ',' }) amount = text
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            when (action) {
                                InvestmentEntryKind.VALUATION -> "Valor actual"
                                InvestmentEntryKind.RETURN -> "Importe del rendimiento"
                                else -> "Importe"
                            },
                        )
                    },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                if (action == InvestmentEntryKind.VALUATION && parsed > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Ajuste: ${if (signedAmount >= 0) "+" else "−"}${formatMoney(abs(signedAmount), currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (signedAmount >= 0) colors.income else colors.expense,
                    )
                }
                if (action == InvestmentEntryKind.WITHDRAWAL && parsed > currentValue) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "No puedes retirar más del valor actual (${formatMoney(currentValue, currency)}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.expense,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceMuted.copy(alpha = 0.5f))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (date == LocalDate.now()) "Hoy, ${formatShortDate(date)}" else formatShortDate(date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(48) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota (opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(signedAmount, date, note) },
                enabled = valid,
            ) { Text("Guardar", color = colors.primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
        },
    )

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
}

@Composable
private fun EntryTypeButton(
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
