package com.budjet.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-rUS-w411dp-h891dp-420dpi")
class EnglishScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots-en/$name.png")
    }

    @Test
    fun captureHomeInEnglish() {
        capture("01-home")
        compose.onNodeWithTag("home-list").performScrollToNode(hasText("Recent transactions"))
        capture("02-home-recent")
    }

    @Test
    fun captureTransactionsInEnglish() {
        compose.onNodeWithContentDescription("Transactions").performClick()
        capture("03-transactions")
        compose.onNodeWithTag("sort-control").performClick()
        capture("04-transactions-sort")
    }
}
