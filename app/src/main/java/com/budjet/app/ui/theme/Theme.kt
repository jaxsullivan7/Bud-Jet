package com.budjet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.budjet.app.data.ThemeMode

val LocalBudColors = staticCompositionLocalOf { PaperLight }
val LocalBudType = staticCompositionLocalOf { budTypography }

object BudTheme {
    val colors: BudPalette
        @Composable get() = LocalBudColors.current
    val type: BudTypography
        @Composable get() = LocalBudType.current
}

private val BudShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun BudJetTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) PaperDark else PaperLight
    val view = LocalView.current

    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primarySoft,
            onPrimaryContainer = palette.ink,
            secondary = palette.primary,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.inkMuted,
            outline = palette.line,
            error = palette.expense,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = palette.primarySoft,
            onPrimaryContainer = palette.ink,
            secondary = palette.primary,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.surfaceMuted,
            onSurfaceVariant = palette.inkMuted,
            outline = palette.line,
            error = palette.expense,
            onError = Color.White,
        )
    }

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(
        LocalBudColors provides palette,
        LocalBudType provides budTypography,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = budMaterialTypography,
            shapes = BudShapes,
            content = content,
        )
    }
}
