package com.budjet.app

import com.budjet.app.data.Finance
import com.budjet.app.data.Recurring
import com.budjet.app.data.RecurringFrequency
import com.budjet.app.data.RecurringRule
import com.budjet.app.data.SavingsGoal
import com.budjet.app.data.TxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurringTest {

    private val today = LocalDate.of(2026, 10, 30)

    private fun rule(
        day: Int,
        start: LocalDate = LocalDate.of(2026, 10, 1),
        lastPosted: String? = null,
        active: Boolean = true,
    ) = RecurringRule(
        id = "r1",
        amountCents = 10_000,
        type = TxType.EXPENSE,
        categoryId = "home",
        accountId = "main",
        dayOfMonth = day,
        note = "Prueba",
        startEpochDay = start.toEpochDay(),
        lastPostedMonth = lastPosted,
        active = active,
    )

    private fun yearlyRule(
        day: Int,
        month: Int,
        start: LocalDate = LocalDate.of(2026, 1, 1),
        lastPosted: String? = null,
        active: Boolean = true,
    ) = RecurringRule(
        id = "ry1",
        amountCents = 10_000,
        type = TxType.EXPENSE,
        categoryId = "home",
        accountId = "main",
        dayOfMonth = day,
        monthOfYear = month,
        frequency = RecurringFrequency.YEARLY,
        note = "Anual",
        startEpochDay = start.toEpochDay(),
        lastPostedMonth = lastPosted,
        active = active,
    )

    @Test
    fun `a rule is due on its day when it has not been posted`() {
        val due = Recurring.dueToday(rule(day = 30), today)
        assertEquals(today, due)
    }

    @Test
    fun `a rule is not due twice in the same month`() {
        assertNull(Recurring.dueToday(rule(day = 30, lastPosted = "2026-10"), today))
    }

    @Test
    fun `next occurrence skips a month that was already posted`() {
        val next = Recurring.nextOccurrence(rule(day = 30, lastPosted = "2026-10"), today)
        assertEquals(LocalDate.of(2026, 11, 30), next)
    }

    @Test
    fun `next occurrence respects the day of the month`() {
        val next = Recurring.nextOccurrence(rule(day = 31), LocalDate.of(2026, 10, 30))
        assertEquals(LocalDate.of(2026, 10, 31), next)
    }

    @Test
    fun `february clamps day 31 to the last day`() {
        val febToday = LocalDate.of(2027, 2, 28)
        val due = Recurring.dueToday(
            rule(day = 31, start = LocalDate.of(2027, 2, 1)),
            febToday,
        )
        assertEquals(febToday, due)

        val next = Recurring.nextOccurrence(
            rule(day = 31, start = LocalDate.of(2027, 2, 1), lastPosted = "2027-02"),
            febToday,
        )
        assertEquals(LocalDate.of(2027, 3, 31), next)
    }

    @Test
    fun `a rule created today does not backfill an earlier day`() {
        val createdToday = rule(day = 1, start = today)
        assertNull(Recurring.dueToday(createdToday, today))
        assertEquals(LocalDate.of(2026, 11, 1), Recurring.nextOccurrence(createdToday, today))
    }

    @Test
    fun `paused rules are neither due nor upcoming`() {
        assertNull(Recurring.dueToday(rule(day = 30, active = false), today))
        assertNull(Recurring.nextOccurrence(rule(day = 30, active = false), today))
    }

    @Test
    fun `yearly rules are due on their month and day`() {
        val dueDate = LocalDate.of(2026, 3, 15)
        assertEquals(
            dueDate,
            Recurring.dueToday(yearlyRule(day = 15, month = 3), dueDate),
        )
    }

    @Test
    fun `yearly rules are not posted twice in the same year`() {
        val dueDate = LocalDate.of(2026, 3, 15)
        assertNull(
            Recurring.dueToday(
                yearlyRule(day = 15, month = 3, lastPosted = "2026"),
                dueDate,
            ),
        )
    }

    @Test
    fun `next yearly occurrence moves to the following year`() {
        val dueDate = LocalDate.of(2026, 3, 15)
        assertEquals(
            LocalDate.of(2027, 3, 15),
            Recurring.nextOccurrence(
                yearlyRule(day = 15, month = 3, lastPosted = "2026"),
                dueDate,
            ),
        )
    }

    @Test
    fun `yearly rule created after its date waits until next year`() {
        val createdToday = yearlyRule(day = 15, month = 3, start = today)
        assertNull(Recurring.dueToday(createdToday, today))
        assertEquals(LocalDate.of(2027, 3, 15), Recurring.nextOccurrence(createdToday, today))
    }

    @Test
    fun `yearly february 29 clamps to february 28`() {
        val dueDate = LocalDate.of(2027, 2, 28)
        assertEquals(
            dueDate,
            Recurring.dueToday(
                yearlyRule(day = 29, month = 2, start = LocalDate.of(2027, 1, 1)),
                dueDate,
            ),
        )
    }
}

class SavingsGoalTest {

    private val goal = SavingsGoal(
        id = "g1",
        name = "Viaje",
        targetCents = 450_000,
        savedCents = 128_000,
        deadlineEpochDay = LocalDate.of(2027, 8, 30).toEpochDay(),
        icon = "flight",
        colorHex = "#3D6B6B",
    )

    @Test
    fun `progress is saved over target`() {
        assertEquals(128_000f / 450_000f, Finance.goalProgress(goal), 0.0001f)
    }

    @Test
    fun `remaining never goes below zero`() {
        assertEquals(322_000L, Finance.goalRemaining(goal))
        assertEquals(0L, Finance.goalRemaining(goal.copy(savedCents = 500_000)))
    }

    @Test
    fun `monthly suggestion spreads the remaining amount to the deadline`() {
        assertEquals(32_200L, Finance.goalMonthlySuggestion(goal, LocalDate.of(2026, 10, 30)))
    }

    @Test
    fun `completed goal has full progress`() {
        assertEquals(1f, Finance.goalProgress(goal.copy(savedCents = 900_000)), 0.0001f)
    }
}
