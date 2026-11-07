package com.budjet.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AdaptiveScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots-adaptive/$name.png")
    }

    @Test
    @Config(qualifiers = "en-rUS-w891dp-h411dp-land-420dpi")
    fun landscapePhone() {
        capture("01-phone-landscape-home")
        compose.onNodeWithContentDescription("Transactions").performClick()
        capture("02-phone-landscape-transactions")
    }

    @Test
    @Config(qualifiers = "en-rUS-w1280dp-h800dp-240dpi")
    fun tablet() {
        capture("03-tablet-home")
        compose.onNodeWithContentDescription("Analysis").performClick()
        capture("04-tablet-analysis")
    }
}
