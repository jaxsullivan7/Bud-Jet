package com.budjet.app.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.SavingsGoal
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BrandWordmark
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.CashflowChart
import com.budjet.app.ui.components.CategoryBadge
import com.budjet.app.ui.components.InitialsAvatar
import com.budjet.app.ui.components.LegendDot
import com.budjet.app.ui.components.LinearProgressBar
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.Pill
import com.budjet.app.ui.components.RingProgress
import com.budjet.app.ui.components.SectionHeader
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.Sparkline
import com.budjet.app.ui.components.ThinDivider
import com.budjet.app.ui.components.TransactionRow
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMoney
import com.budjet.app.util.percentText
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun HomeScreen(
    data: AppData,
    month: YearMonth,
    onOpenSettings: () -> Unit,
    onSeeTransactions: () -> Unit,
    onSeeBudgets: () -> Unit,
    onSeeAnalysis: () -> Unit,
    onSeeGoals: () -> Unit,
    onOpenTransaction: (Transaction) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val today = remember { LocalDate.now() }
    val monthTx = remember(data.transactions, month) { Finance.transactionsOfMonth(data, month) }
    val postedTx = remember(data.transactions, month) { Finance.postedTransactionsOfMonth(data, month, today) }
    val futureCount = monthTx.size - postedTx.size
    val totals = remember(postedTx) { Finance.totals(postedTx) }
    val balance = remember(data) { Finance.totalBalance(data) }
    val previousMonth = month.minusMonths(1)
    val previousTotals = remember(data, month) {
        Finance.totals(Finance.transactionsOfMonth(data, previousMonth))
    }
    val previousBalance = remember(data, month) {
        Finance.balanceAt(data, previousMonth.atEndOfMonth())
    }
    val balanceChange = remember(balance, previousBalance) {
        if (previousBalance == 0L) null
        else (balance - previousBalance).toFloat() / previousBalance
    }
    val series = remember(data, month) { Finance.series(data, month, 6) }
    val balanceSeries = remember(data, month) {
        (5 downTo 0).map { back ->
            val target = month.minusMonths(back.toLong())
            val date = if (target == month) today else target.atEndOfMonth()
            Finance.balanceAt(data, date).toFloat()
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val datePattern = stringResource(R.string.home_date_pattern)
    val dayLine = remember(today, locale, datePattern) {
        today.format(DateTimeFormatter.ofPattern(datePattern, locale))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    val budget = Finance.overallBudget(data)
    val budgetSpent = remember(data, month) { totals.expense }
    val budgetProgress = budget?.let { Finance.budgetProgress(budgetSpent, it.limitCents) } ?: 0f
    val budgetRemaining = budget?.let { it.limitCents - budgetSpent } ?: 0L
    val dailyAllowance = budget?.let { Finance.dailyAllowance(it.limitCents - budgetSpent, today, month) } ?: 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 150.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandWordmark(fontSize = 21.sp, markSize = 30.dp)
                }
                SoftIconButton(
                    icon = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.action_settings),
                    onClick = onOpenSettings,
                    size = 38.dp,
                    container = Color.Transparent,
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_greeting, data.settings.name),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = dayLine,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            BalanceHero(
                balance = balance,
                change = balanceChange,
                series = balanceSeries,
                currency = currency,
                month = month,
            )
        }

        item {
            Spacer(Modifier.height(12.dp))
            MonthSummaryCard(
                income = totals.income,
                expense = totals.expense,
                previous = previousTotals,
                currency = currency,
            )
        }

        item {
            Spacer(Modifier.height(28.dp))
            SectionHeader(
                title = stringResource(R.string.section_cash_flow),
                trailing = stringResource(R.string.action_view_analysis),
                onTrailingClick = onSeeAnalysis,
            )
            Spacer(Modifier.height(12.dp))
            BudCard(onClick = onSeeAnalysis) {
                if (postedTx.isEmpty()) {
                    Overline(stringResource(R.string.overline_this_month))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.home_no_transactions_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (futureCount > 0) {
                            pluralStringResource(
                                R.plurals.home_scheduled_transactions,
                                futureCount,
                                futureCount,
                            )
                        } else {
                            stringResource(R.string.home_add_first_hint)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Overline(stringResource(R.string.overline_this_month))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(
                                    if (totals.balance >= 0) R.string.home_saved else R.string.home_overspent,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                            )
                        }
                        val positive = totals.balance >= 0
                        Pill(
                            text = (if (positive) "+" else "−") + formatMoney(abs(totals.balance), currency),
                            container = if (positive) colors.primarySoft else colors.expense.copy(alpha = 0.12f),
                            content = if (positive) colors.primary else colors.expense,
                            icon = if (positive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    CashflowChart(points = series, highlightMonth = month)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(colors.income, stringResource(R.string.legend_income))
                        Spacer(Modifier.width(16.dp))
                        LegendDot(colors.expense, stringResource(R.string.legend_expenses))
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (totals.income > 0) {
                                stringResource(
                                    R.string.home_savings_rate,
                                    percentText(totals.savingsRate),
                                )
                            } else {
                                stringResource(R.string.home_no_income)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkMuted,
                        )
                    }
                    if (futureCount > 0) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.home_scheduled_pending,
                                futureCount,
                                futureCount,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.warning,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = stringResource(R.string.section_budget),
                trailing = stringResource(
                    if (budget != null) R.string.action_adjust else R.string.action_create,
                ),
                onTrailingClick = onSeeBudgets,
            )
            Spacer(Modifier.height(12.dp))
            if (budget != null) {
                BudCard(onClick = onSeeBudgets) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RingProgress(
                            progress = budgetProgress,
                            color = if (budgetProgress >= 1f) colors.expense else colors.primary,
                            modifier = Modifier.size(66.dp),
                            strokeWidth = 5.dp,
                        ) {
                            Text(
                                text = percentText(budgetProgress.coerceAtMost(1f)),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.ink,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Overline(stringResource(R.string.overline_monthly_budget))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (budgetRemaining >= 0) {
                                    stringResource(
                                        R.string.home_budget_available,
                                        formatMoney(budgetRemaining, currency),
                                    )
                                } else {
                                    stringResource(
                                        R.string.home_budget_over,
                                        formatMoney(abs(budgetRemaining), currency),
                                    )
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = if (budgetRemaining < 0) colors.expense else colors.ink,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(
                                    R.string.home_budget_detail,
                                    formatMoney(budget.limitCents, currency),
                                    formatMoney(budgetSpent, currency),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.inkMuted,
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.inkMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    LinearProgressBar(
                        progress = budgetProgress,
                        color = if (budgetProgress >= 1f) colors.expense else colors.primary,
                        height = 5.dp,
                    )
                    Spacer(Modifier.height(9.dp))
                    Text(
                        text = if (budgetRemaining >= 0 && dailyAllowance > 0) {
                            stringResource(
                                R.string.home_budget_daily,
                                formatMoney(dailyAllowance, currency),
                            )
                        } else if (budgetRemaining < 0) {
                            stringResource(R.string.home_budget_exceeded)
                        } else {
                            stringResource(R.string.home_budget_few_days)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            } else {
                BudCard(onClick = onSeeBudgets) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.home_budget_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.ArrowOutward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_budget_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = stringResource(R.string.section_goals),
                trailing = if (data.savingsGoals.isNotEmpty()) {
                    stringResource(R.string.action_view_all)
                } else {
                    null
                },
                onTrailingClick = onSeeGoals,
            )
            Spacer(Modifier.height(12.dp))
            if (data.savingsGoals.isEmpty()) {
                BudCard(onClick = onSeeGoals) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.home_goals_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.ArrowOutward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_goals_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 20.dp),
                ) {
                    items(data.savingsGoals.take(4), key = { it.id }) { goal ->
                        HomeGoalCard(
                            goal = goal,
                            currency = currency,
                            onClick = onSeeGoals,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(
                title = stringResource(R.string.section_recent_transactions),
                trailing = stringResource(R.string.action_view_all),
                onTrailingClick = onSeeTransactions,
            )
        }

        val recent = data.transactions
            .filter { it.dateEpochDay <= today.toEpochDay() }
            .sortedByDescending { it.dateEpochDay }
            .take(4)
        if (recent.isEmpty()) {
            item {
                BudCard {
                    Text(
                        text = stringResource(R.string.home_no_movements_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted,
                    )
                }
            }
        } else {
            item {
                Spacer(Modifier.height(8.dp))
                BudCard(padding = 10.dp) {
                    recent.forEachIndexed { index, tx ->
                        TransactionRow(
                            transaction = tx,
                            category = data.category(tx.categoryId),
                            account = data.account(tx.accountId),
                            currency = currency,
                            onClick = { onOpenTransaction(tx) },
                            showDate = true,
                        )
                        if (index != recent.lastIndex) {
                            ThinDivider(Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }

        val upcoming = data.transactions
            .filter { it.dateEpochDay > today.toEpochDay() }
            .sortedBy { it.dateEpochDay }
        if (upcoming.isNotEmpty()) {
            item {
                Spacer(Modifier.height(26.dp))
                SectionHeader(
                    title = stringResource(R.string.section_upcoming),
                    trailing = pluralStringResource(
                        R.plurals.home_upcoming_count,
                        upcoming.size,
                        upcoming.size,
                    ),
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                BudCard(padding = 10.dp) {
                    upcoming.forEachIndexed { index, tx ->
                        TransactionRow(
                            transaction = tx,
                            category = data.category(tx.categoryId),
                            account = data.account(tx.accountId),
                            currency = currency,
                            onClick = { onOpenTransaction(tx) },
                        )
                        if (index != upcoming.lastIndex) {
                            ThinDivider(Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeGoalCard(
    goal: SavingsGoal,
    currency: Currency,
    onClick: () -> Unit,
) {
    val colors = BudTheme.colors
    val color = categoryColor(goal.colorHex, colors.isDark)
    val progress = Finance.goalProgress(goal)
    val remaining = Finance.goalRemaining(goal)
    BudCard(
        modifier = Modifier
            .width(224.dp)
            .height(158.dp),
        padding = 16.dp,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (colors.isDark) 0.2f else 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = categoryIcon(goal.icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = formatMoney(goal.savedCents, currency),
            style = BudTheme.type.moneyMedium,
            color = colors.ink,
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.home_goal_of, formatMoney(goal.targetCents, currency)),
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        LinearProgressBar(progress = progress, color = color, height = 5.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (remaining == 0L) {
                stringResource(R.string.home_goal_reached)
            } else {
                stringResource(
                    R.string.home_goal_remaining,
                    percentText(progress),
                    formatMoney(remaining, currency),
                )
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BalanceHero(    balance: Long,
    change: Float?,
    series: List<Float>,
    currency: Currency,
    month: YearMonth,
) {
    val colors = BudTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val previousLabel = remember(month, locale) {
        month.minusMonths(1)
            .format(DateTimeFormatter.ofPattern("MMMM", locale))
            .replace(".", "")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(colors.heroStart, colors.heroEnd)))
            .clickable(enabled = false) {},
    ) {
        Canvas(Modifier.fillMaxWidth().height(210.dp)) {
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.width * 0.52f,
                center = Offset(size.width * 1.02f, -size.height * 0.12f),
                style = Stroke(1.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.32f,
                center = Offset(size.width * 1.02f, -size.height * 0.12f),
                style = Stroke(1.dp.toPx()),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Overline(stringResource(R.string.overline_total_balance), color = colors.heroMuted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(colors.heroMuted.copy(alpha = 0.7f), CircleShape),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.badge_up_to_date),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.heroMuted,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = formatMoney(balance, currency),
                style = BudTheme.type.moneyHero,
                color = colors.heroText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (change != null) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (change >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = colors.heroText,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = percentText(abs(change), decimals = 1),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.heroText,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_vs_previous, previousLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.heroMuted,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.home_starting_point),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.heroMuted,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Sparkline(
                values = series,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                color = colors.heroText,
            )
        }
    }
}

@Composable
private fun MonthSummaryCard(
    income: Long,
    expense: Long,
    previous: com.budjet.app.data.MonthTotals,
    currency: Currency,
) {
    val colors = BudTheme.colors
    BudCard(padding = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
        ) {
            SummaryColumn(
                label = stringResource(R.string.overline_income),
                value = income,
                delta = Finance.monthDelta(income, previous.income),
                color = colors.income,
                currency = currency,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .background(colors.line),
            )
            SummaryColumn(
                label = stringResource(R.string.overline_expenses),
                value = expense,
                delta = Finance.monthDelta(expense, previous.expense),
                color = colors.expense,
                currency = currency,
                modifier = Modifier.weight(1f),
                invertDelta = true,
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    value: Long,
    delta: Float?,
    color: Color,
    currency: Currency,
    modifier: Modifier = Modifier,
    invertDelta: Boolean = false,
) {
    val colors = BudTheme.colors
    val good = if (invertDelta) (delta ?: 0f) <= 0f else (delta ?: 0f) >= 0f
    Column(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(color, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Overline(label)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatMoney(value, currency),
            style = BudTheme.type.moneyMedium,
            color = colors.ink,
            maxLines = 1,
        )
        if (delta != null && abs(delta) > 0.0001f) {
            Text(
                text = stringResource(
                    if (delta > 0) R.string.home_delta_more else R.string.home_delta_less,
                    percentText(abs(delta)),
                ),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = if (good) colors.inkMuted else colors.expense,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
