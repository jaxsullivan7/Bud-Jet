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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.IrpfMath
import com.budjet.app.data.IrpfRegion
import com.budjet.app.data.PayrollKind
import com.budjet.app.data.Payslip
import com.budjet.app.ui.PayrollDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.displayName
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.centsToInput
import com.budjet.app.util.filterAmountInput
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.parseMoneyInput
import com.budjet.app.util.percentText
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@Composable
fun PayrollScreen(
    data: AppData,
    onClose: () -> Unit,
    onSave: (PayrollDraft) -> Unit,
    onDelete: (String) -> Unit,
    onSetRegion: (IrpfRegion) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    var year by rememberSaveable { mutableStateOf(YearMonth.now().year) }
    var editorOpen by remember { mutableStateOf(false) }
    var editorDraft by remember { mutableStateOf<PayrollDraft?>(null) }
    var deleting by remember { mutableStateOf<Payslip?>(null) }
    var regionDialog by remember { mutableStateOf(false) }

    val payslips = remember(data.payslips, year) {
        data.payslips
            .filter { LocalDate.ofEpochDay(it.dateEpochDay).year == year }
            .sortedByDescending { it.dateEpochDay }
    }
    val gross = payslips.sumOf { it.grossCents }
    val irpf = payslips.sumOf { it.irpfCents }
    val socialSecurity = payslips.sumOf { it.socialSecurityCents }
    val otherDeductions = payslips.sumOf { it.otherDeductionsCents }
    val net = payslips.sumOf { it.netCents }
    val estimate = remember(gross, socialSecurity, irpf, data.settings.irpfRegion) {
        IrpfMath.estimate(
            grossCents = gross,
            socialSecurityCents = socialSecurity,
            withheldCents = irpf,
            region = data.settings.irpfRegion,
        )
    }
    val regionLabel = regionLabel(data.settings.irpfRegion)

    if (editorOpen) {
        PayrollEditor(
            initial = editorDraft,
            data = data,
            currency = currency,
            onBack = { editorOpen = false },
            onSave = { draft ->
                onSave(draft)
                editorOpen = false
            },
            onDelete = editorDraft?.id?.let { id ->
                {
                    editorOpen = false
                    deleting = payslips.firstOrNull { it.id == id }
                }
            },
        )
        return
    }

    BackHandler(enabled = true) { onClose() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.payroll_overline))
                SoftIconButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.payroll_add),
                    onClick = {
                        editorDraft = PayrollDraft(
                            date = if (year == YearMonth.now().year) LocalDate.now() else LocalDate.of(year, 1, 1),
                            accountId = data.accounts.firstOrNull()?.id.orEmpty(),
                        )
                        editorOpen = true
                    },
                    size = 38.dp,
                    container = colors.primarySoft,
                    tint = colors.primary,
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.payroll_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.payroll_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SoftIconButton(
                    icon = Icons.Rounded.ChevronLeft,
                    contentDescription = stringResource(R.string.payroll_year_previous),
                    onClick = { year -= 1 },
                    size = 34.dp,
                )
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                SoftIconButton(
                    icon = Icons.Rounded.ChevronRight,
                    contentDescription = stringResource(R.string.payroll_year_next),
                    onClick = { year += 1 },
                    size = 34.dp,
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            BudCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Overline(stringResource(R.string.payroll_annual_summary, year))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = formatMoney(gross, currency),
                            style = BudTheme.type.moneyLarge,
                            color = colors.ink,
                        )
                        Text(
                            text = stringResource(R.string.payroll_gross),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                    Pill(
                        text = pluralCount(payslips.size),
                    )
                }
                Spacer(Modifier.height(14.dp))
                PayrollMetricRow(
                    label = stringResource(R.string.payroll_irpf_withheld),
                    value = formatMoney(irpf, currency),
                    color = colors.expense,
                )
                Spacer(Modifier.height(6.dp))
                PayrollMetricRow(
                    label = stringResource(R.string.payroll_social_security),
                    value = formatMoney(socialSecurity, currency),
                    color = colors.inkMuted,
                )
                if (otherDeductions > 0L) {
                    Spacer(Modifier.height(6.dp))
                    PayrollMetricRow(
                        label = stringResource(R.string.payroll_other_deductions),
                        value = formatMoney(otherDeductions, currency),
                        color = colors.inkMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
                Spacer(Modifier.height(10.dp))
                PayrollMetricRow(
                    label = stringResource(R.string.payroll_net),
                    value = formatMoney(net, currency),
                    color = colors.ink,
                    bold = true,
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            BudCard(color = colors.primarySoft, border = false) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Overline(
                        stringResource(R.string.payroll_estimate_title),
                        color = colors.primary,
                    )
                    Text(
                        text = regionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { regionDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.payroll_estimate_region, regionLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                if (estimate.estimatedTaxCents == null) {
                    Text(
                        text = stringResource(R.string.payroll_foral_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                } else {
                    Text(
                        text = formatMoney(estimate.estimatedTaxCents, currency),
                        style = BudTheme.type.moneyLarge,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    val difference = estimate.differenceCents ?: 0L
                    val toPay = difference >= 0L
                    Text(
                        text = stringResource(
                            if (toPay) R.string.payroll_to_pay else R.string.payroll_to_refund,
                        ) + " · " + formatMoney(kotlin.math.abs(difference), currency),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (toPay) colors.expense else colors.primary,
                    )
                    estimate.effectiveRate?.let { rate ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.payroll_effective_rate) + " · " +
                                percentText(rate, decimals = 2),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.payroll_estimate_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(22.dp))
            Overline(stringResource(R.string.payroll_title))
            Spacer(Modifier.height(8.dp))
        }

        if (payslips.isEmpty()) {
            item {
                BudCard {
                    Text(
                        text = stringResource(R.string.payroll_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.payroll_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
        } else {
            items(payslips, key = { it.id }) { payslip ->
                PayslipRow(
                    payslip = payslip,
                    currency = currency,
                    onClick = {
                        editorDraft = payslip.toDraft()
                        editorOpen = true
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (regionDialog) {
        RegionDialog(
            selected = data.settings.irpfRegion,
            onDismiss = { regionDialog = false },
            onSelect = {
                onSetRegion(it)
                regionDialog = false
            },
        )
    }

    deleting?.let { payslip ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.payroll_delete_title), color = colors.ink) },
            text = {
                Text(
                    stringResource(R.string.payroll_delete_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(payslip.id)
                        deleting = null
                    },
                ) { Text(stringResource(R.string.action_delete), color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                }
            },
        )
    }
}

@Composable
private fun pluralCount(count: Int): String =
    androidx.compose.ui.res.pluralStringResource(R.plurals.payroll_count, count, count)

@Composable
private fun PayrollMetricRow(
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false,
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
            text = value,
            style = if (bold) BudTheme.type.moneySmall else MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun PayslipRow(
    payslip: Payslip,
    currency: Currency,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    BudCard(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = payslip.employer.ifBlank {
                        stringResource(R.string.payroll_default_note)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatShortDate(LocalDate.ofEpochDay(payslip.dateEpochDay)) +
                        " · " + payrollKindLabel(payslip.kind),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(payslip.netCents, currency),
                    style = BudTheme.type.moneySmall,
                    color = colors.income,
                )
                Text(
                    text = stringResource(R.string.payroll_gross) + " " +
                        formatMoney(payslip.grossCents, currency),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.inkMuted,
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun PayrollEditor(
    initial: PayrollDraft?,
    data: AppData,
    currency: Currency,
    onBack: () -> Unit,
    onSave: (PayrollDraft) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val colors = BudTheme.colors
    var employer by remember { mutableStateOf(initial?.employer.orEmpty()) }
    var kind by remember { mutableStateOf(initial?.kind ?: PayrollKind.MONTHLY) }
    var date by remember { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var gross by remember {
        mutableStateOf(
            initial?.grossCents?.takeIf { it > 0 }?.let(::centsToInput) ?: "",
        )
    }
    var irpf by remember {
        mutableStateOf(
            initial?.irpfCents?.takeIf { it > 0 }?.let(::centsToInput) ?: "",
        )
    }
    var socialSecurity by remember {
        mutableStateOf(
            initial?.socialSecurityCents?.takeIf { it > 0 }?.let(::centsToInput) ?: "",
        )
    }
    var otherDeductions by remember {
        mutableStateOf(
            initial?.otherDeductionsCents?.takeIf { it > 0 }?.let(::centsToInput) ?: "",
        )
    }
    var accountId by remember {
        mutableStateOf(initial?.accountId?.takeIf { it.isNotBlank() } ?: data.accounts.firstOrNull()?.id.orEmpty())
    }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val grossCents = parseMoneyInput(gross)
    val irpfCents = parseMoneyInput(irpf)
    val ssCents = parseMoneyInput(socialSecurity)
    val otherCents = parseMoneyInput(otherDeductions)
    val netCents = grossCents - irpfCents - ssCents - otherCents
    val valid = grossCents > 0L && accountId.isNotBlank() && netCents >= 0L
    val errorText = when {
        grossCents <= 0L -> stringResource(R.string.payroll_error_gross)
        netCents < 0L -> stringResource(R.string.payroll_error_net)
        accountId.isBlank() -> stringResource(R.string.payroll_error_account)
        else -> null
    }
    val irpfPercent = if (grossCents > 0L) irpfCents.toFloat() / grossCents else null

    BackHandler(enabled = true) { onBack() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline(
                    stringResource(
                        if (initial?.id == null) R.string.payroll_editor_new else R.string.payroll_editor_edit,
                    ),
                )
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(
                    if (initial?.id == null) {
                        R.string.payroll_editor_new_title
                    } else {
                        R.string.payroll_editor_edit_title
                    },
                ),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(18.dp))
        }

        item {
            BudCard {
                OutlinedTextField(
                    value = employer,
                    onValueChange = { employer = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.payroll_employer)) },
                    placeholder = { Text(stringResource(R.string.payroll_employer_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.payroll_kind),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PayrollKind.entries) { option ->
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
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        ) {
                            Text(
                                text = payrollKindLabel(option),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (kind == option) colors.primary else colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
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
                        text = formatShortDate(date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(14.dp))
                PayrollAmountField(
                    value = gross,
                    onValueChange = { gross = filterAmountInput(it) },
                    label = stringResource(R.string.payroll_amount_gross),
                    currency = currency,
                )
                Spacer(Modifier.height(10.dp))
                PayrollAmountField(
                    value = irpf,
                    onValueChange = { irpf = filterAmountInput(it) },
                    label = stringResource(R.string.payroll_amount_irpf),
                    currency = currency,
                    hint = irpfPercent?.let {
                        stringResource(R.string.payroll_irpf_percent, percentText(it, decimals = 2))
                    },
                )
                Spacer(Modifier.height(10.dp))
                PayrollAmountField(
                    value = socialSecurity,
                    onValueChange = { socialSecurity = filterAmountInput(it) },
                    label = stringResource(R.string.payroll_amount_ss),
                    currency = currency,
                )
                Spacer(Modifier.height(10.dp))
                PayrollAmountField(
                    value = otherDeductions,
                    onValueChange = { otherDeductions = filterAmountInput(it) },
                    label = stringResource(R.string.payroll_amount_other),
                    currency = currency,
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.primarySoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.payroll_amount_net),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ink,
                        )
                        Text(
                            text = formatMoney(netCents, currency),
                            style = BudTheme.type.moneyMedium,
                            color = if (netCents < 0) colors.expense else colors.ink,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.payroll_account),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(data.accounts, key = { it.id }) { account ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (accountId == account.id) colors.primarySoft else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (accountId == account.id) colors.primary else colors.line,
                                    CircleShape,
                                )
                                .clickable { accountId = account.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = account.displayName(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (accountId == account.id) colors.primary else colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.payroll_note)) },
                    placeholder = { Text(stringResource(R.string.payroll_note_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.expense,
                    )
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = stringResource(R.string.payroll_save),
                    onClick = {
                        onSave(
                            PayrollDraft(
                                id = initial?.id,
                                date = date,
                                employer = employer,
                                kind = kind,
                                grossCents = grossCents,
                                irpfCents = irpfCents,
                                socialSecurityCents = ssCents,
                                otherDeductionsCents = otherCents,
                                accountId = accountId,
                                note = note,
                            ),
                        )
                    },
                    enabled = valid,
                )
                if (onDelete != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onDelete)
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
                            text = stringResource(R.string.payroll_delete),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.expense,
                        )
                    }
                }
            }
        }
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
                ) { Text(stringResource(R.string.action_done), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                }
            },
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

@Composable
private fun PayrollAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    currency: Currency,
    hint: String? = null,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text("0") },
            suffix = { Text(currency.symbol, color = BudTheme.colors.inkMuted) },
            singleLine = true,
            textStyle = TextStyle(
                color = BudTheme.colors.ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = BudTheme.colors.inkMuted,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
        }
    }
}

@Composable
private fun RegionDialog(
    selected: IrpfRegion,
    onDismiss: () -> Unit,
    onSelect: (IrpfRegion) -> Unit,
) {
    val colors = BudTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(stringResource(R.string.payroll_region_title), color = colors.ink) },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp)) {
                items(IrpfRegion.entries) { region ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(region) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = regionLabel(region),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        if (region == selected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = colors.primary)
            }
        },
    )
}

@Composable
private fun payrollKindLabel(kind: PayrollKind): String = stringResource(
    when (kind) {
        PayrollKind.MONTHLY -> R.string.payroll_kind_monthly
        PayrollKind.EXTRA -> R.string.payroll_kind_extra
        PayrollKind.OTHER -> R.string.payroll_kind_other
    },
)

@Composable
private fun regionLabel(region: IrpfRegion): String = stringResource(
    when (region) {
        IrpfRegion.ANDALUCIA -> R.string.payroll_region_andalucia
        IrpfRegion.ARAGON -> R.string.payroll_region_aragon
        IrpfRegion.ASTURIAS -> R.string.payroll_region_asturias
        IrpfRegion.BALEARES -> R.string.payroll_region_baleares
        IrpfRegion.CANARIAS -> R.string.payroll_region_canarias
        IrpfRegion.CANTABRIA -> R.string.payroll_region_cantabria
        IrpfRegion.CASTILLA_LA_MANCHA -> R.string.payroll_region_castilla_la_mancha
        IrpfRegion.CASTILLA_Y_LEON -> R.string.payroll_region_castilla_y_leon
        IrpfRegion.CATALUNA -> R.string.payroll_region_cataluna
        IrpfRegion.MADRID -> R.string.payroll_region_madrid
        IrpfRegion.EXTREMADURA -> R.string.payroll_region_extremadura
        IrpfRegion.GALICIA -> R.string.payroll_region_galicia
        IrpfRegion.LA_RIOJA -> R.string.payroll_region_la_rioja
        IrpfRegion.MURCIA -> R.string.payroll_region_murcia
        IrpfRegion.COMUNIDAD_VALENCIANA -> R.string.payroll_region_valencia
        IrpfRegion.CEUTA -> R.string.payroll_region_ceuta
        IrpfRegion.MELILLA -> R.string.payroll_region_melilla
        IrpfRegion.PAIS_VASCO -> R.string.payroll_region_pais_vasco
        IrpfRegion.NAVARRA -> R.string.payroll_region_navarra
    },
)

private fun Payslip.toDraft(): PayrollDraft = PayrollDraft(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    employer = employer,
    kind = kind,
    grossCents = grossCents,
    irpfCents = irpfCents,
    socialSecurityCents = socialSecurityCents,
    otherDeductionsCents = otherDeductionsCents,
    accountId = accountId,
    note = note,
)
