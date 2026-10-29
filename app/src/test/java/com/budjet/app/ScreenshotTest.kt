package com.budjet.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
@Config(qualifiers = "w411dp-h891dp-420dpi")
class ScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/$name.png")
    }

    @Test
    fun captureScreens() {
        capture("01-resumen")

        compose.onNodeWithContentDescription("Movimientos").performClick()
        capture("02-movimientos")

        compose.onNodeWithContentDescription("Presupuestos").performClick()
        capture("03-presupuestos")

        compose.onNodeWithContentDescription("Análisis").performClick()
        capture("04-analisis")

        compose.onNodeWithContentDescription("Inicio").performClick()
        compose.onNodeWithContentDescription("Añadir movimiento").performClick()
        capture("05-nuevo-movimiento")

        compose.onNodeWithContentDescription("Cerrar").performClick()
        compose.onNodeWithContentDescription("Ajustes").performClick()
        capture("06-ajustes")

        compose.onNodeWithText("Oscuro").performClick()
        capture("07-ajustes-oscuro")

        compose.onNodeWithContentDescription("Volver").performClick()
        capture("08-resumen-oscuro")
    }
}
