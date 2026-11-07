package com.budjet.app

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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

    @Test
    fun captureAnalysisInEnglish() {
        compose.onNodeWithContentDescription("Analysis").performClick()
        capture("05-analysis")
        compose.onNodeWithTag("analysis-list")
            .performScrollToNode(hasText("Your month in numbers"))
        capture("06-analysis-stats")
    }

    @Test
    fun captureBudgetsInEnglish() {
        compose.onNodeWithContentDescription("Budgets").performClick()
        capture("07-budgets")
    }

    @Test
    fun capturePatrimonioInEnglish() {
        compose.onNodeWithContentDescription("Analysis").performClick()
        compose.onNodeWithTag("patrimonio-card").performClick()
        capture("08-patrimonio")
    }

    @Test
    fun capturePortfolioInEnglish() {
        compose.onNodeWithContentDescription("Analysis").performClick()
        compose.onNodeWithTag("patrimonio-card").performClick()
        compose.onNodeWithText("Portfolio").performClick()
        capture("09-portfolio")
        compose.onNodeWithTag("portfolio-list")
            .performScrollToNode(hasTestTag("calculator-card"))
        compose.onNodeWithTag("calculator-card").performClick()
        capture("10-calculator")
    }

    @Test
    fun captureDebtPlanInEnglish() {
        compose.onNodeWithContentDescription("Analysis").performClick()
        compose.onNodeWithTag("patrimonio-card").performClick()
        compose.onNodeWithTag("patrimonio-list")
            .performScrollToNode(hasTestTag("debt-plan-button"))
        compose.onNodeWithTag("debt-plan-button").performClick()
        capture("11-debt-plan")
    }

    @Test
    fun captureSettingsInEnglish() {
        openSettings()
        capture("12-settings")
    }

    @Test
    fun captureTransactionEditorInEnglish() {
        compose.onNodeWithContentDescription("Add transaction").performClick()
        capture("13-editor")
    }

    @Test
    fun captureGoalsInEnglish() {
        openSettings()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Savings goals"))
        compose.onNodeWithText("Savings goals").performClick()
        capture("14-goals")
    }

    @Test
    fun captureRecurringInEnglish() {
        openSettings()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Recurring transactions"))
        compose.onNodeWithText("Recurring transactions").performClick()
        capture("15-recurring")
    }

    @Test
    fun captureImportInEnglish() {
        openSettings()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Import statement"))
        compose.onNodeWithText("Import statement").performClick()
        capture("16-import")
    }

    @Test
    fun capturePayrollInEnglish() {
        openSettings()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasTestTag("settings-payroll"))
        compose.onNodeWithTag("settings-payroll").performClick()
        capture("18-payroll")
    }

    private fun openSettings() {
        compose.onNodeWithContentDescription("Settings").performClick()
    }
}
