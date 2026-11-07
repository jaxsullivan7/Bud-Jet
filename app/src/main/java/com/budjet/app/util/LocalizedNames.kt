package com.budjet.app.util

import android.content.Context
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.Category
import com.budjet.app.data.Investment
import com.budjet.app.data.Liability
import com.budjet.app.data.SavingsGoal

/** Built-in category names are translated by ID; user-created categories keep their name. */
fun builtInCategoryNameRes(id: String): Int? = when (id) {
    "food" -> R.string.category_food
    "dining" -> R.string.category_dining
    "transport" -> R.string.category_transport
    "home" -> R.string.category_home
    "bills" -> R.string.category_bills
    "leisure" -> R.string.category_leisure
    "shopping" -> R.string.category_shopping
    "health" -> R.string.category_health
    "beauty" -> R.string.category_beauty
    "subs" -> R.string.category_subs
    "travel" -> R.string.category_travel
    "education" -> R.string.category_education
    "other" -> R.string.category_other
    "salary" -> R.string.category_salary
    "freelance" -> R.string.category_freelance
    "invest" -> R.string.category_invest
    "gifts" -> R.string.category_gifts
    "refund" -> R.string.category_refund
    "reversal" -> R.string.category_reversal
    else -> null
}

/** Built-in account names are translated by ID; user-created accounts keep their name. */
fun builtInAccountNameRes(id: String): Int? = when (id) {
    "main" -> R.string.account_main
    "savings" -> R.string.account_savings
    "cash" -> R.string.account_cash
    "card" -> R.string.account_card
    else -> null
}

/** Demo investment products have stable IDs, so their sample names can be translated. */
fun builtInInvestmentNameRes(id: String): Int? = when (id) {
    "i_index" -> R.string.demo_inv_index
    "i_btc" -> R.string.demo_inv_btc
    "i_pension" -> R.string.demo_inv_pension
    "i_tech" -> R.string.demo_inv_tech
    else -> null
}

fun builtInGoalNameRes(id: String): Int? = when (id) {
    "g_emergency" -> R.string.demo_goal_emergency
    "g_japan" -> R.string.demo_goal_japan
    "g_laptop" -> R.string.demo_goal_laptop
    else -> null
}

fun builtInLiabilityNameRes(id: String): Int? = when (id) {
    "l_car" -> R.string.demo_liability_car
    "l_card" -> R.string.demo_liability_card
    else -> null
}

/**
 * Maps the canonical Spanish sample notes to the current language. Notes written by
 * the user are never in this list, so they are returned unchanged.
 */
fun builtInNoteRes(note: String): Int? = when (note) {
    "Nómina" -> R.string.demo_note_salary
    "Alquiler" -> R.string.demo_note_rent
    "Luz y agua" -> R.string.demo_note_utilities
    "Spotify" -> R.string.demo_note_spotify
    "iCloud+" -> R.string.demo_note_icloud
    "Internet" -> R.string.demo_note_internet
    "Mercadona" -> R.string.demo_note_mercadona
    "Frutería del barrio" -> R.string.demo_note_greengrocer
    "Carrefour" -> R.string.demo_note_carrefour
    "Mercado central" -> R.string.demo_note_market
    "Café" -> R.string.demo_note_coffee
    "La Pepita" -> R.string.demo_note_la_pepita
    "Sushi Ono" -> R.string.demo_note_sushi_ono
    "Bar Manolo" -> R.string.demo_note_bar_manolo
    "Casa Rafa" -> R.string.demo_note_casa_rafa
    "Pizzería Roma" -> R.string.demo_note_pizzeria
    "Abono transporte" -> R.string.demo_note_transport_pass
    "Gasolina" -> R.string.demo_note_fuel
    "Taxi" -> R.string.demo_note_taxi
    "Cine" -> R.string.demo_note_cinema
    "Concierto" -> R.string.demo_note_concert
    "Farmacia" -> R.string.demo_note_pharmacy
    "Zara" -> R.string.demo_note_zara
    "Zapatillas" -> R.string.demo_note_sneakers
    "Panadería" -> R.string.demo_note_bakery
    "Escapada a Lisboa" -> R.string.demo_note_lisbon
    "Curso de fotografía" -> R.string.demo_note_photo_course
    "Proyecto web" -> R.string.demo_note_web_project
    "Cena con amigos" -> R.string.demo_note_dinner
    "Café con leche" -> R.string.demo_note_latte
    "Supermercado" -> R.string.demo_note_supermarket
    "Gimnasio" -> R.string.demo_note_gym
    "Aportación inicial" -> R.string.demo_note_initial
    "Aportación periódica" -> R.string.demo_note_periodic
    "Actualización de valor" -> R.string.demo_note_value_update
    "Compra" -> R.string.demo_note_purchase
    "Rentabilidad" -> R.string.demo_note_return
    "Corrección de mercado" -> R.string.demo_note_market_correction
    "Reposición de efectivo" -> R.string.demo_note_cash_refill
    "Pago de tarjeta" -> R.string.demo_note_card_payment
    "Pago parcial" -> R.string.demo_note_partial_payment
    else -> null
}

fun Category.localizedName(context: Context): String =
    builtInCategoryNameRes(id)?.let(context::getString) ?: name

fun Account.localizedName(context: Context): String =
    builtInAccountNameRes(id)?.let(context::getString) ?: name

fun Investment.localizedName(context: Context): String =
    builtInInvestmentNameRes(id)?.let(context::getString) ?: name

fun SavingsGoal.localizedName(context: Context): String =
    builtInGoalNameRes(id)?.let(context::getString) ?: name

fun Liability.localizedName(context: Context): String =
    builtInLiabilityNameRes(id)?.let(context::getString) ?: name

fun String.localizedNote(context: Context): String =
    builtInNoteRes(this)?.let(context::getString) ?: this
