package com.budjet.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budjet.app.ui.AppViewModel
import com.budjet.app.ui.BudJetApp
import com.budjet.app.ui.theme.BudJetTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : FragmentActivity() {

    val quickCapture = MutableStateFlow<QuickCaptureRequest?>(null)

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory((application as BudJetApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        quickCapture.value = QuickCapture.parse(intent)
        setContent {
            val data by viewModel.data.collectAsStateWithLifecycle()
            BudJetTheme(
                themeMode = data.settings.themeMode,
                paletteStyle = data.settings.palette,
            ) {
                BudJetApp(viewModel = viewModel, data = data)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        quickCapture.value = QuickCapture.parse(intent)
    }

    fun clearQuickCapture() {
        quickCapture.value = null
    }
}
