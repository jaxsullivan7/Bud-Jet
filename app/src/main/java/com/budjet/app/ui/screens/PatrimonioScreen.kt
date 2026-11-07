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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.DebtPlanMath
import com.budjet.app.data.DebtStrategy
import com.budjet.app.data.Finance
import com.budjet.app.data.Investment
import com.budjet.app.data.InvestmentMath
import com.budjet.app.data.Liability
import com.budjet.app.data.LiabilityKind
import com.budjet.app.data.NetWorth
import com.budjet.app.ui.LiabilityDraft
import com.budjet.app.ui.LiabilityPaymentDraft
import com.budjet.app.ui.TransferDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.Sparkline
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.components.displayName
import com.budjet.app.ui.components.localizedLabel
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.centsToInput
import com.budjet.app.util.filterAmountInput
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatMonth
import com.budjet.app.util.formatMonthShort
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.parseMoneyInput
import com.budjet.app.util.parsePercentInput
import com.budjet.app.util.rateToInput
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@Composable
fun PatrimonioScreen(
    data: AppData,
    onClose: () -> Unit,
    onAddTransfer: (TransferDraft) -> Unit,
    onSaveLiability: (LiabilityDraft) -> Unit,
    onDeleteLiability: (String) -> Unit,
    onPayLiability: (LiabilityPaymentDraft) -> Unit,
    onOpenPortfolio: () -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val today = remember { LocalDate.now() }
    val netWorth = remember(data, today) { Finance.netWorth(data, today) }
    val series = remember(data, today) { Finance.netWorthSeries(data, 6, today) }
    var transferOpen by remember { mutableStateOf(false) }
    var liabilityDraft by remember { mutableStateOf<LiabilityDraft?>(null) }
    var deleteTarget by remember { mutableStateOf<Liability?>(null) }
    var paymentTarget by remember { mutableStateOf<Liability?>(null) }
    var debtPlanOpen by remember { mutableStateOf(false) }

    if (transferOpen) {
        TransferFormScreen(
            data = data,
            currency = currency,
            today = today,
            onBack = { transferOpen = false },
            onSave = {
                onAddTransfer(it)
                transferOpen = false
            },
        )
        return
    }

    paymentTarget?.let { liability ->
        LiabilityPaymentScreen(
            liability = liability,
            data = data,
            currency = currency,
            today = today,
            onBack = { paymentTarget = null },
            onSave = {
                onPayLiability(it)
                paymentTarget = null
            },
        )
        return
    }

    if (debtPlanOpen) {
        DebtPlanScreen(
            data = data,
            currency = currency,
            today = today,
            onBack = { debtPlanOpen = false },
        )
        return
    }

    BackHandler(enabled = true) { onClose() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("patrimonio-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 44.dp),
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
                Overline(stringResource(R.string.patrimonio_overline))
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.patrimonio_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.patrimonio_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            NetWorthHero(netWorth = netWorth, currency = currency)
        }

        item {
            Spacer(Modifier.height(12.dp))
            BudCard {
                Overline(stringResource(R.string.patrimonio_evolution))
                Spacer(Modifier.height(10.dp))
                Sparkline(
                    values = series.map { it.totalCents.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    series.forEach { point ->
                        Text(
                            text = formatMonthShort(point.month).take(3),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = colors.inkMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
                HorizontalDividerLine()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.patrimonio_current),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Text(
                        text = formatMoney(netWorth.totalCents, currency),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.ink,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(
                    text = stringResource(R.string.action_transfer),
                    onClick = { transferOpen = true },
                    icon = Icons.Rounded.SwapHoriz,
                    modifier = Modifier.weight(1f),
                )
                BudCard(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenPortfolio,
                    padding = 14.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.label_portfolio),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.ink,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionTitle(stringResource(R.string.label_accounts), formatMoney(netWorth.cashCents, currency))
            Spacer(Modifier.height(8.dp))
            BudCard(padding = 0.dp) {
                data.accounts.forEachIndexed { index, account ->
                    AccountBalanceRow(
                        account = account,
                        balance = Finance.accountBalance(data, account.id, today),
                        currency = currency,
                    )
                    if (index != data.accounts.lastIndex) SettingsLine()
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.patrimonio_accounts_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionTitle(stringResource(R.string.label_investments), formatMoney(netWorth.investmentsCents, currency))
            Spacer(Modifier.height(8.dp))
            BudCard(onClick = onOpenPortfolio) {
                if (data.investments.isEmpty()) {
                    Text(
                        text = stringResource(R.string.patrimonio_investments_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                } else {
                    data.investments.take(4).forEachIndexed { index, investment ->
                        val summary = InvestmentMath.summary(data.entriesFor(investment.id), today)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor(investment.kind.colorHex, colors.isDark).copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    categoryIcon(investment.kind.icon),
                                    contentDescription = null,
                                    tint = categoryColor(investment.kind.colorHex, colors.isDark),
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = investment.displayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.ink,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = formatMoney(summary.valueCents, currency),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.ink,
                            )
                        }
                        if (index != minOf(data.investments.size, 4) - 1) HorizontalDividerLine()
                    }
                    if (data.investments.size > 4) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.patrimonio_more_products,
                                data.investments.size - 4,
                                data.investments.size - 4,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.label_debts),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatMoney(netWorth.liabilitiesCents, currency),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.width(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { debtPlanOpen = true }
                            .testTag("debt-plan-button")
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_plan), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { liabilityDraft = LiabilityDraft() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.action_add), style = MaterialTheme.typography.labelMedium, color = colors.primary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (data.liabilities.isEmpty()) {
                BudCard {
                    Text(
                        text = stringResource(R.string.patrimonio_no_debts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            } else {
                BudCard(padding = 0.dp) {
                    data.liabilities.forEachIndexed { index, liability ->
                        LiabilityRow(
                            liability = liability,
                            paidCents = data.paymentsFor(liability.id).sumOf { it.amountCents },
                            currency = currency,
                            onEdit = {
                                liabilityDraft = LiabilityDraft(
                                    id = liability.id,
                                    name = liability.name,
                                    kind = liability.kind,
                                    balanceCents = liability.balanceCents,
                                    annualRatePercent = liability.annualRatePercent,
                                    minimumPaymentCents = liability.minimumPaymentCents,
                                )
                            },
                            onDelete = { deleteTarget = liability },
                            onPay = { paymentTarget = liability },
                        )
                        if (index != data.liabilities.lastIndex) SettingsLine()
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.patrimonio_formula_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }

    liabilityDraft?.let { draft ->
        LiabilityDialog(
            initial = draft,
            currency = currency,
            onDismiss = { liabilityDraft = null },
            onSave = {
                onSaveLiability(it)
                liabilityDraft = null
            },
        )
    }

    deleteTarget?.let { liability ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.patrimonio_delete_title), color = colors.ink) },
            text = {
                Text(
                    text = stringResource(R.string.patrimonio_delete_body, liability.displayName()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLiability(liability.id)
                        deleteTarget = null
                    },
                ) { Text(stringResource(R.string.action_delete), color = colors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
            },
        )
    }
}

@Composable
private fun NetWorthHero(netWorth: NetWorth, currency: Currency) {
    val colors = BudTheme.colors
    BudCard(color = colors.heroStart, border = false) {
        Overline(stringResource(R.string.patrimonio_net_worth), color = colors.heroMuted)
        Spacer(Modifier.height(8.dp))
        Text(
            text = formatMoney(netWorth.totalCents, currency),
            style = BudTheme.type.moneyHero.copy(fontSize = 34.sp, lineHeight = 40.sp),
            color = colors.heroText,
            maxLines = 1,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            HeroMetric(stringResource(R.string.label_cash), netWorth.cashCents, currency)
            HeroMetric(stringResource(R.string.label_investments), netWorth.investmentsCents, currency)
            HeroMetric(stringResource(R.string.label_debts), netWorth.liabilitiesCents, currency)
        }
    }
}

@Composable
private fun HeroMetric(label: String, cents: Long, currency: Currency) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = BudTheme.colors.heroMuted)
        Text(
            text = formatMoney(cents, currency),
            style = BudTheme.type.moneySmall,
            color = BudTheme.colors.heroText,
        )
    }
}

@Composable
private fun AccountBalanceRow(account: Account, balance: Long, currency: Currency) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                categoryIcon(account.icon),
                contentDescription = null,
                tint = categoryColor(account.colorHex, colors.isDark),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = account.displayName(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatMoney(balance, currency),
            style = BudTheme.type.moneySmall,
            color = colors.ink,
        )
    }
}

@Composable
private fun LiabilityRow(
    liability: Liability,
    paidCents: Long,
    currency: Currency,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPay: () -> Unit,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(categoryColor(liability.kind.colorHex, colors.isDark).copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                categoryIcon(liability.kind.icon),
                contentDescription = null,
                tint = categoryColor(liability.kind.colorHex, colors.isDark),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(liability.displayName(), style = MaterialTheme.typography.bodyMedium, color = colors.ink)
            Text(
                text = if (paidCents > 0L) {
                    stringResource(
                        R.string.liability_paid,
                        liability.kind.localizedLabel(),
                        formatMoney(paidCents, currency),
                    )
                } else {
                    liability.kind.localizedLabel()
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
        Text(
            text = formatMoney(liability.balanceCents, currency),
            style = BudTheme.type.moneySmall,
            color = colors.expense,
        )
        Spacer(Modifier.width(6.dp))
        if (liability.balanceCents > 0L) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onPay)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.SwapHoriz,
                    contentDescription = stringResource(R.string.action_pay_debt),
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(3.dp))
                Text(stringResource(R.string.action_pay), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
        }
        SoftIconButton(
            icon = Icons.Rounded.DeleteOutline,
            contentDescription = stringResource(R.string.action_delete_debt),
            onClick = onDelete,
            size = 28.dp,
        )
    }
}

@Composable
private fun TransferFormScreen(
    data: AppData,
    currency: Currency,
    today: LocalDate,
    onBack: () -> Unit,
    onSave: (TransferDraft) -> Unit,
) {
    val colors = BudTheme.colors
    var fromType by remember { mutableIntStateOf(if (data.accounts.isNotEmpty()) 0 else 1) }
    var toType by remember { mutableIntStateOf(if (data.investments.isNotEmpty()) 1 else 0) }
    var fromAccount by remember { mutableStateOf(data.accounts.firstOrNull()?.id) }
    var fromInvestment by remember { mutableStateOf<String?>(null) }
    var toAccount by remember { mutableStateOf<String?>(null) }
    var toInvestment by remember { mutableStateOf(data.investments.firstOrNull()?.id) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var showDate by remember { mutableStateOf(false) }

    val fromId = if (fromType == 0) fromAccount else fromInvestment
    val toId = if (toType == 0) toAccount else toInvestment
    val parsed = parseMoneyInput(amount)
    val available = when {
        fromType == 0 && fromAccount != null -> Finance.accountBalanceAt(data, fromAccount!!, date)
        fromType == 1 && fromInvestment != null ->
            InvestmentMath.summary(data.entriesFor(fromInvestment!!), date).valueCents
        else -> 0L
    }
    val distinct = fromId != null && toId != null && !(fromType == toType && fromId == toId)
    val valid = distinct && !(fromType == 1 && toType == 1) && parsed > 0L && parsed <= available

    BackHandler(enabled = true) { onBack() }

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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.transfer_overline))
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.transfer_title), style = MaterialTheme.typography.headlineLarge, color = colors.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.transfer_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(18.dp))
        }

        item {
            BudCard {
                Text(stringResource(R.string.label_from), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                TypeTabs(
                    first = stringResource(R.string.label_account),
                    second = stringResource(R.string.label_investment),
                    selected = fromType,
                    onSelect = { fromType = it; fromInvestment = data.investments.firstOrNull()?.id },
                )
                Spacer(Modifier.height(6.dp))
                if (fromType == 0) {
                    ChoiceList(
                        items = data.accounts.map { it.id to it.displayName() },
                        selected = fromAccount,
                        onSelect = { fromAccount = it },
                    )
                } else {
                    ChoiceList(
                        items = data.investments.map { it.id to it.displayName() },
                        selected = fromInvestment,
                        onSelect = { fromInvestment = it },
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.label_to), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                TypeTabs(
                    first = stringResource(R.string.label_account),
                    second = stringResource(R.string.label_investment),
                    selected = toType,
                    onSelect = { toType = it; toInvestment = data.investments.firstOrNull()?.id },
                )
                Spacer(Modifier.height(6.dp))
                if (toType == 0) {
                    ChoiceList(
                        items = data.accounts.map { it.id to it.displayName() },
                        selected = toAccount,
                        onSelect = { toAccount = it },
                    )
                } else {
                    ChoiceList(
                        items = data.investments.map { it.id to it.displayName() },
                        selected = toInvestment,
                        onSelect = { toInvestment = it },
                    )
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = filterAmountInput(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_amount)) },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.transfer_available, formatMoney(available, currency)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (parsed > available) colors.expense else colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceMuted.copy(alpha = 0.5f))
                        .clickable { showDate = true }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (date == today) {
                            stringResource(R.string.today_prefix, formatShortDate(date))
                        } else {
                            formatShortDate(date)
                        },
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
                    label = { Text(stringResource(R.string.label_note_optional)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = stringResource(R.string.action_transfer),
                    onClick = {
                        onSave(
                            TransferDraft(
                                fromAccountId = if (fromType == 0) fromAccount else null,
                                fromInvestmentId = if (fromType == 1) fromInvestment else null,
                                toAccountId = if (toType == 0) toAccount else null,
                                toInvestmentId = if (toType == 1) toInvestment else null,
                                amountCents = parsed,
                                date = date,
                                note = note,
                            ),
                        )
                    },
                    enabled = valid,
                    icon = Icons.Rounded.SwapHoriz,
                )
            }
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDate = false
                    },
                ) { Text(stringResource(R.string.action_done), color = colors.primary) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) } },
        ) { DatePicker(state = state, showModeToggle = false) }
    }
}

@Composable
private fun DebtPlanScreen(
    data: AppData,
    currency: Currency,
    today: LocalDate,
    onBack: () -> Unit,
) {
    val colors = BudTheme.colors
    val debts = remember(data.liabilities) { DebtPlanMath.fromData(data) }
    val minimums = remember(debts) { DebtPlanMath.minimumTotal(debts) }
    val defaultBudget = if (minimums > 0L) minimums + 5_000L else 10_000L
    var budget by remember { mutableStateOf(centsToInput(defaultBudget)) }
    var strategy by remember { mutableStateOf(DebtStrategy.SNOWBALL) }
    val parsedBudget = parseMoneyInput(budget)
    val result = remember(debts, parsedBudget, strategy, today) {
        DebtPlanMath.simulate(debts, parsedBudget, strategy, today)
    }
    val snowball = remember(debts, parsedBudget, today) {
        DebtPlanMath.simulate(debts, parsedBudget, DebtStrategy.SNOWBALL, today)
    }
    val avalanche = remember(debts, parsedBudget, today) {
        DebtPlanMath.simulate(debts, parsedBudget, DebtStrategy.AVALANCHE, today)
    }

    BackHandler(enabled = true) { onBack() }

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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.debt_plan_overline))
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.debt_plan_title), style = MaterialTheme.typography.headlineLarge, color = colors.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.debt_plan_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(18.dp))
        }

        if (debts.isEmpty()) {
            item {
                BudCard {
                    Text(
                        text = stringResource(R.string.debt_plan_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
            return@LazyColumn
        }

        item {
            BudCard {
                Text(stringResource(R.string.debt_plan_monthly_total), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = filterAmountInput(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.debt_plan_monthly_hint)) },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                    R.string.debt_plan_minimums,
                    formatMoney(minimums, currency),
                ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.label_strategy), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DebtStrategy.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (strategy == option) colors.primarySoft else Color.Transparent)
                                .border(1.dp, if (strategy == option) colors.primary else colors.line, RoundedCornerShape(12.dp))
                                .clickable { strategy = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = option.localizedLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (strategy == option) colors.primary else colors.inkMuted,
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            BudCard(modifier = Modifier.fillMaxWidth(), color = colors.heroStart, border = false) {
                Overline(stringResource(R.string.debt_plan_free_date), color = colors.heroMuted)
                Spacer(Modifier.height(8.dp))
                if (result.feasible && result.months > 0) {
                    Text(
                        text = stringResource(R.string.debt_plan_months, result.months),
                        style = BudTheme.type.moneyHero.copy(fontSize = 34.sp, lineHeight = 40.sp),
                        color = colors.heroText,
                    )
                    Text(
                        text = result.debtFreeDate?.let { formatMonth(YearMonth.from(it)) } ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.heroMuted,
                    )
                } else if (result.feasible) {
                    Text(stringResource(R.string.debt_plan_no_debts), style = BudTheme.type.moneyHero.copy(fontSize = 34.sp), color = colors.heroText)
                } else {
                    Text(
                        text = when {
                            parsedBudget <= 0L -> stringResource(R.string.debt_plan_needs_payment)
                            parsedBudget < minimums -> stringResource(
                                R.string.debt_plan_below_minimums,
                                formatMoney(minimums, currency),
                            )
                            else -> stringResource(R.string.debt_plan_not_amortizing)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.heroText,
                    )
                }
                if (result.feasible) {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        HeroMetric(stringResource(R.string.debt_plan_interest), result.totalInterestCents, currency)
                        HeroMetric(stringResource(R.string.debt_plan_total_paid), result.totalPaidCents, currency)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            BudCard {
                Overline(stringResource(R.string.debt_plan_comparison))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.debt_plan_comparison_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                ComparisonRow(stringResource(R.string.debt_strategy_snowball), snowball, strategy == DebtStrategy.SNOWBALL, currency)
                Spacer(Modifier.height(8.dp))
                ComparisonRow(stringResource(R.string.debt_strategy_avalanche), avalanche, strategy == DebtStrategy.AVALANCHE, currency)
                if (snowball.months == avalanche.months &&
                    kotlin.math.abs(snowball.totalInterestCents - avalanche.totalInterestCents) < 1L
                ) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.debt_plan_same_end),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            Overline(stringResource(R.string.debt_plan_order))
            Spacer(Modifier.height(8.dp))
            BudCard(padding = 0.dp) {
                result.items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.primarySoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.primary,
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                data.liability(item.debtId)?.displayName() ?: item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.ink,
                            )
                            Text(
                                text = if (item.payoffMonth > 0) {
                                    stringResource(R.string.debt_plan_free_month, item.payoffMonth)
                                } else {
                                    stringResource(R.string.debt_plan_no_debt)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted,
                            )
                        }
                        Text(
                            text = formatMoney(item.interestPaidCents, currency),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                    if (index != result.items.lastIndex) SettingsLine()
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.debt_plan_interest_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    result: com.budjet.app.data.DebtPlanResult,
    selected: Boolean,
    currency: Currency,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primarySoft else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.ink)
            Text(
                text = if (result.feasible) {
                    stringResource(R.string.debt_plan_months, result.months)
                } else {
                    stringResource(R.string.debt_plan_no_amortize)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
        Text(
            text = if (result.feasible) formatMoney(result.totalInterestCents, currency) else "—",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.inkMuted,
        )
    }
}

@Composable
private fun LiabilityPaymentScreen(
    liability: Liability,
    data: AppData,
    currency: Currency,
    today: LocalDate,
    onBack: () -> Unit,
    onSave: (LiabilityPaymentDraft) -> Unit,
) {
    val colors = BudTheme.colors
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var showDate by remember { mutableStateOf(false) }
    var accountId by remember { mutableStateOf(data.accounts.firstOrNull()?.id.orEmpty()) }
    val parsed = parseMoneyInput(amount)
    val available = if (accountId.isNotBlank()) Finance.accountBalanceAt(data, accountId, date) else 0L
    val valid = parsed > 0L && parsed <= liability.balanceCents && parsed <= available

    BackHandler(enabled = true) { onBack() }

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
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.debt_payment_overline))
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.debt_payment_title), style = MaterialTheme.typography.headlineLarge, color = colors.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.debt_payment_subtitle, liability.displayName()),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(18.dp))
        }

        item {
            BudCard {
                Text(
                    text = stringResource(
                    R.string.debt_pending_balance,
                    formatMoney(liability.balanceCents, currency),
                ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = filterAmountInput(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.debt_payment_amount)) },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.debt_from_account), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(data.accounts, key = { it.id }) { account ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (accountId == account.id) colors.primarySoft else Color.Transparent)
                                .border(1.dp, if (accountId == account.id) colors.primary else colors.line, CircleShape)
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
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.transfer_available, formatMoney(available, currency)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (parsed > available) colors.expense else colors.inkMuted,
                )
                if (parsed > liability.balanceCents) {
                    Text(
                        text = stringResource(R.string.debt_payment_exceeds_balance),
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
                        .clickable { showDate = true }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (date == today) {
                            stringResource(R.string.today_prefix, formatShortDate(date))
                        } else {
                            formatShortDate(date)
                        },
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
                    label = { Text(stringResource(R.string.label_note_optional)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = stringResource(R.string.action_register_payment),
                    onClick = {
                        onSave(
                            LiabilityPaymentDraft(
                                liabilityId = liability.id,
                                accountId = accountId,
                                amountCents = parsed,
                                date = date,
                                note = note,
                            ),
                        )
                    },
                    enabled = valid,
                    icon = Icons.Rounded.SwapHoriz,
                )
            }
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDate = false
                    },
                ) { Text(stringResource(R.string.action_done), color = colors.primary) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.action_close_calendar), color = colors.inkMuted) } },
        ) { DatePicker(state = state, showModeToggle = false) }
    }
}

@Composable
private fun LiabilityDialog(
    initial: LiabilityDraft,
    currency: Currency,
    onDismiss: () -> Unit,
    onSave: (LiabilityDraft) -> Unit,
) {
    val colors = BudTheme.colors
    var name by remember { mutableStateOf(initial.name) }
    var kind by remember { mutableStateOf(initial.kind) }
    var amount by remember { mutableStateOf(if (initial.balanceCents > 0L) centsToInput(initial.balanceCents) else "") }
    var rate by remember {
        mutableStateOf(rateToInput(initial.annualRatePercent))
    }
    var minimum by remember {
        mutableStateOf(if (initial.minimumPaymentCents > 0L) centsToInput(initial.minimumPaymentCents) else "")
    }
    val parsed = parseMoneyInput(amount)
    val rateValue = parsePercentInput(rate)
    val minimumCents = parseMoneyInput(minimum)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(
                    stringResource(if (initial.id == null) R.string.debt_new else R.string.debt_edit),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        if (initial.id == null) R.string.debt_dialog_what else R.string.debt_dialog_adjust,
                    ),
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
                    label = { Text(stringResource(R.string.label_name)) },
                    placeholder = { Text(stringResource(R.string.debt_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.label_type), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LiabilityKind.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (kind == option) colors.primarySoft else Color.Transparent)
                                .border(1.dp, if (kind == option) colors.primary else colors.line, CircleShape)
                                .clickable { kind = option }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                categoryIcon(option.icon),
                                contentDescription = null,
                                tint = categoryColor(option.colorHex, colors.isDark),
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(option.localizedLabel(), style = MaterialTheme.typography.labelSmall, color = colors.ink)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = filterAmountInput(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.debt_balance)) },
                    placeholder = { Text("0") },
                    suffix = { Text(currency.symbol, color = colors.inkMuted) },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { rate = filterAmountInput(it, 6) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.debt_annual_rate)) },
                        suffix = { Text("%", color = colors.inkMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = minimum,
                        onValueChange = { minimum = filterAmountInput(it, 10) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.debt_minimum_payment)) },
                        suffix = { Text(currency.symbol, color = colors.inkMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.debt_dialog_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        LiabilityDraft(
                            id = initial.id,
                            name = name,
                            kind = kind,
                            balanceCents = parsed,
                            annualRatePercent = rateValue,
                            minimumPaymentCents = minimumCents,
                        ),
                    )
                },
                enabled = name.isNotBlank() && parsed >= 0L,
            ) {
                Text(
                    stringResource(if (initial.id == null) R.string.action_add else R.string.action_save),
                    color = colors.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
        },
    )
}

@Composable
private fun TypeTabs(first: String, second: String, selected: Int, onSelect: (Int) -> Unit) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceMuted.copy(alpha = 0.55f))
            .padding(3.dp),
    ) {
        listOf(first, second).forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected == index) colors.surface else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected == index) colors.ink else colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun ChoiceList(
    items: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    val colors = BudTheme.colors
    Column {
        items.take(4).forEach { (id, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(id) }
                    .padding(horizontal = 6.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(if (selected == id) colors.primary else colors.line),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = BudTheme.colors.ink)
        Text(trailing, style = MaterialTheme.typography.labelMedium, color = BudTheme.colors.inkMuted)
    }
}

@Composable
private fun SettingsLine() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = BudTheme.colors.line,
    )
}

@Composable
private fun HorizontalDividerLine() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = BudTheme.colors.line,
    )
}
