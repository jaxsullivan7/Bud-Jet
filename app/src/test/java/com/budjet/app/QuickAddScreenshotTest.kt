package com.budjet.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "es-rES-w411dp-h891dp-420dpi")
class QuickAddScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<QuickAddActivity>()

    @Test
    fun captureQuickAddFromWidget() {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/21-widget-quickadd.png")
    }

    @Test
    fun quickAddSavesTheMovementBeforeClosing() {
        val app = compose.activity.application as BudJetApplication
        compose.onNodeWithTag("quickadd-amount").performTextInput("12,50")
        compose.onNodeWithText("Guardar movimiento").performClick()
        compose.waitForIdle()
        val saved = runBlocking { app.repository.data.value.transactions.any { it.amountCents == 1_250L } }
        assertTrue(saved)
    }
}
