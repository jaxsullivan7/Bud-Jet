package com.budjet.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import com.budjet.app.MainActivity
import com.budjet.app.QuickAddActivity
import com.budjet.app.R
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Finance
import com.budjet.app.data.ThemeMode
import com.budjet.app.data.ThemePalette
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
    val palette: ThemePalette,
    val dark: Boolean,
)

object WidgetData {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun read(context: Context): WidgetSnapshot {
        val file = File(context.filesDir, "budjet.json")
        val fallback = WidgetSnapshot(0L, 0L, 0L, "EUR", ThemePalette.BUSINESS, false)
        if (!file.exists()) return fallback
        return runCatching {
            val data = json.decodeFromString(AppData.serializer(), file.readText()).migratedToCurrent()
            val today = LocalDate.now()
            val totals = Finance.totals(Finance.postedTransactionsOfMonth(data, YearMonth.from(today), today))
            WidgetSnapshot(
                balanceCents = Finance.totalBalance(data, today),
                monthExpenseCents = totals.expense,
                monthIncomeCents = totals.income,
                currencyCode = data.settings.currencyCode,
                palette = data.settings.palette,
                dark = data.settings.themeMode == ThemeMode.DARK ||
                    (data.settings.themeMode == ThemeMode.SYSTEM &&
                        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES),
            )
        }.getOrElse { fallback }
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
        val palette = widgetColors(snapshot)
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val add = Intent(context, QuickAddActivity::class.java).apply {
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
            setInt(R.id.widget_root, "setBackgroundResource", palette.background)
            setTextColor(R.id.widget_brand, palette.text)
            setTextColor(R.id.widget_add, palette.text)
            setTextColor(R.id.widget_balance, palette.text)
            setTextColor(R.id.widget_month, palette.muted)
            setTextViewText(R.id.widget_balance, formatMoney(snapshot.balanceCents, currency))
            setTextViewText(
                R.id.widget_month,
                context.getString(
                    R.string.widget_month_expense,
                    formatMoney(snapshot.monthExpenseCents, currency),
                ),
            )
            setOnClickPendingIntent(R.id.widget_brand, openPending)
            setOnClickPendingIntent(R.id.widget_balance, openPending)
            setOnClickPendingIntent(R.id.widget_add, addPending)
        }
    }

    private data class WidgetColors(val background: Int, val text: Int, val muted: Int)

    private fun widgetColors(snapshot: WidgetSnapshot): WidgetColors = when (snapshot.palette) {
        ThemePalette.BUSINESS -> if (snapshot.dark) {
            WidgetColors(R.drawable.widget_business_dark, 0xFFEDF0E6.toInt(), 0xFF9FB3A8.toInt())
        } else {
            WidgetColors(R.drawable.widget_business_light, 0xFFF6F1E5.toInt(), 0xFFC9D8CE.toInt())
        }
        ThemePalette.NAVY -> if (snapshot.dark) {
            WidgetColors(R.drawable.widget_navy_dark, 0xFFF1EDE4.toInt(), 0xFF9FB0C4.toInt())
        } else {
            WidgetColors(R.drawable.widget_navy_light, 0xFFF8F3E8.toInt(), 0xFFC3CEDD.toInt())
        }
        ThemePalette.BOSS -> if (snapshot.dark) {
            WidgetColors(R.drawable.widget_boss_dark, 0xFFF3EDE8.toInt(), 0xFFB9A7AB.toInt())
        } else {
            WidgetColors(R.drawable.widget_boss_light, 0xFFF4EFE9.toInt(), 0xFFBEB1B3.toInt())
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
