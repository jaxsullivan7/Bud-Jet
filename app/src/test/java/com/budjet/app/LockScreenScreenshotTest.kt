package com.budjet.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budjet.app.ui.screens.LockScreen
import com.budjet.app.ui.theme.BudJetTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-420dpi")
class LockScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun captureLockScreen() {
        compose.setContent {
            BudJetTheme {
                LockScreen(
                    name = "Jax",
                    error = null,
                    authenticating = false,
                    onUnlock = {},
                )
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/17-bloqueo.png")
    }
}
