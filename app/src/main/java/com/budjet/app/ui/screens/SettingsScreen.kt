package com.budjet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Euro
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationImportant
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.BuildConfig
import com.budjet.app.R
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
import com.budjet.app.data.AutoBackupInfo
import com.budjet.app.data.Backup
import com.budjet.app.data.Category
import com.budjet.app.data.Currency
import com.budjet.app.data.ThemeMode
import com.budjet.app.data.ThemePalette
import com.budjet.app.data.TxType
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.BudSwitch
import com.budjet.app.ui.components.InitialsAvatar
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.SectionHeader
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.components.categoryColor
import com.budjet.app.ui.components.categoryIcon
import com.budjet.app.ui.components.iconChoices
import com.budjet.app.ui.components.parseHex
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.ui.theme.BossLight
import com.budjet.app.ui.theme.CategoryPalette
import com.budjet.app.ui.theme.NavyLight
import com.budjet.app.ui.theme.PaperLight
import com.budjet.app.util.centsToInput
import com.budjet.app.util.filterAmountInput
import com.budjet.app.util.formatMoney
import com.budjet.app.util.formatShortDate
import com.budjet.app.util.parseMoneyInput
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    data: AppData,
    onClose: () -> Unit,
    onSetName: (String) -> Unit,
    onSetCurrency: (String) -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetPalette: (ThemePalette) -> Unit,
    onAddCategory: (name: String, icon: String, colorHex: String, type: TxType) -> Unit,
    onOpenGoals: () -> Unit,
    onOpenRecurring: () -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onTestBiometric: () -> Unit,
    onBuildBackup: () -> String,
    onValidateBackup: (String) -> Result<AppData>,
    onRestoreBackup: (AppData) -> Unit,
    onOpenImport: () -> Unit,
    onBuildEncryptedBackup: (String) -> ByteArray,
    onValidateEncryptedBackup: (ByteArray, String) -> Result<AppData>,
    onListAutoBackups: () -> List<AutoBackupInfo>,
    onRestoreAutoBackup: (String) -> Result<AppData>,
    onCreateAutoBackup: () -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSetBudgetAlerts: (Boolean) -> Unit,
    onSetRecurringAlerts: (Boolean) -> Unit,
    onSetMonthlyReviewAlerts: (Boolean) -> Unit,
    onSetValuationAlerts: (Boolean) -> Unit,
    onSetAutoBackupEnabled: (Boolean) -> Unit,
    onAddCategoryRule: (String, String) -> Unit,
    onDeleteCategoryRule: (String) -> Unit,
    onNotify: (String) -> Unit,
    onSetOpeningBalance: (Long) -> Unit,
    onExport: () -> Unit,
    onResetDemo: () -> Unit,
    onStartFresh: () -> Unit,
) {
    val colors = BudTheme.colors
    val context = LocalContext.current
    var nameDialog by remember { mutableStateOf(false) }
    var currencyDialog by remember { mutableStateOf(false) }
    var categoryDialog by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var confirmFresh by remember { mutableStateOf(false) }
    var openingDialog by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<AppData?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }
    var encryptedExportDialog by remember { mutableStateOf(false) }
    var encryptedImportDialog by remember { mutableStateOf(false) }
    var encryptedPassword by remember { mutableStateOf("") }
    var pendingEncryptedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var autoBackupsDialog by remember { mutableStateOf(false) }
    var rulesDialog by remember { mutableStateOf(false) }
    val currency = Currency.from(data.settings.currencyCode)
    val errorOpenFile = stringResource(R.string.settings_error_open_file)
    val errorFileTooLarge = stringResource(R.string.settings_error_file_too_large)
    val errorReadFile = stringResource(R.string.settings_error_read_file)
    val errorInvalidBackup = stringResource(R.string.settings_error_invalid_backup)
    val errorSaveBackup = stringResource(R.string.settings_error_save_backup)
    val errorSaveEncrypted = stringResource(R.string.settings_error_save_encrypted)
    val errorOpenEncrypted = stringResource(R.string.settings_error_open_encrypted)
    val errorOpenBackup = stringResource(R.string.settings_error_open_backup)
    val notifyBackupSaved = stringResource(R.string.settings_backup_saved)
    val notifyEncryptedSaved = stringResource(R.string.settings_encrypted_backup_saved)
    val notifyAutoBackupCreated = stringResource(R.string.settings_auto_backup_created)
    val notifyRestored = stringResource(R.string.settings_backup_restored)
    val backupFilePrefix = stringResource(R.string.settings_backup_file_prefix)
    val encryptedFilePrefix = stringResource(R.string.settings_encrypted_file_prefix)

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(onBuildBackup().toByteArray(Charsets.UTF_8))
                } ?: error(errorOpenFile)
            }.onSuccess {
                onNotify(notifyBackupSaved)
            }.onFailure {
                onNotify(errorSaveBackup)
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                val declaredSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
                if (declaredSize > Backup.MAX_BYTES) error(errorFileTooLarge)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error(errorReadFile)
                if (bytes.size > Backup.MAX_BYTES) error(errorFileTooLarge)
                String(bytes, Charsets.UTF_8)
            }.getOrElse {
                importError = it.message ?: errorReadFile
                null
            }
            if (text != null) {
                onValidateBackup(text)
                    .onSuccess { pendingRestore = it }
                    .onFailure { importError = it.message ?: errorInvalidBackup }
            }
        }
    }

    val encryptedExportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri != null && encryptedPassword.isNotBlank()) {
            runCatching {
                val bytes = onBuildEncryptedBackup(encryptedPassword)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error(errorOpenFile)
            }.onSuccess {
                onNotify(notifyEncryptedSaved)
            }.onFailure {
                onNotify(errorSaveEncrypted)
            }
            encryptedPassword = ""
        }
    }

    val encryptedImportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error(errorReadFile)
            }.getOrElse {
                importError = it.message ?: errorReadFile
                null
            }
            if (bytes != null) {
                pendingEncryptedBytes = bytes
                encryptedImportDialog = true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 40.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SoftIconButton(
                    icon = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline(stringResource(R.string.settings_overline))
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(22.dp))
        }

        item {
            BudCard(onClick = { nameDialog = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = data.settings.name, size = 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = data.settings.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.ink,
                        )
                        Text(
                            text = stringResource(R.string.settings_profile_local),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_appearance))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 6.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeOption(
                        label = stringResource(R.string.theme_light),
                        icon = Icons.Rounded.LightMode,
                        selected = data.settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onSetTheme(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_dark),
                        icon = Icons.Rounded.DarkMode,
                        selected = data.settings.themeMode == ThemeMode.DARK,
                        onClick = { onSetTheme(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_system),
                        icon = Icons.Rounded.SettingsBrightness,
                        selected = data.settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onSetTheme(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BudCard(padding = 14.dp) {
                Overline(stringResource(R.string.overline_palette))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePalette.entries.forEach { palette ->
                        PaletteOption(
                            palette = palette,
                            selected = data.settings.palette == palette,
                            onClick = { onSetPalette(palette) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_preferences))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.Euro,
                    title = stringResource(R.string.label_currency),
                    subtitle = "${currency.symbol} · ${currency.code}",
                    onClick = { currencyDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = stringResource(R.string.settings_export_transactions),
                    subtitle = stringResource(R.string.settings_export_transactions_sub),
                    onClick = onExport,
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_notifications))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SwitchRow(
                    icon = Icons.Rounded.NotificationImportant,
                    title = stringResource(R.string.settings_reminders),
                    subtitle = stringResource(R.string.settings_reminders_sub),
                    checked = data.settings.notificationsEnabled,
                    onCheckedChange = onSetNotificationsEnabled,
                )
                if (data.settings.notificationsEnabled) {
                    SettingsDivider()
                    SwitchRow(
                        icon = Icons.Rounded.NotificationImportant,
                        title = stringResource(R.string.settings_budget_alerts),
                        subtitle = stringResource(R.string.settings_budget_alerts_sub),
                        checked = data.settings.budgetAlerts,
                        onCheckedChange = onSetBudgetAlerts,
                    )
                    SettingsDivider()
                    SwitchRow(
                        icon = Icons.Rounded.EventRepeat,
                        title = stringResource(R.string.settings_recurring_alerts),
                        subtitle = stringResource(R.string.settings_recurring_alerts_sub),
                        checked = data.settings.recurringAlerts,
                        onCheckedChange = onSetRecurringAlerts,
                    )
                    SettingsDivider()
                    SwitchRow(
                        icon = Icons.Rounded.History,
                        title = stringResource(R.string.settings_monthly_review),
                        subtitle = stringResource(R.string.settings_monthly_review_sub),
                        checked = data.settings.monthlyReviewAlerts,
                        onCheckedChange = onSetMonthlyReviewAlerts,
                    )
                    SettingsDivider()
                    SwitchRow(
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        title = stringResource(R.string.settings_portfolio_alerts),
                        subtitle = stringResource(R.string.settings_portfolio_alerts_sub),
                        checked = data.settings.valuationAlerts,
                        onCheckedChange = onSetValuationAlerts,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_savings_automation))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.Savings,
                    title = stringResource(R.string.settings_goals),
                    subtitle = if (data.savingsGoals.isEmpty()) {
                        stringResource(R.string.settings_goals_empty)
                    } else {
                        pluralStringResource(
                            R.plurals.settings_goals_count,
                            data.savingsGoals.size,
                            data.savingsGoals.size,
                            formatMoney(data.savingsGoals.sumOf { it.savedCents }, currency),
                        )
                    },
                    onClick = onOpenGoals,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.EventRepeat,
                    title = stringResource(R.string.settings_recurring),
                    subtitle = if (data.recurringRules.isEmpty()) {
                        stringResource(R.string.settings_recurring_empty)
                    } else {
                        pluralStringResource(
                            R.plurals.settings_recurring_active,
                            data.recurringRules.count { it.active },
                            data.recurringRules.count { it.active },
                        )
                    },
                    onClick = onOpenRecurring,
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_security))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetBiometric(!data.settings.biometricLock) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceMuted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = colors.inkMuted,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_biometric),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.ink,
                        )
                        Text(
                            text = stringResource(R.string.settings_biometric_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                    BudSwitch(
                        checked = data.settings.biometricLock,
                        onCheckedChange = onSetBiometric,
                    )
                }
                if (data.settings.biometricLock) {
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Fingerprint,
                        title = stringResource(R.string.settings_test_unlock),
                        subtitle = stringResource(R.string.settings_test_unlock_sub),
                        onClick = onTestBiometric,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_categories), trailing = null)
            Spacer(Modifier.height(10.dp))
            BudCard {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    data.categories.forEach { category ->
                        CategoryChip(category = category)
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, colors.line, CircleShape)
                            .clickable { categoryDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_new),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_accounts))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                data.accounts.forEachIndexed { index, account ->
                    AccountRow(account)
                    if (index != data.accounts.lastIndex) SettingsDivider()
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = stringResource(R.string.section_data))
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.FileOpen,
                    title = stringResource(R.string.settings_import_statement),
                    subtitle = stringResource(R.string.settings_import_statement_sub),
                    onClick = onOpenImport,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.settings_encrypted_backup),
                    subtitle = stringResource(R.string.settings_encrypted_backup_sub),
                    onClick = { encryptedExportDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    title = stringResource(R.string.settings_restore_encrypted),
                    subtitle = stringResource(R.string.settings_restore_encrypted_sub),
                    onClick = { encryptedImportLauncher.launch(arrayOf("application/octet-stream", "application/*")) },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.settings_auto_backups),
                    subtitle = stringResource(
                        if (data.settings.autoBackupEnabled) {
                            R.string.settings_auto_backups_on
                        } else {
                            R.string.settings_auto_backups_off
                        },
                    ),
                    onClick = { autoBackupsDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Rule,
                    title = stringResource(R.string.settings_rules),
                    subtitle = pluralStringResource(
                        R.plurals.settings_rules_count,
                        data.categoryRules.size,
                        data.categoryRules.size,
                    ),
                    onClick = { rulesDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.AccountBalance,
                    title = stringResource(R.string.settings_opening_balance),
                    subtitle = stringResource(R.string.settings_opening_balance_sub),
                    trailing = formatMoney(data.settings.openingBalanceCents, currency),
                    onClick = { openingDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Save,
                    title = stringResource(R.string.settings_json_backup),
                    subtitle = stringResource(R.string.settings_json_backup_sub),
                    onClick = { exportLauncher.launch("$backupFilePrefix${LocalDate.now()}.json") },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    title = stringResource(R.string.settings_restore_json),
                    subtitle = stringResource(R.string.settings_restore_json_sub),
                    onClick = {
                        importLauncher.launch(
                            arrayOf("application/json", "application/octet-stream", "text/plain"),
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Refresh,
                    title = stringResource(R.string.settings_demo_data),
                    subtitle = stringResource(R.string.settings_demo_data_sub),
                    onClick = { confirmReset = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.DeleteOutline,
                    title = stringResource(R.string.settings_start_fresh),
                    subtitle = stringResource(R.string.settings_start_fresh_sub),
                    destructive = true,
                    onClick = { confirmFresh = true },
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.settings_backup_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
            BudCard(color = colors.primarySoft, border = false) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row {
                            Text(
                                text = "Bud ",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.ink,
                            )
                            Text(
                                text = "Jet",
                                style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                                color = colors.primary,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_tagline) + " " +
                                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    }
                }
            }
        }
    }

    if (nameDialog) {
        var value by remember { mutableStateOf(data.settings.name) }
        AlertDialog(
            onDismissRequest = { nameDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.settings_name_title), style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.take(24) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.label_name)) },
                    shape = RoundedCornerShape(14.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetName(value)
                        nameDialog = false
                    },
                    enabled = value.isNotBlank(),
                ) { Text(stringResource(R.string.action_save), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { nameDialog = false }) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
            },
        )
    }

    if (currencyDialog) {
        AlertDialog(
            onDismissRequest = { currencyDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.label_currency), style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                Column {
                    Currency.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetCurrency(option.code)
                                    currencyDialog = false
                                }
                                .padding(vertical = 11.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.ink,
                                modifier = Modifier.width(30.dp),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(option.code, style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                            }
                            if (option.code == data.settings.currencyCode) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { currencyDialog = false }) { Text(stringResource(R.string.action_close), color = colors.primary) }
            },
        )
    }

    if (categoryDialog) {
        AddCategoryDialog(
            onDismiss = { categoryDialog = false },
            onSave = { name, icon, color, type ->
                onAddCategory(name, icon, color, type)
                categoryDialog = false
            },
        )
    }

    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.settings_reset_title),
            message = stringResource(R.string.settings_reset_message),
            confirm = stringResource(R.string.action_restore),
            onDismiss = { confirmReset = false },
            onConfirm = {
                onResetDemo()
                confirmReset = false
            },
        )
    }

    if (confirmFresh) {
        ConfirmDialog(
            title = stringResource(R.string.settings_fresh_title),
            message = stringResource(R.string.settings_fresh_message),
            confirm = stringResource(R.string.settings_fresh_confirm),
            onDismiss = { confirmFresh = false },
            onConfirm = {
                onStartFresh()
                confirmFresh = false
            },
        )
    }

    if (openingDialog) {
        var openingText by remember { mutableStateOf(centsToInput(data.settings.openingBalanceCents)) }
        val parsed = parseMoneyInput(openingText)
        AlertDialog(
            onDismissRequest = { openingDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    stringResource(R.string.settings_opening_balance),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.settings_opening_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = openingText,
                        onValueChange = { openingText = filterAmountInput(it, 10) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_amount)) },
                        placeholder = { Text("0") },
                        suffix = { Text(currency.symbol, color = colors.inkMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetOpeningBalance(parsed)
                        openingDialog = false
                    },
                ) { Text(stringResource(R.string.action_save), color = colors.primary) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onSetOpeningBalance(0L)
                            openingDialog = false
                        },
                    ) { Text(stringResource(R.string.action_set_zero), color = colors.inkMuted) }
                    TextButton(onClick = { openingDialog = false }) {
                        Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                    }
                }
            },
        )
    }

    if (encryptedExportDialog) {
        PasswordDialog(
            title = stringResource(R.string.settings_password_protect_title),
            message = stringResource(R.string.settings_password_protect_message),
            confirmLabel = stringResource(R.string.action_save_backup),
            onDismiss = { encryptedExportDialog = false },
            onConfirm = { password ->
                encryptedPassword = password
                encryptedExportDialog = false
                encryptedExportLauncher.launch("$encryptedFilePrefix${LocalDate.now()}.bjet")
            },
        )
    }

    if (encryptedImportDialog && pendingEncryptedBytes != null) {
        PasswordDialog(
            title = stringResource(R.string.settings_password_open_title),
            message = stringResource(R.string.settings_password_open_message),
            confirmLabel = stringResource(R.string.action_open),
            onDismiss = {
                encryptedImportDialog = false
                pendingEncryptedBytes = null
            },
            onConfirm = { password ->
                val bytes = pendingEncryptedBytes
                if (bytes != null) {
                    onValidateEncryptedBackup(bytes, password)
                        .onSuccess {
                            pendingRestore = it
                            encryptedImportDialog = false
                            pendingEncryptedBytes = null
                        }
                        .onFailure {
                            importError = it.message ?: errorOpenEncrypted
                            encryptedImportDialog = false
                            pendingEncryptedBytes = null
                        }
                }
            },
        )
    }

    if (autoBackupsDialog) {
        val backups = remember(autoBackupsDialog, data.settings.autoBackupEnabled) { onListAutoBackups() }
        AlertDialog(
            onDismissRequest = { autoBackupsDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text(stringResource(R.string.settings_auto_backups), style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_daily_backup), style = MaterialTheme.typography.bodyLarge, color = colors.ink)
                            Text(stringResource(R.string.settings_daily_backup_sub), style = MaterialTheme.typography.bodySmall, color = colors.inkMuted)
                        }
                        Switch(
                            checked = data.settings.autoBackupEnabled,
                            onCheckedChange = onSetAutoBackupEnabled,
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (backups.isEmpty()) {
                        Text(
                            text = stringResource(R.string.settings_no_backups),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                        )
                    } else {
                        backups.take(6).forEach { backup ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRestoreAutoBackup(backup.name)
                                            .onSuccess {
                                                pendingRestore = it
                                                autoBackupsDialog = false
                                            }
                                            .onFailure { importError = it.message ?: errorOpenBackup }
                                    }
                                    .padding(vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.History, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = formatShortDate(backup.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${backup.sizeBytes / 1024} KB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.inkMuted,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateAutoBackup()
                        onNotify(notifyAutoBackupCreated)
                        autoBackupsDialog = false
                    },
                    enabled = data.settings.autoBackupEnabled,
                ) { Text(stringResource(R.string.action_create_now), color = colors.primary) }
            },
            dismissButton = { TextButton(onClick = { autoBackupsDialog = false }) { Text(stringResource(R.string.action_close), color = colors.inkMuted) } },
        )
    }

    if (rulesDialog) {
        RuleListDialog(
            rules = data.categoryRules,
            categories = data.categories,
            onDismiss = { rulesDialog = false },
            onDelete = onDeleteCategoryRule,
            onAdd = onAddCategoryRule,
        )
    }

    pendingRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    stringResource(R.string.settings_restore_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(
                            R.string.settings_restore_summary,
                            backup.transactions.size,
                            backup.budgets.size,
                            backup.savingsGoals.size,
                            backup.recurringRules.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_restore_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestoreBackup(backup)
                        pendingRestore = null
                        onNotify(notifyRestored)
                    },
                ) { Text(stringResource(R.string.action_restore), color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.action_cancel), color = colors.inkMuted)
                }
            },
        )
    }

    importError?.let { message ->
        AlertDialog(
            onDismissRequest = { importError = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    stringResource(R.string.settings_import_failed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            },
            confirmButton = {
                TextButton(onClick = { importError = null }) {
                    Text(stringResource(R.string.action_understood), color = colors.primary)
                }
            },
        )
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.surfaceMuted),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.inkMuted)
        }
        BudSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PasswordDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = BudTheme.colors
    var password by remember { mutableStateOf("") }
    val transformation = remember { androidx.compose.ui.text.input.PasswordVisualTransformation() }
    val valid = password.length >= 6
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        BudCard(
            modifier = Modifier
                .padding(24.dp)
                .clickable(onClick = {}),
            padding = 22.dp,
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = colors.inkMuted)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.take(64) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_password)) },
                singleLine = true,
                visualTransformation = transformation,
                shape = RoundedCornerShape(14.dp),
            )
            if (password.isNotBlank() && password.length < 6) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.settings_password_min), style = MaterialTheme.typography.bodySmall, color = colors.expense)
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
                TextButton(onClick = { onConfirm(password) }, enabled = valid) {
                    Text(confirmLabel, color = colors.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleListDialog(
    rules: List<com.budjet.app.data.CategoryRule>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onAdd: (String, String) -> Unit,
) {
    val colors = BudTheme.colors
    var adding by remember { mutableStateOf(false) }
    var pattern by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(categories.firstOrNull { it.type == TxType.EXPENSE }?.id.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(stringResource(R.string.settings_rules_overline))
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.settings_rules_title), style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            }
        },
        text = {
            Column {
                if (!adding) {
                    Text(
                        text = stringResource(R.string.settings_rules_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(rules) { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("«${rule.pattern}»", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                                    Text(
                                        categories.firstOrNull { it.id == rule.categoryId }?.name ?: rule.categoryId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.inkMuted,
                                    )
                                }
                                SoftIconButton(
                                    icon = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.action_delete_rule),
                                    onClick = { onDelete(rule.id) },
                                    size = 28.dp,
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_pattern)) },
                        placeholder = { Text(stringResource(R.string.settings_pattern_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.label_category), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        categories.filter { it.type == TxType.EXPENSE }.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (categoryId == category.id) colors.primarySoft else Color.Transparent)
                                    .border(1.dp, if (categoryId == category.id) colors.primary else colors.line, CircleShape)
                                    .clickable { categoryId = category.id }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (categoryId == category.id) colors.primary else colors.inkMuted,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (adding) {
                        onAdd(pattern, categoryId)
                        pattern = ""
                        adding = false
                    } else {
                        adding = true
                    }
                },
                enabled = !adding || (pattern.isNotBlank() && categoryId.isNotBlank()),
            ) {
                Text(
                    stringResource(if (adding) R.string.action_save_rule else R.string.action_add_rule),
                    color = colors.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (adding) adding = false else onDismiss()
                },
            ) {
                Text(
                    stringResource(if (adding) R.string.action_back else R.string.action_close),
                    color = colors.inkMuted,
                )
            }
        },
    )
}

@Composable
private fun PaletteOption(
    palette: ThemePalette,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BudTheme.colors
    val swatches = when (palette) {
        ThemePalette.BUSINESS -> listOf(PaperLight.background, PaperLight.primary, PaperLight.expense)
        ThemePalette.NAVY -> listOf(NavyLight.background, NavyLight.primary, NavyLight.expense)
        ThemePalette.BOSS -> listOf(BossLight.surfaceMuted, BossLight.ink, BossLight.primary)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primarySoft else colors.surfaceMuted.copy(alpha = 0.55f))
            .border(
                1.dp,
                if (selected) colors.primary else Color.Transparent,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                swatches.forEach { color ->
                    Box(
                        Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, colors.line.copy(alpha = 0.6f), CircleShape),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = palette.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.inkMuted,
            maxLines = 1,
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {    val colors = BudTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primarySoft else Color.Transparent)
            .border(1.dp, if (selected) colors.primary.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.primary else colors.inkMuted,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.inkMuted,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
    destructive: Boolean = false,
) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (destructive) colors.expense.copy(alpha = 0.1f) else colors.surfaceMuted),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) colors.expense else colors.inkMuted,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (destructive) colors.expense else colors.ink,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = colors.ink,
                maxLines = 1,
            )
        }
        if (onClick != null) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.inkMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 60.dp)
            .height(1.dp)
            .background(BudTheme.colors.line.copy(alpha = 0.7f)),
    )
}

@Composable
private fun CategoryChip(category: Category) {
    val colors = BudTheme.colors
    val color = categoryColor(category.colorHex, colors.isDark)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            color = colors.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun AccountRow(account: Account) {
    val colors = BudTheme.colors
    val color = categoryColor(account.colorHex, colors.isDark)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = categoryIcon(account.icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = account.name,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.ink,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, TxType) -> Unit,
) {
    val colors = BudTheme.colors
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TxType.EXPENSE) }
    var icon by remember { mutableStateOf("groceries") }
    var color by remember { mutableStateOf(CategoryPalette.swatches.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Overline(stringResource(R.string.overline_new_category))
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.settings_category_name_title), style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(22) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChoice(stringResource(R.string.type_expense), type == TxType.EXPENSE) { type = TxType.EXPENSE }
                    TypeChoice(stringResource(R.string.type_income), type == TxType.INCOME) { type = TxType.INCOME }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.label_icon), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    iconChoices.forEach { (key, _) ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (icon == key) colors.primarySoft else colors.surfaceMuted)
                                .border(
                                    1.dp,
                                    if (icon == key) colors.primary else Color.Transparent,
                                    CircleShape,
                                )
                                .clickable { icon = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = categoryIcon(key),
                                contentDescription = null,
                                tint = if (icon == key) colors.primary else colors.inkMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.label_color), style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryPalette.swatches.take(10).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseHex(hex))
                                .border(
                                    width = if (color == hex) 2.dp else 0.dp,
                                    color = colors.ink,
                                    shape = CircleShape,
                                )
                                .clickable { color = hex },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, icon, color, type) },
                enabled = name.isNotBlank(),
            ) {
                Text(
                    stringResource(R.string.action_create),
                    color = if (name.isNotBlank()) colors.primary else colors.inkMuted,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
        },
    )
}

@Composable
private fun TypeChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = BudTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) colors.primarySoft else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.line, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.inkMuted,
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirm: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = BudTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirm, color = colors.expense) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = colors.inkMuted) }
        },
    )
}
