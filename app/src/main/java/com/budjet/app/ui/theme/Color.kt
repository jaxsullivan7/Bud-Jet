package com.budjet.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class BudPalette(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val ink: Color,
    val inkMuted: Color,
    val line: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val income: Color,
    val expense: Color,
    val warning: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val heroText: Color,
    val heroMuted: Color,
    val isDark: Boolean,
)

val PaperLight = BudPalette(
    background = Color(0xFFF4F1EA),
    surface = Color(0xFFFCFAF6),
    surfaceMuted = Color(0xFFEDE8DD),
    ink = Color(0xFF171A18),
    inkMuted = Color(0xFF71756C),
    line = Color(0xFFE4DED2),
    primary = Color(0xFF2C5C4C),
    onPrimary = Color(0xFFF8F4EA),
    primarySoft = Color(0xFFE5EDE7),
    income = Color(0xFF2F6B54),
    expense = Color(0xFFB4553F),
    warning = Color(0xFFC08A3E),
    heroStart = Color(0xFF2F6150),
    heroEnd = Color(0xFF223F36),
    heroText = Color(0xFFF6F1E5),
    heroMuted = Color(0xFFC9D8CE),
    isDark = false,
)

val PaperDark = BudPalette(
    background = Color(0xFF0E1110),
    surface = Color(0xFF171B18),
    surfaceMuted = Color(0xFF202620),
    ink = Color(0xFFECEBE2),
    inkMuted = Color(0xFF98A096),
    line = Color(0xFF2A312C),
    primary = Color(0xFFA9CDBB),
    onPrimary = Color(0xFF0E241C),
    primarySoft = Color(0xFF1C2B24),
    income = Color(0xFF8FC7AA),
    expense = Color(0xFFE09A83),
    warning = Color(0xFFE0B878),
    heroStart = Color(0xFF22352E),
    heroEnd = Color(0xFF18241F),
    heroText = Color(0xFFEDF0E6),
    heroMuted = Color(0xFF9FB3A8),
    isDark = true,
)

/** Sober, earthy palette used for categories. */
object CategoryPalette {
    val swatches = listOf(
        "#6C7F5B", "#C2703D", "#4E6E81", "#8A6A4F", "#B08B4F",
        "#7D5A6B", "#A94438", "#5E7D6E", "#5C6470", "#3D6B6B",
        "#6B6A9E", "#8C8C84", "#2F6B54", "#4E7A68", "#6E7F3D", "#A8794A",
    )
}
