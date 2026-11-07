package com.budjet.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.budjet.app.MainActivity
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.BudRepository
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatMonthLower
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.TimeUnit

object BudJetNotifications {

    private const val CHANNEL_ID = "budjet_reminders"
    private const val WORK_NAME = "budjet-daily-reminders"
    private const val PREFS = "budjet_notifications"

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMillis().coerceAtLeast(0L)
        val request = PeriodicWorkRequestBuilder<BudJetDailyWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun notify(context: Context, id: Int, title: String, message: String) {
        if (!canNotify(context)) return
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_budjet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    internal fun checkAndNotify(context: Context, data: AppData, today: LocalDate = LocalDate.now()) {
        if (!data.settings.notificationsEnabled) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val currency = Currency.from(data.settings.currencyCode)
        val month = YearMonth.from(today)
        val posted = Finance.postedTransactionsOfMonth(data, month, today)
        val totals = Finance.totals(posted)

        if (data.settings.budgetAlerts) {
            data.budgets.forEach { budget ->
                val spent = if (budget.categoryId == null) {
                    totals.expense
                } else {
                    Finance.spentForCategory(data, month, budget.categoryId, today)
                }
                val ratio = if (budget.limitCents <= 0L) 0f else spent.toFloat() / budget.limitCents
                val level = when {
                    ratio >= 1f -> 100
                    ratio >= 0.8f -> 80
                    else -> 0
                }
                if (level == 0) return@forEach
                val key = "budget_${budget.id}_${month}_$level"
                if (prefs.getBoolean(key, false)) return@forEach
                val name = budget.categoryId?.let { data.category(it)?.name }
                    ?: context.getString(R.string.notif_budget_general)
                val title = if (level == 100) {
                    context.getString(R.string.notif_budget_exceeded_title, name)
                } else {
                    context.getString(
                        R.string.notif_budget_near_title,
                        name,
                        (ratio * 100).toInt(),
                    )
                }
                notify(
                    context,
                    key.hashCode(),
                    title,
                    context.getString(
                        R.string.notif_budget_message,
                        formatMoney(spent, currency),
                        formatMoney(budget.limitCents, currency),
                    ),
                )
                prefs.edit { putBoolean(key, true) }
            }
        }

        if (data.settings.recurringAlerts) {
            val tomorrow = today.plusDays(1)
            data.recurringRules.filter { it.active && it.dayOfMonth == tomorrow.dayOfMonth }.forEach { rule ->
                val key = "recurring_${rule.id}_${month}"
                if (prefs.getBoolean(key, false)) return@forEach
                val name = rule.note.ifBlank {
                    data.category(rule.categoryId)?.name
                        ?: context.getString(R.string.notif_recurring_fallback)
                }
                notify(
                    context,
                    key.hashCode(),
                    context.getString(R.string.notif_recurring_title, name),
                    context.getString(
                        R.string.notif_recurring_message,
                        formatMoney(rule.amountCents, currency),
                        context.getString(
                            if (rule.type.name == "INCOME") {
                                R.string.notif_recurring_income
                            } else {
                                R.string.notif_recurring_expense
                            },
                        ),
                    ),
                )
                prefs.edit { putBoolean(key, true) }
            }
        }

        if (data.settings.monthlyReviewAlerts && today.dayOfMonth == 1) {
            val previous = month.minusMonths(1)
            val key = "review_$previous"
            if (!prefs.getBoolean(key, false)) {
                val previousTotals = Finance.totals(Finance.postedTransactionsOfMonth(data, previous, today))
                notify(
                    context,
                    key.hashCode(),
                    context.getString(
                        R.string.notif_review_title,
                        formatMonthLower(previous),
                    ),
                    context.getString(
                        R.string.notif_review_message,
                        formatMoney(previousTotals.balance, currency),
                    ),
                )
                prefs.edit { putBoolean(key, true) }
            }
        }

        if (data.settings.valuationAlerts && data.investments.isNotEmpty() && today.dayOfMonth == 1) {
            val lastValuation = data.investmentEntries
                .filter { it.kind == InvestmentEntryKind.VALUATION || it.kind == InvestmentEntryKind.RETURN }
                .maxOfOrNull { it.dateEpochDay }
                ?.let(LocalDate::ofEpochDay)
            val stale = lastValuation == null || lastValuation.isBefore(today.minusDays(90))
            val key = "valuation_${month}"
            if (stale && !prefs.getBoolean(key, false)) {
                notify(
                    context,
                    key.hashCode(),
                    context.getString(R.string.notif_valuation_title),
                    context.getString(R.string.notif_valuation_message),
                )
                prefs.edit { putBoolean(key, true) }
            }
        }
    }
}

class BudJetDailyWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val data = BudRepository(applicationContext).data.value
            BudJetNotifications.checkAndNotify(applicationContext, data)
        }
        Result.success()
    }
}
