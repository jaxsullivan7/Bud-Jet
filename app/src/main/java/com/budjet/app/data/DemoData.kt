package com.budjet.app.data

import java.time.LocalDate
import java.time.YearMonth
import kotlin.random.Random

/**
 * Realistic sample data so the first run of the app has something to look at.
 * Everything is relative to today, which keeps the demo fresh.
 */
object DemoData {

    val categories: List<Category> = listOf(
        Category("food", "Alimentación", "groceries", "#6C7F5B", TxType.EXPENSE),
        Category("dining", "Restaurantes", "restaurant", "#C2703D", TxType.EXPENSE),
        Category("transport", "Transporte", "car", "#4E6E81", TxType.EXPENSE),
        Category("home", "Hogar", "home", "#8A6A4F", TxType.EXPENSE),
        Category("bills", "Facturas", "bolt", "#B08B4F", TxType.EXPENSE),
        Category("leisure", "Ocio", "movie", "#7D5A6B", TxType.EXPENSE),
        Category("shopping", "Compras", "bag", "#A94438", TxType.EXPENSE),
        Category("health", "Salud", "health", "#5E7D6E", TxType.EXPENSE),
        Category("subs", "Suscripciones", "subscriptions", "#5C6470", TxType.EXPENSE),
        Category("travel", "Viajes", "flight", "#3D6B6B", TxType.EXPENSE),
        Category("education", "Educación", "school", "#6B6A9E", TxType.EXPENSE),
        Category("other", "Otros", "more", "#8C8C84", TxType.EXPENSE),
        Category("salary", "Nómina", "salary", "#2F6B54", TxType.INCOME),
        Category("freelance", "Freelance", "work", "#4E7A68", TxType.INCOME),
        Category("invest", "Inversiones", "trending", "#6E7F3D", TxType.INCOME),
        Category("gifts", "Regalos", "gift", "#A8794A", TxType.INCOME),
    )

    val accounts: List<Account> = listOf(
        Account("main", "Cuenta principal", "bank", "#4E6E81"),
        Account("savings", "Ahorro", "savings", "#6C7F5B"),
        Account("cash", "Efectivo", "cash", "#A8794A"),
        Account("card", "Tarjeta", "card", "#7D5A6B"),
    )

    val budgets: List<Budget> = listOf(
        Budget("b_overall", null, 210_000),
        Budget("b_food", "food", 42_000),
        Budget("b_dining", "dining", 16_000),
        Budget("b_transport", "transport", 11_000),
        Budget("b_home", "home", 96_000),
        Budget("b_leisure", "leisure", 14_000),
        Budget("b_shopping", "shopping", 15_000),
        Budget("b_health", "health", 7_000),
        Budget("b_subs", "subs", 4_500),
    )

    fun build(today: LocalDate = LocalDate.now()): AppData {
        val transactions = mutableListOf<Transaction>()
        var counter = 0

        fun add(
            date: LocalDate,
            type: TxType,
            category: String,
            amount: Long,
            account: String = "main",
            note: String,
        ) {
            transactions += Transaction(
                id = "t${counter++}",
                amountCents = amount,
                type = type,
                categoryId = category,
                accountId = account,
                dateEpochDay = date.toEpochDay(),
                note = note,
            )
        }

        for (back in 5 downTo 0) {
            val month = YearMonth.from(today).minusMonths(back.toLong())
            val rnd = Random(back * 977 + 31)
            val lastDay = if (back == 0) today.dayOfMonth else month.lengthOfMonth()
            fun day(d: Int) = month.atDay(d.coerceAtMost(lastDay).coerceAtLeast(1))
            fun inMonth(d: Int) = d <= lastDay

            add(day(1), TxType.INCOME, "salary", 245_000, note = "Nómina")
            if (inMonth(2)) add(day(2), TxType.EXPENSE, "home", 85_000, note = "Alquiler")
            if (inMonth(3)) add(day(3), TxType.EXPENSE, "bills", (5_800 + rnd.nextInt(2_400)).toLong(), note = "Luz y agua")
            if (inMonth(3)) add(day(3), TxType.EXPENSE, "subs", 1_299, account = "card", note = "Spotify")
            if (inMonth(5)) add(day(5), TxType.EXPENSE, "subs", 999, account = "card", note = "iCloud+")
            if (inMonth(5)) add(day(5), TxType.EXPENSE, "home", 4_990, note = "Internet")

            val groceryDays = listOf(4, 10, 17, 24)
            for (d in groceryDays) {
                if (inMonth(d)) {
                    add(
                        day(d),
                        TxType.EXPENSE,
                        "food",
                        (3_800 + rnd.nextInt(3_600)).toLong(),
                        account = if (rnd.nextBoolean()) "card" else "main",
                        note = listOf("Mercadona", "Frutería del barrio", "Carrefour", "Mercado central")[rnd.nextInt(4)],
                    )
                }
            }

            val coffeeCount = 3 + rnd.nextInt(3)
            repeat(coffeeCount) {
                val d = 1 + rnd.nextInt(lastDay)
                add(day(d), TxType.EXPENSE, "dining", (250 + rnd.nextInt(450)).toLong(), account = "cash", note = "Café")
            }

            repeat(2 + rnd.nextInt(3)) {
                val d = 2 + rnd.nextInt(lastDay - 1)
                add(
                    day(d), TxType.EXPENSE, "dining", (1_200 + rnd.nextInt(2_600)).toLong(),
                    account = "card",
                    note = listOf("La Pepita", "Sushi Ono", "Bar Manolo", "Casa Rafa", "Pizzería Roma")[rnd.nextInt(5)],
                )
            }

            if (inMonth(8)) add(day(8), TxType.EXPENSE, "transport", 4_500, account = "main", note = "Abono transporte")
            repeat(2 + rnd.nextInt(3)) {
                val d = 2 + rnd.nextInt(lastDay - 1)
                add(day(d), TxType.EXPENSE, "transport", (900 + rnd.nextInt(2_000)).toLong(), account = "card", note = "Gasolina")
            }
            repeat(1 + rnd.nextInt(2)) {
                val d = 2 + rnd.nextInt(lastDay - 1)
                add(day(d), TxType.EXPENSE, "transport", (450 + rnd.nextInt(900)).toLong(), account = "cash", note = "Taxi")
            }

            if (inMonth(12)) add(day(12), TxType.EXPENSE, "leisure", 1_199, account = "card", note = "Cine")
            if (inMonth(19)) add(day(19), TxType.EXPENSE, "leisure", (1_800 + rnd.nextInt(2_200)).toLong(), account = "card", note = "Concierto")
            if (inMonth(21)) add(day(21), TxType.EXPENSE, "health", (1_500 + rnd.nextInt(3_000)).toLong(), note = "Farmacia")
            if (inMonth(16)) add(day(16), TxType.EXPENSE, "shopping", (2_900 + rnd.nextInt(6_000)).toLong(), account = "card", note = "Zara")
            if (back % 2 == 0 && inMonth(22)) {
                add(day(22), TxType.EXPENSE, "shopping", 7_990, account = "card", note = "Zapatillas")
            }
            if (inMonth(26)) add(day(26), TxType.EXPENSE, "food", (2_400 + rnd.nextInt(2_000)).toLong(), account = "cash", note = "Panadería")
            if (back == 2 && inMonth(14)) {
                add(day(14), TxType.EXPENSE, "travel", 18_500, account = "card", note = "Escapada a Lisboa")
            }
            if (back == 3 && inMonth(9)) {
                add(day(9), TxType.EXPENSE, "education", 4_900, account = "card", note = "Curso de fotografía")
            }
            if (back % 3 == 1) {
                add(day(20), TxType.INCOME, "freelance", (32_000 + rnd.nextInt(22_000)).toLong(), note = "Proyecto web")
            }
            if (inMonth(28)) add(day(28), TxType.EXPENSE, "dining", 3_450, account = "card", note = "Cena con amigos")
        }

        // A fresh movement for today so the "Hoy" group is always alive.
        add(today, TxType.EXPENSE, "dining", 850, account = "cash", note = "Café con leche")
        if (today.dayOfMonth < 29) {
            add(today, TxType.EXPENSE, "food", 2_780, account = "card", note = "Supermercado")
        }

        val thisMonth = YearMonth.from(today)
        val monthKey = thisMonth.toString()
        val startOfMonth = thisMonth.atDay(1).toEpochDay()

        val recurringRules = listOf(
            RecurringRule(
                id = "r_salary", amountCents = 245_000, type = TxType.INCOME,
                categoryId = "salary", accountId = "main", dayOfMonth = 1,
                note = "Nómina", startEpochDay = startOfMonth, lastPostedMonth = monthKey,
            ),
            RecurringRule(
                id = "r_rent", amountCents = 85_000, type = TxType.EXPENSE,
                categoryId = "home", accountId = "main", dayOfMonth = 2,
                note = "Alquiler", startEpochDay = startOfMonth, lastPostedMonth = monthKey,
            ),
            RecurringRule(
                id = "r_spotify", amountCents = 1_299, type = TxType.EXPENSE,
                categoryId = "subs", accountId = "card", dayOfMonth = 3,
                note = "Spotify", startEpochDay = startOfMonth, lastPostedMonth = monthKey,
            ),
            RecurringRule(
                id = "r_icloud", amountCents = 999, type = TxType.EXPENSE,
                categoryId = "subs", accountId = "card", dayOfMonth = 5,
                note = "iCloud+", startEpochDay = startOfMonth, lastPostedMonth = monthKey,
            ),
            RecurringRule(
                id = "r_internet", amountCents = 4_990, type = TxType.EXPENSE,
                categoryId = "home", accountId = "main", dayOfMonth = 5,
                note = "Internet", startEpochDay = startOfMonth, lastPostedMonth = monthKey,
            ),
            // This one is due the first time the demo runs, so the automatic
            // posting can be seen in action (and then waits until next month).
            RecurringRule(
                id = "r_gym", amountCents = 3_990, type = TxType.EXPENSE,
                categoryId = "health", accountId = "card", dayOfMonth = today.dayOfMonth,
                note = "Gimnasio", startEpochDay = today.toEpochDay(), lastPostedMonth = null,
            ),
        )

        val goals = listOf(
            SavingsGoal(
                id = "g_emergency", name = "Fondo de emergencia",
                targetCents = 600_000, savedCents = 285_000,
                icon = "savings", colorHex = "#2F6B54",
            ),
            SavingsGoal(
                id = "g_japan", name = "Viaje a Japón",
                targetCents = 450_000, savedCents = 128_000,
                deadlineEpochDay = today.plusMonths(10).toEpochDay(),
                icon = "flight", colorHex = "#3D6B6B",
            ),
            SavingsGoal(
                id = "g_laptop", name = "Portátil nuevo",
                targetCents = 160_000, savedCents = 64_000,
                deadlineEpochDay = today.plusMonths(5).toEpochDay(),
                icon = "bag", colorHex = "#6B6A9E",
            ),
        )

        return AppData(
            version = 4,
            settings = Settings(openingBalanceCents = 385_000),
            categories = categories,
            accounts = accounts,
            transactions = transactions.sortedByDescending { it.dateEpochDay },
            budgets = budgets,
            recurringRules = recurringRules,
            savingsGoals = goals,
        )
    }
}
