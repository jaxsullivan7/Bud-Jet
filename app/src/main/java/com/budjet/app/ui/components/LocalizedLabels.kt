package com.budjet.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.budjet.app.R
import com.budjet.app.data.CompoundFrequency
import com.budjet.app.data.DebtStrategy
import com.budjet.app.data.InvestmentEntryKind
import com.budjet.app.data.InvestmentKind
import com.budjet.app.data.LiabilityKind

@Composable
fun InvestmentKind.localizedLabel(): String = stringResource(
    when (this) {
        InvestmentKind.INDEX_FUND -> R.string.investment_kind_index_fund
        InvestmentKind.STOCK -> R.string.investment_kind_stock
        InvestmentKind.ETF -> R.string.investment_kind_etf
        InvestmentKind.CRYPTO -> R.string.investment_kind_crypto
        InvestmentKind.DEPOSIT -> R.string.investment_kind_deposit
        InvestmentKind.PENSION -> R.string.investment_kind_pension
        InvestmentKind.REAL_ESTATE -> R.string.investment_kind_real_estate
        InvestmentKind.OTHER -> R.string.investment_kind_other
    },
)

@Composable
fun InvestmentEntryKind.localizedLabel(): String = stringResource(
    when (this) {
        InvestmentEntryKind.CONTRIBUTION -> R.string.investment_entry_contribution
        InvestmentEntryKind.WITHDRAWAL -> R.string.investment_entry_withdrawal
        InvestmentEntryKind.RETURN -> R.string.investment_entry_return
        InvestmentEntryKind.VALUATION -> R.string.investment_entry_valuation
    },
)

@Composable
fun LiabilityKind.localizedLabel(): String = stringResource(
    when (this) {
        LiabilityKind.MORTGAGE -> R.string.liability_kind_mortgage
        LiabilityKind.LOAN -> R.string.liability_kind_loan
        LiabilityKind.CARD -> R.string.liability_kind_card
        LiabilityKind.OTHER -> R.string.liability_kind_other
    },
)

@Composable
fun CompoundFrequency.localizedLabel(): String = stringResource(
    when (this) {
        CompoundFrequency.MONTHLY -> R.string.compound_monthly
        CompoundFrequency.ANNUAL -> R.string.compound_annual
    },
)

@Composable
fun DebtStrategy.localizedLabel(): String = stringResource(
    when (this) {
        DebtStrategy.SNOWBALL -> R.string.debt_strategy_snowball
        DebtStrategy.AVALANCHE -> R.string.debt_strategy_avalanche
    },
)
