package com.budjet.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.TxType
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.capitalizeEs
import com.budjet.app.util.formatMonth
import com.budjet.app.util.formatMoney
import java.time.YearMonth

@Composable
fun Overline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BudTheme.colors.inkMuted,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}

@Composable
fun BudCard(
    modifier: Modifier = Modifier,
    color: Color = BudTheme.colors.surface,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    border: Boolean = true,
    padding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(
                if (border) Modifier.border(BorderStroke(1.dp, BudTheme.colors.line), shape)
                else Modifier
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = BudTheme.colors.ink,
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = BudTheme.colors.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = onTrailingClick != null) { onTrailingClick?.invoke() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun ScreenTitle(
    overline: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Overline(overline)
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = BudTheme.colors.ink,
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    enabled: Boolean = true,
    container: Color = BudTheme.colors.surface,
    tint: Color = BudTheme.colors.ink,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, BudTheme.colors.line, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else BudTheme.colors.inkMuted.copy(alpha = 0.35f),
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) BudTheme.colors.primary else BudTheme.colors.surfaceMuted)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) BudTheme.colors.onPrimary else BudTheme.colors.inkMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) BudTheme.colors.onPrimary else BudTheme.colors.inkMuted,
        )
    }
}

@Composable
fun MonthSelector(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    canGoNext: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SoftIconButton(
            icon = Icons.Rounded.ChevronLeft,
            contentDescription = "Mes anterior",
            onClick = onPrevious,
            size = 34.dp,
        )
        Text(
            text = formatMonth(month),
            modifier = Modifier.width(150.dp),
            style = MaterialTheme.typography.titleMedium,
            color = BudTheme.colors.ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        SoftIconButton(
            icon = Icons.Rounded.ChevronRight,
            contentDescription = "Mes siguiente",
            onClick = onNext,
            size = 34.dp,
            enabled = canGoNext,
        )
    }
}

@Composable
fun CategoryBadge(
    category: Category?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val color = category?.let { categoryColor(it.colorHex, BudTheme.colors.isDark) }
        ?: BudTheme.colors.inkMuted
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = if (BudTheme.colors.isDark) 0.18f else 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = categoryIcon(category?.icon ?: "more"),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

@Composable
fun LinearProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = BudTheme.colors.surfaceMuted,
    height: Dp = 6.dp,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "bar",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color),
        )
    }
}

@Composable
fun AmountText(
    cents: Long,
    type: TxType,
    currency: Currency,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.TextUnit = 14.sp,
    colorize: Boolean = true,
) {
    val colors = BudTheme.colors
    val prefix = if (type == TxType.INCOME) "+" else "−"
    val color = when {
        !colorize -> colors.ink
        type == TxType.INCOME -> colors.income
        else -> colors.ink
    }
    Text(
        text = prefix + formatMoney(cents, currency),
        modifier = modifier,
        style = BudTheme.type.moneySmall.copy(fontSize = size),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = BudTheme.colors.primarySoft,
    content: Color = BudTheme.colors.primary,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = content,
        )
    }
}

@Composable
fun ThinDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BudTheme.colors.line.copy(alpha = 0.75f)),
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp, horizontal = 32.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(BudTheme.colors.surfaceMuted),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BudTheme.colors.inkMuted,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = BudTheme.colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = BudTheme.colors.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun RowLabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = BudTheme.colors.ink,
    bold: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = BudTheme.colors.inkMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = valueColor,
        )
    }
}

@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "B" }
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = BudTheme.colors.primarySoft,
        border = BorderStroke(1.dp, BudTheme.colors.line),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = size.value.times(0.32).sp),
                color = BudTheme.colors.primary,
            )
        }
    }
}

@Composable
fun MiniLabel(text: String, color: Color = BudTheme.colors.inkMuted) {
    Text(
        text = text.capitalizeEs(),
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun BudSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    val track by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.surfaceMuted,
        animationSpec = tween(200),
        label = "switchTrack",
    )
    val thumbX by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "switchThumb",
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(CircleShape)
            .background(track)
            .border(1.dp, if (checked) colors.primary else colors.line, CircleShape)
            .toggleable(
                value = checked,
                interactionSource = interaction,
                indication = null,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { androidx.compose.ui.unit.IntOffset(thumbX.roundToPx(), 0) }
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) colors.onPrimary else colors.inkMuted),
        )
    }
}
