package com.budjet.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.budjet.app.R

val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val SerifDisplay = FontFamily(
    Font(R.font.dm_serif, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.dm_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

private val tight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private val base = TextStyle(
    fontFamily = Inter,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = tight,
)

@Immutable
data class BudTypography(
    val moneyHero: TextStyle = base.copy(
        fontSize = 43.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1.8).sp,
        fontFeatureSettings = "tnum",
    ),
    val moneyLarge: TextStyle = base.copy(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum",
    ),
    val moneyMedium: TextStyle = base.copy(
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = "tnum",
    ),
    val moneySmall: TextStyle = base.copy(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum",
    ),
)

val budTypography = BudTypography()

val budMaterialTypography = Typography(
    displayLarge = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 44.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 34.sp,
        lineHeight = 38.sp,
    ),
    displaySmall = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    ),
    headlineLarge = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 32.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = base.copy(
        fontFamily = SerifDisplay,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = base.copy(
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = base.copy(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = base.copy(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = base.copy(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = base.copy(
        fontSize = 13.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = base.copy(
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = base.copy(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = base.copy(
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = base.copy(
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
    ),
)
