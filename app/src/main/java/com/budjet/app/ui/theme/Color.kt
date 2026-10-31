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

val NavyLight = BudPalette(
    background = Color(0xFFF2EEE5),
    surface = Color(0xFFFBF8F1),
    surfaceMuted = Color(0xFFE8E2D5),
    ink = Color(0xFF16233A),
    inkMuted = Color(0xFF6C7180),
    line = Color(0xFFDED6C6),
    primary = Color(0xFF24405F),
    onPrimary = Color(0xFFF8F2E6),
    primarySoft = Color(0xFFE3E8EF),
    income = Color(0xFF2F6B54),
    expense = Color(0xFF9C623D),
    warning = Color(0xFFB5894B),
    heroStart = Color(0xFF24405F),
    heroEnd = Color(0xFF14243B),
    heroText = Color(0xFFF8F3E8),
    heroMuted = Color(0xFFC3CEDD),
    isDark = false,
)

val NavyDark = BudPalette(
    background = Color(0xFF0C131D),
    surface = Color(0xFF141C28),
    surfaceMuted = Color(0xFF1E2836),
    ink = Color(0xFFECE9E0),
    inkMuted = Color(0xFF98A2B3),
    line = Color(0xFF2A3546),
    primary = Color(0xFFA9C0DE),
    onPrimary = Color(0xFF0B1A2A),
    primarySoft = Color(0xFF1B2A3D),
    income = Color(0xFF8FC7AA),
    expense = Color(0xFFD99A72),
    warning = Color(0xFFD9B06E),
    heroStart = Color(0xFF1B2E4A),
    heroEnd = Color(0xFF0E1A2A),
    heroText = Color(0xFFF1EDE4),
    heroMuted = Color(0xFF9FB0C4),
    isDark = true,
)

val BossLight = BudPalette(
    background = Color(0xFFF1F0EE),
    surface = Color(0xFFFAF9F7),
    surfaceMuted = Color(0xFFE7E4E2),
    ink = Color(0xFF171518),
    inkMuted = Color(0xFF6E686C),
    line = Color(0xFFDDD8D6),
    primary = Color(0xFF6E1F2B),
    onPrimary = Color(0xFFF8F1EC),
    primarySoft = Color(0xFFF2E4E5),
    income = Color(0xFF3C6B57),
    expense = Color(0xFF8E2B3A),
    warning = Color(0xFFA87B3E),
    heroStart = Color(0xFF352428),
    heroEnd = Color(0xFF171214),
    heroText = Color(0xFFF4EFE9),
    heroMuted = Color(0xFFBEB1B3),
    isDark = false,
)

val BossDark = BudPalette(
    background = Color(0xFF0B0A0B),
    surface = Color(0xFF151315),
    surfaceMuted = Color(0xFF211D20),
    ink = Color(0xFFEFEAE7),
    inkMuted = Color(0xFFA39A9E),
    line = Color(0xFF2D272B),
    primary = Color(0xFFD3A0A8),
    onPrimary = Color(0xFF2A0F14),
    primarySoft = Color(0xFF2A1B1E),
    income = Color(0xFF93B7A3),
    expense = Color(0xFFD28A93),
    warning = Color(0xFFD1A46A),
    heroStart = Color(0xFF3A2027),
    heroEnd = Color(0xFF1A1013),
    heroText = Color(0xFFF3EDE8),
    heroMuted = Color(0xFFB9A7AB),
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
