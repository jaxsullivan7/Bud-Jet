package com.budjet.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.ui.theme.BudTheme

@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    container: Color = BudTheme.colors.primary,
    bars: Color = BudTheme.colors.onPrimary,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * 0.5f)) {
            val barWidth = this.size.width / 6
            val heights = listOf(0.45f, 0.72f, 1f)
            heights.forEachIndexed { index, h ->
                drawRoundRect(
                    color = bars,
                    topLeft = Offset(index * (barWidth * 2), this.size.height * (1 - h)),
                    size = Size(barWidth, this.size.height * h),
                    cornerRadius = CornerRadius(barWidth / 2),
                )
            }
        }
    }
}

@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 21.sp,
    markSize: Dp = 30.dp,
    onClick: (() -> Unit)? = null,
) {
    val colors = BudTheme.colors
    Row(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark(size = markSize)
        Spacer(Modifier.width(9.dp))
        Text(
            text = "Bud ",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = fontSize),
            color = colors.ink,
        )
        Text(
            text = "Jet",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = fontSize,
                fontStyle = FontStyle.Italic,
            ),
            color = colors.primary,
        )
    }
}
