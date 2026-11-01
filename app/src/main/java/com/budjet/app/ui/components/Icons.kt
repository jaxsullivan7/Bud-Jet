package com.budjet.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.lerp
import androidx.core.graphics.toColorInt

fun parseHex(hex: String): Color = runCatching { Color(hex.toColorInt()) }.getOrDefault(Color(0xFF8C8C84))

fun categoryIcon(key: String): ImageVector = when (key) {
    "groceries" -> Icons.Rounded.ShoppingBasket
    "restaurant" -> Icons.Rounded.Restaurant
    "car" -> Icons.Rounded.DirectionsCar
    "home" -> Icons.Rounded.Home
    "bolt" -> Icons.Rounded.Bolt
    "movie" -> Icons.Rounded.Movie
    "bag" -> Icons.Rounded.ShoppingBag
    "health" -> Icons.Rounded.Favorite
    "subscriptions" -> Icons.Rounded.Subscriptions
    "flight" -> Icons.Rounded.Flight
    "school" -> Icons.Rounded.School
    "more" -> Icons.Rounded.MoreHoriz
    "salary" -> Icons.Rounded.Payments
    "work" -> Icons.Rounded.Work
    "trending" -> Icons.Rounded.TrendingUp
    "gift" -> Icons.Rounded.CardGiftcard
    "refund" -> Icons.Rounded.Undo
    "reversal" -> Icons.Rounded.Block
    "spa" -> Icons.Rounded.Spa
    "fund" -> Icons.Rounded.PieChart
    "stock" -> Icons.Rounded.ShowChart
    "etf" -> Icons.Rounded.Public
    "crypto" -> Icons.Rounded.CurrencyExchange
    "deposit" -> Icons.Rounded.Savings
    "pension" -> Icons.Rounded.Shield
    "real_estate" -> Icons.Rounded.Home
    "bank" -> Icons.Rounded.AccountBalance
    "savings" -> Icons.Rounded.Savings
    "cash" -> Icons.Rounded.Payments
    "card" -> Icons.Rounded.CreditCard
    else -> Icons.Rounded.Category
}

/** Earthy category colors can get muddy on a dark canvas, so lift them a little. */
fun categoryColor(hex: String, dark: Boolean): Color {
    val base = parseHex(hex)
    return if (dark) lerp(base, Color.White, 0.34f) else base
}

val iconChoices = listOf(
    "groceries" to "Cesta",
    "restaurant" to "Restaurante",
    "car" to "Coche",
    "home" to "Casa",
    "bolt" to "Factura",
    "movie" to "Ocio",
    "bag" to "Compras",
    "health" to "Salud",
    "subscriptions" to "Suscripción",
    "flight" to "Viaje",
    "school" to "Estudios",
    "trending" to "Inversión",
    "work" to "Trabajo",
    "gift" to "Regalo",
    "refund" to "Devolución",
    "reversal" to "Anulación",
    "spa" to "Belleza",
    "more" to "Otros",
)
