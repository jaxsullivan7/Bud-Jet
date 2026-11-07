package com.budjet.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.budjet.app.data.Account
import com.budjet.app.data.Category
import com.budjet.app.data.Investment
import com.budjet.app.data.Liability
import com.budjet.app.data.SavingsGoal
import com.budjet.app.util.builtInAccountNameRes
import com.budjet.app.util.builtInCategoryNameRes
import com.budjet.app.util.builtInGoalNameRes
import com.budjet.app.util.builtInInvestmentNameRes
import com.budjet.app.util.builtInLiabilityNameRes
import com.budjet.app.util.builtInNoteRes

@Composable
fun Category.displayName(): String =
    if (customName) name else builtInCategoryNameRes(id)?.let { stringResource(it) } ?: name

@Composable
fun Account.displayName(): String =
    if (customName) name else builtInAccountNameRes(id)?.let { stringResource(it) } ?: name

@Composable
fun Investment.displayName(): String =
    if (customName) name else builtInInvestmentNameRes(id)?.let { stringResource(it) } ?: name

@Composable
fun SavingsGoal.displayName(): String =
    if (customName) name else builtInGoalNameRes(id)?.let { stringResource(it) } ?: name

@Composable
fun Liability.displayName(): String =
    if (customName) name else builtInLiabilityNameRes(id)?.let { stringResource(it) } ?: name

@Composable
fun String.displayNote(): String =
    builtInNoteRes(this)?.let { stringResource(it) } ?: this
