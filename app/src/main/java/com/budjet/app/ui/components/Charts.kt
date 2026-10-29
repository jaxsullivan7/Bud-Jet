package com.budjet.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.MonthPoint
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMonthShort
import java.time.YearMonth

@Composable
fun CashflowChart(
    points: List<MonthPoint>,
    modifier: Modifier = Modifier,
    highlightMonth: YearMonth? = null,
) {
    val colors = BudTheme.colors
    val anim = remember { Animatable(0f) }
    val signature = points.joinToString { "${it.month}:${it.income}:${it.expense}" }
    LaunchedEffect(signature) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val maxValue = points.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(128.dp),
        ) {
            val groupWidth = size.width / points.size.coerceAtLeast(1)
            val barWidth = 7.dp.toPx()
            val gap = 3.5.dp.toPx()
            val usable = size.height - 6.dp.toPx()
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(
                    color = colors.line.copy(alpha = 0.65f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            points.forEachIndexed { index, point ->
                val center = groupWidth * (index + 0.5f)
                val base = size.height
                val incomeHeight = (point.income.toFloat() / maxValue) * usable * anim.value
                val expenseHeight = (point.expense.toFloat() / maxValue) * usable * anim.value
                val isCurrent = point.month == highlightMonth
                drawRoundRect(
                    color = colors.income.copy(alpha = if (isCurrent) 1f else 0.55f),
                    topLeft = Offset(center - gap / 2 - barWidth, base - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                )
                drawRoundRect(
                    color = colors.expense.copy(alpha = if (isCurrent) 1f else 0.42f),
                    topLeft = Offset(center + gap / 2, base - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            points.forEach { point ->
                Text(
                    text = formatMonthShort(point.month).take(3),
                    modifier = Modifier.weight(1f),
                    style = com.budjet.app.ui.theme.budMaterialTypography.labelMedium,
                    color = if (point.month == highlightMonth) colors.ink else colors.inkMuted,
                    fontWeight = if (point.month == highlightMonth) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

data class DonutSegment(val value: Float, val color: Color)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val colors = BudTheme.colors
    val anim = remember { Animatable(0f) }
    val signature = segments.joinToString { "${it.value}:${it.color.value}" }
    LaunchedEffect(signature) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = colors.surfaceMuted,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
            var start = -90f
            segments.forEach { segment ->
                val rawSweep = 360f * (segment.value / total)
                val gap = if (rawSweep > 8f) 3f else 0f
                val sweep = ((rawSweep - gap) * anim.value).coerceAtLeast(0f)
                if (sweep > 0.5f) {
                    drawArc(
                        color = segment.color,
                        startAngle = start + gap / 2,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                start += rawSweep
            }
        }
        center()
    }
}

@Composable
fun RingProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 6.dp,
    track: Color = BudTheme.colors.surfaceMuted,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        anim.animateTo(progress.coerceIn(0f, 1.5f), tween(800, easing = FastOutSlowInEasing))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * anim.value,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        center()
    }
}

@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = BudTheme.colors.heroText,
    strokeWidth: Dp = 1.6.dp,
) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0001f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val topPadding = 4.dp.toPx()
        val usableHeight = size.height - topPadding - 2.dp.toPx()
        val points = values.mapIndexed { index, value ->
            Offset(
                x = stepX * index,
                y = topPadding + (1f - (value - min) / range) * usableHeight,
            )
        }
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val previous = points[i - 1]
                val current = points[i]
                val midX = (previous.x + current.x) / 2f
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            ),
        )
        drawPath(
            path = line,
            color = color.copy(alpha = 0.9f),
            style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(
            color = color,
            radius = 2.6.dp.toPx(),
            center = points.last(),
        )
    }
}

@Composable
fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = com.budjet.app.ui.theme.budMaterialTypography.labelMedium,
            color = BudTheme.colors.inkMuted,
        )
    }
}
