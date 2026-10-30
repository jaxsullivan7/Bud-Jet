package com.budjet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budjet.app.ui.components.BrandMark
import com.budjet.app.ui.components.PrimaryButton
import com.budjet.app.ui.theme.BudTheme

@Composable
fun LockScreen(
    name: String,
    error: String?,
    authenticating: Boolean,
    onUnlock: () -> Unit,
) {
    val colors = BudTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.background, colors.primarySoft.copy(alpha = 0.7f), colors.background),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BrandMark(size = 62.dp)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Bud ",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                    color = colors.ink,
                )
                Text(
                    text = "Jet",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 34.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    ),
                    color = colors.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hola, $name.",
                style = MaterialTheme.typography.titleMedium,
                color = colors.inkMuted,
            )
            Spacer(Modifier.height(42.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.primarySoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Tu dinero, solo para ti.",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                color = colors.ink,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Desbloquea para ver tus cuentas.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            AnimatedVisibility(visible = error != null) {
                Text(
                    text = error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.expense,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            PrimaryButton(
                text = if (authenticating) "Esperando…" else "Desbloquear",
                onClick = onUnlock,
                enabled = !authenticating,
                icon = Icons.Rounded.Lock,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Si la huella falla, usa el PIN de tu móvil.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Protegido con la biometría o el PIN de tu dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
