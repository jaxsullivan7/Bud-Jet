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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.data.Account
import com.budjet.app.data.AppData
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
import com.budjet.app.util.formatMoney
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
    val currency = Currency.from(data.settings.currencyCode)

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(onBuildBackup().toByteArray(Charsets.UTF_8))
                } ?: error("No se pudo abrir el archivo.")
            }.onSuccess {
                onNotify("Copia de seguridad guardada")
            }.onFailure {
                onNotify("No se pudo guardar la copia")
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                val declaredSize = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
                if (declaredSize > Backup.MAX_BYTES) error("El archivo es demasiado grande.")
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("No se pudo leer el archivo.")
                if (bytes.size > Backup.MAX_BYTES) error("El archivo es demasiado grande.")
                String(bytes, Charsets.UTF_8)
            }.getOrElse {
                importError = it.message ?: "No se pudo leer el archivo."
                null
            }
            if (text != null) {
                onValidateBackup(text)
                    .onSuccess { pendingRestore = it }
                    .onFailure { importError = it.message ?: "El archivo no es una copia válida." }
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
                    contentDescription = "Volver",
                    onClick = onClose,
                    size = 38.dp,
                )
                Overline("AJUSTES")
                Spacer(Modifier.size(38.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Hazlo tuyo.",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Preferencias y datos de tu Bud Jet.",
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
                            text = "Perfil local · este dispositivo",
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
            SectionHeader(title = "Apariencia")
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 6.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ThemeOption(
                        label = "Claro",
                        icon = Icons.Rounded.LightMode,
                        selected = data.settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onSetTheme(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = "Oscuro",
                        icon = Icons.Rounded.DarkMode,
                        selected = data.settings.themeMode == ThemeMode.DARK,
                        onClick = { onSetTheme(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                    )
                    ThemeOption(
                        label = "Sistema",
                        icon = Icons.Rounded.SettingsBrightness,
                        selected = data.settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onSetTheme(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BudCard(padding = 14.dp) {
                Overline("GAMA DE COLOR")
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
            SectionHeader(title = "Preferencias")
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.Euro,
                    title = "Moneda",
                    subtitle = "${currency.symbol} · ${currency.code}",
                    onClick = { currencyDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Download,
                    title = "Exportar movimientos",
                    subtitle = "Archivo CSV para hoja de cálculo",
                    onClick = onExport,
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Ahorro y automatización")
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.Savings,
                    title = "Objetivos de ahorro",
                    subtitle = if (data.savingsGoals.isEmpty()) {
                        "Crea tu primer objetivo"
                    } else {
                        "${data.savingsGoals.size} objetivos · ${formatMoney(data.savingsGoals.sumOf { it.savedCents }, currency, decimals = false)} apartados"
                    },
                    onClick = onOpenGoals,
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.EventRepeat,
                    title = "Movimientos recurrentes",
                    subtitle = if (data.recurringRules.isEmpty()) {
                        "Nómina, alquiler, suscripciones…"
                    } else {
                        "${data.recurringRules.count { it.active }} reglas activas"
                    },
                    onClick = onOpenRecurring,
                )
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Seguridad")
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
                            text = "Bloqueo biométrico",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.ink,
                        )
                        Text(
                            text = "Pide huella o PIN al abrir la app",
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
                        title = "Probar desbloqueo",
                        subtitle = "Comprueba ahora la huella o el PIN",
                        onClick = onTestBiometric,
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Categorías", trailing = null)
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
                            text = "Nueva",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(26.dp))
            SectionHeader(title = "Cuentas")
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
            SectionHeader(title = "Datos")
            Spacer(Modifier.height(10.dp))
            BudCard(padding = 0.dp) {
                SettingsRow(
                    icon = Icons.Rounded.AccountBalance,
                    title = "Saldo inicial",
                    subtitle = "Punto de partida de tu economía",
                    trailing = formatMoney(data.settings.openingBalanceCents, currency),
                    onClick = { openingDialog = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Save,
                    title = "Copia de seguridad",
                    subtitle = "Guarda todos tus datos en un archivo JSON",
                    onClick = { exportLauncher.launch("budjet-copia-${LocalDate.now()}.json") },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Restore,
                    title = "Restaurar copia",
                    subtitle = "Importa un archivo .json de Bud Jet",
                    onClick = {
                        importLauncher.launch(
                            arrayOf("application/json", "application/octet-stream", "text/plain"),
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.Refresh,
                    title = "Cargar datos de ejemplo",
                    subtitle = "Sustituye todo por la demostración",
                    onClick = { confirmReset = true },
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Rounded.DeleteOutline,
                    title = "Empezar de cero",
                    subtitle = "Borra movimientos, presupuestos, objetivos y recurrentes",
                    destructive = true,
                    onClick = { confirmFresh = true },
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "La copia incluye movimientos, presupuestos, objetivos, recurrentes y ajustes. Por seguridad, el bloqueo biométrico no se restaura.",
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
                            text = "Un lugar tranquilo para tu dinero. Versión 0.4.0 · boceto.",
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
            title = { Text("¿Cómo te llamas?", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.take(24) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nombre") },
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
                ) { Text("Guardar", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { nameDialog = false }) { Text("Cancelar", color = colors.inkMuted) }
            },
        )
    }

    if (currencyDialog) {
        AlertDialog(
            onDismissRequest = { currencyDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = { Text("Moneda", style = MaterialTheme.typography.headlineSmall, color = colors.ink) },
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
                TextButton(onClick = { currencyDialog = false }) { Text("Cerrar", color = colors.primary) }
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
            title = "¿Restaurar el ejemplo?",
            message = "Se sustituirán todos tus movimientos y presupuestos por los datos de demostración.",
            confirm = "Restaurar",
            onDismiss = { confirmReset = false },
            onConfirm = {
                onResetDemo()
                confirmReset = false
            },
        )
    }

    if (confirmFresh) {
        ConfirmDialog(
            title = "¿Empezar de cero?",
            message = "Se borrarán movimientos, presupuestos, objetivos y recurrentes. El saldo inicial pasará a 0 €. Se mantienen categorías, cuentas, nombre y apariencia.",
            confirm = "Empezar de cero",
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
                    "Saldo inicial",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Es el dinero que ya tenías antes de empezar a apuntar. Se suma a tus ingresos y se resta con tus gastos. Puede ser 0 €.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = openingText,
                        onValueChange = { text ->
                            if (text.length <= 10 && text.all { it.isDigit() || it == ',' }) {
                                openingText = text
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Importe") },
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
                ) { Text("Guardar", color = colors.primary) }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onSetOpeningBalance(0L)
                            openingDialog = false
                        },
                    ) { Text("Poner 0", color = colors.inkMuted) }
                    TextButton(onClick = { openingDialog = false }) {
                        Text("Cancelar", color = colors.inkMuted)
                    }
                }
            },
        )
    }

    pendingRestore?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            containerColor = colors.surface,
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    "¿Restaurar esta copia?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
            },
            text = {
                Column {
                    Text(
                        text = "${backup.transactions.size} movimientos · ${backup.budgets.size} presupuestos · " +
                            "${backup.savingsGoals.size} objetivos · ${backup.recurringRules.size} recurrentes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Se sustituirán todos los datos actuales. Por seguridad, el bloqueo biométrico se desactivará.",
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
                        onNotify("Copia restaurada")
                    },
                ) { Text("Restaurar", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text("Cancelar", color = colors.inkMuted)
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
                    "No se pudo importar",
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
                    Text("Entendido", color = colors.primary)
                }
            },
        )
    }
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
                Overline("NUEVA CATEGORÍA")
                Spacer(Modifier.height(4.dp))
                Text("Ponle nombre", style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(22) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChoice("Gasto", type == TxType.EXPENSE) { type = TxType.EXPENSE }
                    TypeChoice("Ingreso", type == TxType.INCOME) { type = TxType.INCOME }
                }
                Spacer(Modifier.height(14.dp))
                Text("Icono", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
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
                Text("Color", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
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
            ) { Text("Crear", color = if (name.isNotBlank()) colors.primary else colors.inkMuted) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
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
            TextButton(onClick = onDismiss) { Text("Cancelar", color = colors.inkMuted) }
        },
    )
}
