package com.budjet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.CompoundFrequency
import com.budjet.app.data.CompoundResult
import com.budjet.app.data.Currency
import com.budjet.app.data.InvestmentMath
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.MiniBarChart
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.formatMoney
import com.budjet.app.util.parseMoneyInput
import com.budjet.app.util.percentText

@Composable
fun CompoundCalculatorScreen(
    initialAnnualRate: Double?,
    currency: Currency,
    onBack: () -> Unit,
) {
    val colors = BudTheme.colors
    var initial by remember { mutableStateOf("1000") }
    var monthly by remember { mutableStateOf("100") }
    var rate by remember {
        mutableStateOf(
            initialAnnualRate
                ?.takeIf { it.isFinite() && it > 0 }
                ?.let { String.format(java.util.Locale.US, "%.1f", it).replace('.', ',') }
                ?: "7",
        )
    }
    var years by remember { mutableStateOf("10") }
    var frequency by remember { mutableStateOf(CompoundFrequency.MONTHLY) }
    var volatility by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CompoundResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val initialCents = parseMoneyInput(initial)
        val monthlyCents = parseMoneyInput(monthly)
        val annualRate = parsePercent(rate)
        val yearsInt = years.toIntOrNull() ?: 0
        val vol = if (volatility.isBlank()) 0.0 else parsePercent(volatility)
        if (yearsInt !in 1..60) {
            error = "Introduce entre 1 y 60 años."
            result = null
            return
        }
        if (annualRate == null || vol == null) {
            error = "Revisa la rentabilidad y la volatilidad."
            result = null
            return
        }
        error = null
        result = InvestmentMath.compound(
            initialCents = initialCents,
            monthlyCents = monthlyCents,
            annualRatePercent = annualRate,
            years = yearsInt,
            frequency = frequency,
            annualVolatilityPercent = vol,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calculator-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SoftIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    onClick = onBack,
                    size = 38.dp,
                )
                Overline("HERRAMIENTA")
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Interés compuesto.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Juega con el tiempo y la rentabilidad. Es una estimación, no una promesa.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            BudCard {
                CalculatorField(
                    value = initial,
                    onValueChange = { initial = filterMoney(it) },
                    label = "Capital inicial",
                    suffix = currency.symbol,
                )
                Spacer(Modifier.height(10.dp))
                CalculatorField(
                    value = monthly,
                    onValueChange = { monthly = filterMoney(it) },
                    label = "Aportación mensual",
                    suffix = currency.symbol,
                )
                Spacer(Modifier.height(10.dp))
                CalculatorField(
                    value = rate,
                    onValueChange = { rate = filterPercent(it) },
                    label = "Rentabilidad anual",
                    suffix = "%",
                )
                Spacer(Modifier.height(10.dp))
                CalculatorField(
                    value = years,
                    onValueChange = { years = it.filter(Char::isDigit).take(2) },
                    label = "Años",
                    suffix = "años",
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Frecuencia de capitalización",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompoundFrequency.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (frequency == option) colors.primarySoft else Color.Transparent,
                                )
                                .border(
                                    1.dp,
                                    if (frequency == option) colors.primary else colors.line,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable { frequency = option }
                                .padding(horizontal = 13.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (frequency == option) colors.primary else colors.inkMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                CalculatorField(
                    value = volatility,
                    onValueChange = { volatility = filterPercent(it) },
                    label = "Volatilidad anual (opcional)",
                    suffix = "%",
                    modifier = Modifier.testTag("volatility-field"),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cuánto puede variar la rentabilidad de un año a otro. 0 % = cálculo fijo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "Calcular",
                    onClick = { calculate() },
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.expense,
                    )
                }
            }
        }

        result?.let { value ->
            item {
                Spacer(Modifier.height(16.dp))
                ResultCard(
                    result = value,
                    currency = currency,
                )
            }
        }

        item {
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = colors.inkMuted,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (result?.simulated == true) {
                        "Simulación con 1.200 escenarios. Los resultados son probabilísticos y no garantizan rentabilidad futura."
                    } else {
                        "Cálculo determinista. No incluye impuestos, comisiones ni inflación."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun ResultCard(result: CompoundResult, currency: Currency) {
    val colors = BudTheme.colors
    val positive = result.finalCents >= result.contributedCents
    BudCard(modifier = Modifier.testTag("result-card"), color = colors.heroStart, border = false) {
        Overline(if (result.simulated) "ESCENARIO CENTRAL" else "CAPITAL FINAL", color = colors.heroMuted)
        Spacer(Modifier.height(8.dp))
        Text(
            text = formatMoney(if (result.simulated) result.medianCents else result.finalCents, currency),
            style = BudTheme.type.moneyHero.copy(fontSize = 34.sp, lineHeight = 40.sp),
            color = colors.heroText,
            maxLines = 1,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ResultMetric("Aportado", formatMoney(result.contributedCents, currency))
            ResultMetric("Intereses", (if (positive) "+" else "−") + formatMoney(kotlin.math.abs(result.interestCents), currency))
        }
        if (result.simulated) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeChip("Pesimista", result.lowCents, colors.heroMuted, currency, Modifier.weight(1f))
                RangeChip("Central", result.medianCents, colors.heroText, currency, Modifier.weight(1f))
                RangeChip("Optimista", result.highCents, colors.heroMuted, currency, Modifier.weight(1f))
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    BudCard {
        Overline("EVOLUCIÓN")
        Spacer(Modifier.height(12.dp))
        MiniBarChart(
            values = result.yearlyBalances.mapIndexed { index, balance ->
                "${index + 1}" to balance
            },
            color = colors.primary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Valor al final de cada año.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
        )
    }

    if (result.simulated) {
        Spacer(Modifier.height(12.dp))
        BudCard(color = colors.primarySoft, border = false) {
            Text(
                text = "Rango probable: entre ${formatMoney(result.lowCents, currency)} y ${formatMoney(result.highCents, currency)} (P10–P90).",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
            )
        }
    }
}

@Composable
private fun ResultMetric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = BudTheme.colors.heroMuted)
        Text(value, style = BudTheme.type.moneySmall, color = BudTheme.colors.heroText)
    }
}

@Composable
private fun RangeChip(
    label: String,
    value: Long,
    color: Color,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = color)
        Spacer(Modifier.height(2.dp))
        Text(
            text = formatMoney(value, currency),
            style = BudTheme.type.moneySmall.copy(fontSize = 10.sp),
            color = BudTheme.colors.heroText,
            maxLines = 1,
        )
    }
}

@Composable
private fun CalculatorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text("0") },
        suffix = { Text(suffix, color = BudTheme.colors.inkMuted) },
        singleLine = true,
        textStyle = TextStyle(
            color = BudTheme.colors.ink,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
    )
}

private fun filterMoney(text: String): String =
    if (text.length <= 11) text.filter { it.isDigit() || it == ',' } else text

private fun filterPercent(text: String): String =
    if (text.length <= 6) text.filter { it.isDigit() || it == ',' } else text

private fun parsePercent(text: String): Double? =
    if (text.isBlank()) 0.0 else text.replace(',', '.').toDoubleOrNull()
