package com.budjet.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMoney
import java.time.LocalDate

@Composable
fun TransactionRow(
    transaction: Transaction,
    category: Category?,
    account: Account?,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = BudTheme.colors
    val isFuture = LocalDate.ofEpochDay(transaction.dateEpochDay).isAfter(LocalDate.now())
    val clickAction = onClick
    val longAction = onLongClick
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primarySoft.copy(alpha = 0.6f) else Color.Transparent)
            .pointerInput(transaction.id) {
                detectTapGestures(
                    onTap = { clickAction() },
                    onLongPress = { longAction?.invoke() },
                )
            }
            .semantics {
                this.onClick { clickAction(); true }
                if (longAction != null) this.onLongClick { longAction(); true }
            }
            .testTag("tx-${transaction.note.ifBlank { transaction.id }}")
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectionMode) {
            Icon(
                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = stringResource(
                    if (selected) R.string.semantics_selected else R.string.semantics_not_selected,
                ),
                tint = if (selected) colors.primary else colors.inkMuted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        CategoryBadge(category = category, size = 42.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = transaction.note.ifBlank {
                        category?.name ?: stringResource(R.string.transaction_fallback)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (transaction.recurringId != null) {
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Rounded.EventRepeat,
                        contentDescription = stringResource(R.string.semantics_recurring),
                        tint = colors.inkMuted,
                        modifier = Modifier.size(12.dp),
                    )
                }
                if (isFuture) {
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = stringResource(R.string.transaction_scheduled),
                        tint = colors.warning,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = buildString {
                    append(category?.name ?: stringResource(R.string.category_uncategorized))
                    if (account != null) {
                        append(" · ")
                        append(account.name)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (transaction.type == TxType.INCOME) "+" else "−") +
                    formatMoney(transaction.amountCents, currency),
                style = BudTheme.type.moneySmall,
                color = when {
                    isFuture -> colors.ink
                    transaction.type == TxType.INCOME -> colors.income
                    else -> colors.ink
                },
                maxLines = 1,
            )
            if (isFuture) {
                Text(
                    text = stringResource(R.string.transaction_scheduled),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = colors.warning,
                )
                Text(
                    text = com.budjet.app.util.formatShortDate(LocalDate.ofEpochDay(transaction.dateEpochDay)),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = colors.inkMuted,
                )
            } else if (showDate) {
                Text(
                    text = com.budjet.app.util.formatDayLabel(java.time.LocalDate.ofEpochDay(transaction.dateEpochDay)),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

@Composable
fun SectionRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: String? = null,
    trailingColor: androidx.compose.ui.graphics.Color = BudTheme.colors.inkMuted,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = BudTheme.colors.ink)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = BudTheme.colors.inkMuted)
            }
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelLarge, color = trailingColor)
        }
    }
}
