package com.budjet.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budjet.app.data.Transaction
import com.budjet.app.data.TxType
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-420dpi")
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/$name.png")
    }

    private fun back() {
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    @Test
    fun captureScreens() {
        capture("01-resumen")

        compose.onNodeWithTag("home-list").performScrollToNode(hasText("Objetivos"))
        capture("02-resumen-objetivos")
        compose.onNodeWithText("Fondo de emergencia").performClick()
        capture("03-objetivos")
        back()

        compose.onNodeWithContentDescription("Movimientos").performClick()
        capture("04-movimientos")
        compose.onNodeWithContentDescription("Buscar").performClick()
        compose.onNodeWithTag("search-field").performTextInput("Restaurantes")
        capture("04b-movimientos-busqueda")
        compose.onNodeWithTag("search-field").performTextClearance()
        compose.onNodeWithContentDescription("Cerrar búsqueda").performClick()

        compose.onNodeWithContentDescription("Presupuestos").performClick()
        capture("05-presupuestos")

        compose.onNodeWithContentDescription("Análisis").performClick()
        capture("06-analisis")
        compose.onNodeWithText("Restaurantes").performClick()
        capture("07-categoria")
        back()

        compose.onNodeWithContentDescription("Inicio").performClick()
        compose.onNodeWithContentDescription("Añadir movimiento").performClick()
        capture("08-nuevo-movimiento")

        compose.onNodeWithText("Ingreso").performClick()
        compose.onNodeWithTag("editor-categories").performScrollToNode(hasText("Anulación"))
        compose.onNodeWithText("Anulación").performClick()
        compose.onNodeWithText("Buscar pago a anular").performClick()
        capture("08b-anulacion-picker")
        compose.onNodeWithText("Casa Rafa").performClick()
        capture("08c-anulacion")
        compose.onNodeWithContentDescription("Cerrar").performClick()
        compose.onNodeWithContentDescription("Ajustes").performClick()
        capture("09-ajustes")

        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Copia de seguridad"))
        capture("09b-ajustes-datos")

        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Navy"))
        compose.onNodeWithText("Navy").performClick()
        capture("10-ajustes-navy")
        back()
        capture("11-resumen-navy")

        compose.onNodeWithContentDescription("Ajustes").performClick()
        compose.onNodeWithText("Boss").performClick()
        capture("12-ajustes-boss")
        back()
        capture("13-resumen-boss")

        compose.onNodeWithContentDescription("Ajustes").performClick()
        compose.onNodeWithText("Business").performClick()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Movimientos recurrentes"))
        compose.onNodeWithText("Movimientos recurrentes").performClick()
        capture("14-recurrentes")
        back()

        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Oscuro"))
        compose.onNodeWithText("Oscuro").performClick()
        capture("15-ajustes-oscuro")

        back()
        capture("16-resumen-oscuro")

        compose.onNodeWithContentDescription("Ajustes").performClick()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Claro"))
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithTag("settings-list").performScrollToNode(hasText("Empezar de cero"))
        compose.onNodeWithText("Empezar de cero").performClick()
        compose.onAllNodesWithText("Empezar de cero").onLast().performClick()
        back()
        capture("18-resumen-vacio")

        compose.activityRule.scenario.onActivity { activity ->
            val app = activity.application as BudJetApplication
            runBlocking {
                app.repository.update { current ->
                    current.copy(
                        transactions = listOf(
                            Transaction(
                                id = "future-salary",
                                amountCents = 245_000,
                                type = TxType.INCOME,
                                categoryId = "salary",
                                accountId = "main",
                                dateEpochDay = LocalDate.now().plusDays(5).toEpochDay(),
                                note = "Nómina",
                            ),
                        ),
                    )
                }
            }
        }
        compose.waitForIdle()
        capture("19-resumen-programado")
        compose.onNodeWithTag("home-list").performScrollToNode(hasText("Próximos"))
        capture("20-programado-detalle")
    }
}
