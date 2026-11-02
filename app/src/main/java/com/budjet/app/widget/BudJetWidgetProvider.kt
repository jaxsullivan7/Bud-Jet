package com.budjet.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.budjet.app.MainActivity
import com.budjet.app.QuickCapture
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.migratedToCurrent
import com.budjet.app.util.formatMoney
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

data class WidgetSnapshot(
    val balanceCents: Long,
    val monthExpenseCents: Long,
    val monthIncomeCents: Long,
    val currencyCode: String,
)

object WidgetData {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun read(context: Context): WidgetSnapshot {
        val file = File(context.filesDir, "budjet.json")
        if (!file.exists()) return WidgetSnapshot(0L, 0L, 0L, "EUR")
        return runCatching {
            val data = json.decodeFromString(AppData.serializer(), file.readText()).migratedToCurrent()
            val today = LocalDate.now()
            val totals = Finance.totals(Finance.postedTransactionsOfMonth(data, YearMonth.from(today), today))
            WidgetSnapshot(
                balanceCents = Finance.totalBalance(data, today),
                monthExpenseCents = totals.expense,
                monthIncomeCents = totals.income,
                currencyCode = data.settings.currencyCode,
            )
        }.getOrElse { WidgetSnapshot(0L, 0L, 0L, "EUR") }
    }
}

class BudJetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views(context)) }
    }

    override fun onEnabled(context: Context) {
        updateAll(context)
    }

    private fun views(context: Context): RemoteViews {
        val snapshot = WidgetData.read(context)
        val currency = Currency.from(snapshot.currencyCode)
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val add = Intent(context, MainActivity::class.java).apply {
            action = QuickCapture.ACTION_QUICK_ADD
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context,
            1,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val addPending = PendingIntent.getActivity(
            context,
            2,
            add,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return RemoteViews(context.packageName, R.layout.widget_budjet).apply {
            setTextViewText(R.id.widget_balance, formatMoney(snapshot.balanceCents, currency))
            setTextViewText(
                R.id.widget_month,
                "Este mes · ${formatMoney(snapshot.monthExpenseCents, currency)} de gastos",
            )
            setOnClickPendingIntent(R.id.widget_brand, openPending)
            setOnClickPendingIntent(R.id.widget_balance, openPending)
            setOnClickPendingIntent(R.id.widget_add, addPending)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            runCatching {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, BudJetWidgetProvider::class.java))
                if (ids.isEmpty()) return
                val views = BudJetWidgetProvider().views(context)
                ids.forEach { manager.updateAppWidget(it, views) }
            }
        }
    }
}
