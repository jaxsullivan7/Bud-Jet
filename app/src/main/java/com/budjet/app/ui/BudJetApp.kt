package com.budjet.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.budjet.app.R
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budjet.app.MainActivity
import com.budjet.app.QuickCaptureRequest
import com.budjet.app.data.AppData
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.budjet.app.data.movementDateOrDefault
import com.budjet.app.ui.components.AppTab
import com.budjet.app.ui.components.BudBottomBar
import com.budjet.app.ui.screens.AnalysisScreen
import com.budjet.app.ui.screens.BudgetsScreen
import com.budjet.app.ui.screens.CategoryDetailScreen
import com.budjet.app.ui.screens.GoalsScreen
import com.budjet.app.ui.screens.HomeScreen
import com.budjet.app.ui.screens.ImportScreen
import com.budjet.app.ui.screens.LockScreen
import com.budjet.app.ui.screens.PatrimonioScreen
import com.budjet.app.ui.screens.PortfolioScreen
import com.budjet.app.ui.screens.RecurringScreen
import com.budjet.app.ui.screens.SettingsScreen
import com.budjet.app.ui.screens.TransactionEditorScreen
import com.budjet.app.ui.screens.TransactionsScreen
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.BiometricAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun BudJetApp(
    viewModel: AppViewModel,
    data: AppData,
) {
    val colors = BudTheme.colors
    val msgNotifOn = stringResource(R.string.snackbar_notifications_on)
    val msgNotifAndroid = stringResource(R.string.snackbar_notifications_android)
    val unlockTitle = stringResource(R.string.unlock_title)
    val unlockSubtitle = stringResource(R.string.unlock_subtitle)
    val msgMovementDeleted = stringResource(R.string.snackbar_movement_deleted)
    val labelUndo = stringResource(R.string.action_undo)
    val msgAuthFailed = stringResource(R.string.snackbar_auth_open_failed)
    val biometricTitle = stringResource(R.string.biometric_enable_title)
    val biometricSubtitle = stringResource(R.string.biometric_subtitle)
    val msgBiometricEnabled = stringResource(R.string.snackbar_biometric_enabled)
    val testUnlockTitle = stringResource(R.string.settings_test_unlock)
    val msgUnlockVerified = stringResource(R.string.snackbar_unlock_verified)
    val msgAutoBackupCreated = stringResource(R.string.settings_auto_backup_created)
    val msgBackupFailed = stringResource(R.string.snackbar_backup_failed)
    val exportTitle = stringResource(R.string.settings_export_transactions)
    val msgFileFailed = stringResource(R.string.snackbar_file_failed)
    val msgDemoRestored = stringResource(R.string.snackbar_demo_restored)
    val msgFreshStarted = stringResource(R.string.snackbar_fresh_started)
    val msgGoalAdded = stringResource(R.string.snackbar_goal_added)
    val msgGoalWithdrawn = stringResource(R.string.snackbar_goal_withdrawn)
    val msgMovementAdded = stringResource(R.string.snackbar_movement_added)
    val msgMovementUpdated = stringResource(R.string.snackbar_movement_updated)
    val month by viewModel.month.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var goalsOpen by rememberSaveable { mutableStateOf(false) }
    var recurringOpen by rememberSaveable { mutableStateOf(false) }
    var portfolioOpen by rememberSaveable { mutableStateOf(false) }
    var patrimonioOpen by rememberSaveable { mutableStateOf(false) }
    var importOpen by rememberSaveable { mutableStateOf(false) }
    var editorDraft by remember { mutableStateOf<EditorDraft?>(null) }
    var editorVisible by remember { mutableStateOf(false) }
    var categoryDetail by remember { mutableStateOf<Category?>(null) }
    var categoryVisible by remember { mutableStateOf(false) }
    var locked by remember {
        mutableStateOf(data.settings.biometricLock && !viewModel.sessionUnlocked)
    }
    var lockError by remember { mutableStateOf<String?>(null) }
    var authenticating by remember { mutableStateOf(false) }
    var backgroundedAt by remember { mutableStateOf(android.os.SystemClock.elapsedRealtime()) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scope.launch {
            snackbar.showSnackbar(
                if (granted) msgNotifOn else msgNotifAndroid,
            )
        }
    }

    LaunchedEffect(data.settings.notificationsEnabled, data.settings.notificationPermissionAsked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            data.settings.notificationsEnabled &&
            !data.settings.notificationPermissionAsked
        ) {
            viewModel.markNotificationPermissionAsked()
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(editorVisible) {
        if (!editorVisible) {
            delay(340)
            editorDraft = null
        }
    }
    LaunchedEffect(categoryVisible) {
        if (!categoryVisible) {
            delay(340)
            categoryDetail = null
        }
    }

    fun requestUnlock() {
        val host = activity ?: return
        lockError = null
        authenticating = true
        BiometricAuth.request(
            activity = host,
            title = unlockTitle,
            subtitle = unlockSubtitle,
            onSuccess = {
                authenticating = false
                locked = false
                viewModel.sessionUnlocked = true
                backgroundedAt = android.os.SystemClock.elapsedRealtime()
            },
            onError = { message ->
                authenticating = false
                lockError = message
            },
        )
    }

    LaunchedEffect(locked) {
        if (locked) requestUnlock()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lockEnabledNow by androidx.compose.runtime.rememberUpdatedState(data.settings.biometricLock)
    val authenticatingNow by androidx.compose.runtime.rememberUpdatedState(authenticating)

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    if (lockEnabledNow && !authenticatingNow) {
                        backgroundedAt = android.os.SystemClock.elapsedRealtime()
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    val awayFor = android.os.SystemClock.elapsedRealtime() - backgroundedAt
                    if (lockEnabledNow && awayFor in 30_000..Long.MAX_VALUE && !authenticatingNow) {
                        locked = true
                        viewModel.sessionUnlocked = false
                    }
                    backgroundedAt = android.os.SystemClock.elapsedRealtime()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openCategory(category: Category) {
        categoryDetail = category
        categoryVisible = true
    }

    fun openEditor(draft: EditorDraft) {
        editorDraft = draft
        editorVisible = true
    }

    fun newDraft(): EditorDraft {
        val firstExpense = data.categories.firstOrNull { it.type == TxType.EXPENSE && !it.archived }
        val date = data.settings.movementDateOrDefault(
            fallback = if (month == YearMonth.now()) {
                LocalDate.now()
            } else {
                month.atDay(minOf(LocalDate.now().dayOfMonth, month.lengthOfMonth()))
            },
        )
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
        reversalOfId = transaction.reversalOfId,
    )

    val hostActivity = context as? MainActivity
    LaunchedEffect(hostActivity) {
        hostActivity?.quickCapture?.collect { request ->
            if (request != null) {
                val draft = newDraft().copy(
                    amountCents = request.amountCents ?: 0L,
                    note = request.note,
                )
                openEditor(draft)
                hostActivity.clearQuickCapture()
            }
        }
    }

    fun deleteTransaction(id: String) {
        val transaction = data.transactions.firstOrNull { it.id == id } ?: return
        viewModel.deleteTransaction(transaction)
        editorVisible = false
        scope.launch {
            val result = snackbar.showSnackbar(
                message = msgMovementDeleted,
                actionLabel = labelUndo,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTransaction(transaction)
            }
        }
    }

    BackHandler(enabled = locked) { }
    BackHandler(enabled = editorVisible || goalsOpen || recurringOpen || portfolioOpen || patrimonioOpen || importOpen || categoryVisible || settingsOpen) {
        when {
            editorVisible -> editorVisible = false
            categoryVisible -> categoryVisible = false
            goalsOpen -> goalsOpen = false
            recurringOpen -> recurringOpen = false
            portfolioOpen -> portfolioOpen = false
            patrimonioOpen -> patrimonioOpen = false
            importOpen -> importOpen = false
            settingsOpen -> settingsOpen = false
        }
    }
    BackHandler(
        enabled = !locked && !editorVisible && !goalsOpen && !recurringOpen && !portfolioOpen && !patrimonioOpen &&
            !importOpen && !categoryVisible && !settingsOpen && tab != AppTab.HOME,
    ) {
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
                        onSeeGoals = { goalsOpen = true },
                        onOpenTransaction = { openEditor(draftFor(it)) },
                    )

                    AppTab.TRANSACTIONS -> TransactionsScreen(
                        data = data,
                        month = month,
                        onPreviousMonth = viewModel::previousMonth,
                        onNextMonth = viewModel::nextMonth,
                        onOpenTransaction = { openEditor(draftFor(it)) },
                        onChangeTransactionsDate = viewModel::updateTransactionsDate,
                        onDeleteTransactions = viewModel::deleteTransactions,
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
                        onOpenCategory = { openCategory(it) },
                        onSetStartDate = viewModel::setAnalysisStart,
                        onOpenPortfolio = { portfolioOpen = true },
                        onOpenPatrimonio = { patrimonioOpen = true },
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
                    onSetPalette = viewModel::setPalette,
                    onAddCategory = viewModel::addCategory,
                    onOpenGoals = { goalsOpen = true },
                    onOpenRecurring = { recurringOpen = true },
                    onSetBiometric = { enabled ->
                        if (!enabled) {
                            viewModel.setBiometricLock(false)
                        } else {
                            val host = activity
                            when {
                                host == null -> scope.launch {
                                    snackbar.showSnackbar(msgAuthFailed)
                                }
                                !BiometricAuth.canAuthenticate(host) -> scope.launch {
                                    snackbar.showSnackbar(BiometricAuth.unavailableReason(host))
                                }
                                else -> {
                                    authenticating = true
                                    BiometricAuth.request(
                                        activity = host,
                                        title = biometricTitle,
                                        subtitle = biometricSubtitle,
                                        onSuccess = {
                                            authenticating = false
                                            viewModel.setBiometricLock(true)
                                            backgroundedAt = android.os.SystemClock.elapsedRealtime()
                                            scope.launch { snackbar.showSnackbar(msgBiometricEnabled) }
                                        },
                                        onError = { message ->
                                            authenticating = false
                                            scope.launch { snackbar.showSnackbar(message) }
                                        },
                                    )
                                }
                            }
                        }
                    },
                    onTestBiometric = {
                        val host = activity
                        when {
                            host == null -> scope.launch {
                                snackbar.showSnackbar(msgAuthFailed)
                            }
                            !BiometricAuth.canAuthenticate(host) -> scope.launch {
                                snackbar.showSnackbar(BiometricAuth.unavailableReason(host))
                            }
                            else -> {
                                authenticating = true
                                BiometricAuth.request(
                                    activity = host,
                                    title = testUnlockTitle,
                                    subtitle = biometricSubtitle,
                                    onSuccess = {
                                        authenticating = false
                                        scope.launch { snackbar.showSnackbar(msgUnlockVerified) }
                                    },
                                    onError = { message ->
                                        authenticating = false
                                        scope.launch { snackbar.showSnackbar(message) }
                                    },
                                )
                            }
                        }
                    },
                    onBuildBackup = viewModel::buildBackup,
                    onValidateBackup = viewModel::parseBackup,
                    onRestoreBackup = viewModel::restoreBackup,
                    onOpenImport = {
                        settingsOpen = false
                        importOpen = true
                    },
                    onBuildEncryptedBackup = viewModel::buildEncryptedBackup,
                    onValidateEncryptedBackup = viewModel::parseEncryptedBackup,
                    onListAutoBackups = { viewModel.autoBackups(context) },
                    onRestoreAutoBackup = { name -> viewModel.restoreAutoBackup(context, name) },
                    onCreateAutoBackup = {
                        val result = viewModel.createAutoBackupNow(context)
                        scope.launch {
                            snackbar.showSnackbar(
                                if (result.isSuccess) msgAutoBackupCreated else msgBackupFailed,
                            )
                        }
                    },
                    onSetNotificationsEnabled = viewModel::setNotificationsEnabled,
                    onSetBudgetAlerts = viewModel::setBudgetAlerts,
                    onSetRecurringAlerts = viewModel::setRecurringAlerts,
                    onSetMonthlyReviewAlerts = viewModel::setMonthlyReviewAlerts,
                    onSetValuationAlerts = viewModel::setValuationAlerts,
                    onSetAutoBackupEnabled = viewModel::setAutoBackupEnabled,
                    onAddCategoryRule = viewModel::addCategoryRule,
                    onDeleteCategoryRule = viewModel::deleteCategoryRule,
                    onNotify = { message ->
                        scope.launch { snackbar.showSnackbar(message) }
                    },
                    onSetOpeningBalance = viewModel::setOpeningBalance,
                    onExport = {
                        val intent = viewModel.buildExport(context, data)
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, exportTitle))
                        } else {
                            scope.launch { snackbar.showSnackbar(msgFileFailed) }
                        }
                    },
                    onResetDemo = {
                        viewModel.resetDemoData()
                        tab = AppTab.HOME
                        scope.launch { snackbar.showSnackbar(msgDemoRestored) }
                    },
                    onStartFresh = {
                        viewModel.startFresh()
                        scope.launch { snackbar.showSnackbar(msgFreshStarted) }
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = categoryVisible,
            enter = slideInHorizontally(tween(320)) { it } + fadeIn(tween(220)),
            exit = slideOutHorizontally(tween(280)) { it } + fadeOut(tween(160)),
        ) {
            val category = categoryDetail
            if (category != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .statusBarsPadding(),
                ) {
                    CategoryDetailScreen(
                        data = data,
                        category = category,
                        month = month,
                        onClose = { categoryVisible = false },
                        onOpenTransaction = { openEditor(draftFor(it)) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = goalsOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                GoalsScreen(
                    data = data,
                    onClose = { goalsOpen = false },
                    onSaveGoal = viewModel::saveGoal,
                    onDeleteGoal = viewModel::deleteGoal,
                    onAdjustGoal = { id, delta ->
                        viewModel.adjustGoal(id, delta)
                        scope.launch {
                            snackbar.showSnackbar(
                                if (delta > 0) msgGoalAdded else msgGoalWithdrawn,
                            )
                        }
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = recurringOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                RecurringScreen(
                    data = data,
                    onClose = { recurringOpen = false },
                    onSave = viewModel::saveRecurring,
                    onDelete = viewModel::deleteRecurring,
                    onToggle = viewModel::setRecurringActive,
                )
            }
        }

        AnimatedVisibility(
            visible = portfolioOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                PortfolioScreen(
                    data = data,
                    onClose = { portfolioOpen = false },
                    onAddInvestment = viewModel::addInvestment,
                    onUpdateInvestment = viewModel::updateInvestment,
                    onDeleteInvestment = viewModel::deleteInvestment,
                    onAddEntry = viewModel::addInvestmentEntry,
                )
            }
        }

        AnimatedVisibility(
            visible = patrimonioOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                PatrimonioScreen(
                    data = data,
                    onClose = { patrimonioOpen = false },
                    onAddTransfer = viewModel::addTransfer,
                    onSaveLiability = viewModel::saveLiability,
                    onDeleteLiability = viewModel::deleteLiability,
                    onPayLiability = viewModel::payLiability,
                    onOpenPortfolio = {
                        patrimonioOpen = false
                        portfolioOpen = true
                    },
                )
            }
        }

        AnimatedVisibility(
            visible = importOpen,
            enter = slideInVertically(tween(320)) { it / 8 } + fadeIn(tween(240)),
            exit = slideOutVertically(tween(260)) { it / 8 } + fadeOut(tween(180)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .statusBarsPadding(),
            ) {
                ImportScreen(
                    data = data,
                    onClose = { importOpen = false },
                    onImport = { rows, accountId ->
                        viewModel.importTransactions(rows, accountId)
                        scope.launch { snackbar.showSnackbar(
                            context.resources.getQuantityString(
                                R.plurals.snackbar_imported,
                                rows.size,
                                rows.size,
                            ),
                        ) }
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
                            scope.launch { snackbar.showSnackbar(if (updated.id == null) msgMovementAdded else msgMovementUpdated) }
                        },
                        onDelete = { id -> deleteTransaction(id) },
                    )
                }
            }
        }

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
            visible = locked,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(220)),
        ) {
            LockScreen(
                name = data.settings.name,
                error = lockError,
                authenticating = authenticating,
                onUnlock = { requestUnlock() },
            )
        }
    }
}
