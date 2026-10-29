package com.budjet.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PieChartOutline
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.ui.theme.BudTheme

enum class AppTab(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Inicio", Icons.Rounded.Home),
    TRANSACTIONS("Movimientos", Icons.Rounded.ReceiptLong),
    BUDGETS("Presupuestos", Icons.Rounded.PieChartOutline),
    ANALYSIS("Análisis", Icons.Rounded.Insights),
}

@Composable
fun BudBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(24.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarTab(AppTab.HOME, selected, onSelect, Modifier.weight(1f))
            BarTab(AppTab.TRANSACTIONS, selected, onSelect, Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(68.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Añadir movimiento",
                        tint = colors.onPrimary,
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
            BarTab(AppTab.BUDGETS, selected, onSelect, Modifier.weight(1f))
            BarTab(AppTab.ANALYSIS, selected, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BarTab(
    tab: AppTab,
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    val active = tab == selected
    val tint by animateColorAsState(
        targetValue = if (active) colors.primary else colors.inkMuted,
        animationSpec = tween(220),
        label = "tabTint",
    )
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null) { onSelect(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                letterSpacing = 0.1.sp,
            ),
            color = tint,
            maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .size(3.dp)
                .background(if (active) colors.primary else Color.Transparent, CircleShape),
        )
    }
}
