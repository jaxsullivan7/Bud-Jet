package com.budjet.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.budjet.app.data.Currency
import com.budjet.app.data.TxType
import com.budjet.app.ui.AppViewModel
import com.budjet.app.ui.EditorDraft
import com.budjet.app.ui.components.BudCard
import com.budjet.app.ui.components.Overline
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.components.SoftIconButton
import com.budjet.app.ui.theme.BudJetTheme
import com.budjet.app.ui.theme.BudTheme
import com.budjet.app.util.BiometricAuth
import com.budjet.app.util.parseMoneyInput
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Compact quick-add flow launched from the home-screen widget. It authenticates
 * with the system biometric prompt and never opens the full app shell.
 */
class QuickAddActivity : FragmentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory((application as BudJetApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val data by viewModel.data.collectAsStateWithLifecycle()
            BudJetTheme(
                themeMode = data.settings.themeMode,
                paletteStyle = data.settings.palette,
            ) {
                QuickAddContent(
                    data = data,
                    onCancel = { finish() },
                    onSave = { draft ->
                        lifecycleScope.launch {
                            viewModel.saveTransaction(draft)?.join()
                            Toast.makeText(this@QuickAddActivity, "Movimiento añadido", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickAddContent(
    data: com.budjet.app.data.AppData,
    onCancel: () -> Unit,
    onSave: (EditorDraft) -> Unit,
) {
    val colors = BudTheme.colors
    val currency = Currency.from(data.settings.currencyCode)
    val activity = LocalActivity.current as? FragmentActivity
    var unlocked by remember { mutableStateOf(!data.settings.biometricLock) }
    var authenticating by remember { mutableStateOf(false) }
    var lockError by remember { mutableStateOf<String?>(null) }

    fun requestUnlock() {
        val host = activity ?: return
        if (!BiometricAuth.canAuthenticate(host)) {
            lockError = BiometricAuth.unavailableReason(host)
            return
        }
        authenticating = true
        BiometricAuth.request(
            activity = host,
            title = "Añadir movimiento",
            subtitle = "Confirma tu identidad para usar el widget",
            onSuccess = {
                authenticating = false
                unlocked = true
            },
            onError = { message ->
                authenticating = false
                lockError = message
            },
        )
    }

    LaunchedEffect(Unit) {
        if (!unlocked) requestUnlock()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        BudCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable(onClick = {}),
            padding = 20.dp,
        ) {
            if (!unlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Bud Jet está bloqueado", style = MaterialTheme.typography.titleMedium, color = colors.ink)
                        Text(
                            text = lockError ?: "Desbloquea para añadir un movimiento desde el widget.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (lockError != null) colors.expense else colors.inkMuted,
                        )
                    }
                    SoftIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        onClick = onCancel,
                        size = 30.dp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = if (authenticating) "Desbloqueando…" else "Desbloquear",
                    onClick = { requestUnlock() },
                    enabled = !authenticating,
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = colors.inkMuted)
                }
                return@BudCard
            }

            var type by remember { mutableStateOf(TxType.EXPENSE) }
            var amount by remember { mutableStateOf("") }
            var categoryId by remember {
                mutableStateOf(
                    data.categories.firstOrNull { it.type == TxType.EXPENSE && !it.archived }?.id.orEmpty(),
                )
            }
            var accountId by remember { mutableStateOf(data.accounts.firstOrNull()?.id.orEmpty()) }
            var note by remember { mutableStateOf("") }
            val parsed = parseMoneyInput(amount)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Overline("DESDE EL WIDGET")
                    Spacer(Modifier.height(4.dp))
                    Text("Nuevo movimiento", style = MaterialTheme.typography.headlineSmall, color = colors.ink)
                }
                SoftIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Cerrar",
                    onClick = onCancel,
                    size = 32.dp,
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceMuted.copy(alpha = 0.6f))
                    .padding(3.dp),
            ) {
                listOf("Gasto" to TxType.EXPENSE, "Ingreso" to TxType.INCOME).forEach { (label, option) ->
                    val selected = type == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) colors.surface else Color.Transparent)
                            .clickable {
                                type = option
                                categoryId = data.categories
                                    .firstOrNull { it.type == option && !it.archived }
                                    ?.id.orEmpty()
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) colors.ink else colors.inkMuted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() || c == ',' }) amount = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quickadd-amount"),
                label = { Text("Importe") },
                placeholder = { Text("0") },
                suffix = { Text(currency.symbol, color = colors.inkMuted) },
                singleLine = true,
                textStyle = TextStyle(color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.Medium),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text("Categoría", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(data.categories.filter { it.type == type && !it.archived }, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (categoryId == category.id) colors.primarySoft else Color.Transparent)
                            .border(1.dp, if (categoryId == category.id) colors.primary else colors.line, CircleShape)
                            .clickable { categoryId = category.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (categoryId == category.id) colors.primary else colors.inkMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Cuenta", style = MaterialTheme.typography.labelMedium, color = colors.inkMuted)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(data.accounts, key = { it.id }) { account ->
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (accountId == account.id) colors.primarySoft else Color.Transparent)
                            .border(1.dp, if (accountId == account.id) colors.primary else colors.line, CircleShape)
                            .clickable { accountId = account.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (accountId == account.id) colors.primary else colors.inkMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(48) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nota (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Guardar movimiento",
                onClick = {
                    onSave(
                        EditorDraft(
                            amountCents = parsed,
                            type = type,
                            categoryId = categoryId,
                            accountId = accountId,
                            date = LocalDate.now(),
                            note = note,
                        ),
                    )
                },
                enabled = parsed > 0L && categoryId.isNotBlank() && accountId.isNotBlank(),
            )
        }
    }
}
