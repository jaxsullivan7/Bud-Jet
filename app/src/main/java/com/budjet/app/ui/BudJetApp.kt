package com.budjet.app.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budjet.app.data.AppData
import com.budjet.app.data.Currency
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.AppTab
import com.budjet.app.ui.components.BudBottomBar
import com.budjet.app.ui.screens.AnalysisScreen
import com.budjet.app.ui.screens.BudgetsScreen
import com.budjet.app.ui.screens.HomeScreen
import com.budjet.app.ui.screens.SettingsScreen
import com.budjet.app.ui.screens.TransactionEditorScreen
import com.budjet.app.ui.screens.TransactionsScreen
import com.budjet.app.ui.theme.BudTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun BudJetApp(
    viewModel: AppViewModel,
    data: AppData,
) {
    val colors = BudTheme.colors
    val month by viewModel.month.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var editorDraft by remember { mutableStateOf<EditorDraft?>(null) }
    var editorVisible by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(editorVisible) {
        if (!editorVisible) {
            delay(340)
            editorDraft = null
        }
    }

    fun openEditor(draft: EditorDraft) {
        editorDraft = draft
        editorVisible = true
    }

    fun newDraft(): EditorDraft {
        val firstExpense = data.categories.firstOrNull { it.type == TxType.EXPENSE && !it.archived }
        val date = if (month == YearMonth.now()) {
            LocalDate.now()
        } else {
            month.atDay(minOf(LocalDate.now().dayOfMonth, month.lengthOfMonth()))
        }
        return EditorDraft(
            type = TxType.EXPENSE,
            categoryId = firstExpense?.id ?: "",
            accountId = data.accounts.firstOrNull()?.id ?: "",
            date = date,
        )
    }

    fun draftFor(transaction: Transaction) = EditorDraft(
        id = transaction.id,
        amountCents = transaction.amountCents,
        type = transaction.type,
        categoryId = transaction.categoryId,
        accountId = transaction.accountId,
        date = LocalDate.ofEpochDay(transaction.dateEpochDay),
        note = transaction.note,
    )

    fun deleteTransaction(id: String) {
        val transaction = data.transactions.firstOrNull { it.id == id } ?: return
        viewModel.deleteTransaction(transaction)
        editorVisible = false
        scope.launch {
            val result = snackbar.showSnackbar(
                message = "Movimiento eliminado",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTransaction(transaction)
            }
        }
    }

    BackHandler(enabled = editorVisible || settingsOpen) {
        if (editorVisible) editorVisible = false else settingsOpen = false
    }
    BackHandler(enabled = !editorVisible && !settingsOpen && tab != AppTab.HOME) {
        tab = AppTab.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                fadeIn(tween(220)) togetherWith fadeOut(tween(140))
            },
            label = "tabs",
        ) { target ->
            Box(Modifier.fillMaxSize().statusBarsPadding()) {
                when (target) {
                    AppTab.HOME -> HomeScreen(
                        data = data,
                        month = month,
                        onOpenSettings = { settingsOpen = true },
                        onSeeTransactions = { tab = AppTab.TRANSACTIONS },
                        onSeeBudgets = { tab = AppTab.BUDGETS },
                        onSeeAnalysis = { tab = AppTab.ANALYSIS },
                        onOpenTransaction = { openEditor(draftFor(it)) },
                    )

                    AppTab.TRANSACTIONS -> TransactionsScreen(
                        data = data,
                        month = month,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                        onOpenTransaction = { openEditor(draftFor(it)) },
                    )

                    AppTab.BUDGETS -> BudgetsScreen(
                        data = data,
                        month = month,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                        onSetBudget = viewModel::setBudget,
                    )

                    AppTab.ANALYSIS -> AnalysisScreen(
                        data = data,
                        month = month,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(146.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            colors.background.copy(alpha = 0f),
                            colors.background.copy(alpha = 0.85f),
                            colors.background,
                        ),
                    ),
                ),
        )

        BudBottomBar(
            selected = tab,
            onSelect = { tab = it },
            onAdd = { openEditor(newDraft()) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 100.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = colors.ink,
                contentColor = colors.background,
                actionColor = colors.primary,
                shape = MaterialTheme.shapes.medium,
            )
        }

        AnimatedVisibility(
            visible = settingsOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                SettingsScreen(
                    data = data,
                    onClose = { settingsOpen = false },
                    onSetName = viewModel::setName,
                    onSetCurrency = viewModel::setCurrency,
                    onSetTheme = viewModel::setTheme,
                    onAddCategory = viewModel::addCategory,
                    onExport = {
                        val intent = viewModel.buildExport(context, data)
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, "Exportar movimientos"))
                        } else {
                            scope.launch { snackbar.showSnackbar("No se pudo crear el archivo") }
                        }
                    },
                    onResetDemo = {
                        viewModel.resetDemoData()
                        tab = AppTab.HOME
                        scope.launch { snackbar.showSnackbar("Datos de ejemplo restaurados") }
                    },
                    onClearTransactions = {
                        viewModel.clearEverything()
                        scope.launch { snackbar.showSnackbar("Movimientos borrados") }
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = editorVisible,
            enter = slideInVertically(tween(320)) { it } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(280)) { it } + fadeOut(tween(160)),
        ) {
            val draft = editorDraft
            if (draft != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .statusBarsPadding(),
                ) {
                    TransactionEditorScreen(
                        data = data,
                        initial = draft,
                        onClose = { editorVisible = false },
                        onSave = { updated ->
                            viewModel.saveTransaction(updated)
                            editorVisible = false
                            scope.launch { snackbar.showSnackbar(if (updated.id == null) "Movimiento añadido" else "Movimiento actualizado") }
                        },
                        onDelete = { id -> deleteTransaction(id) },
                    )
                }
            }
        }
    }
}
