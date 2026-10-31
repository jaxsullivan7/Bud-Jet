package com.budjet.app

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budjet.app.util.BiometricAuth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class BiometricAuthTest {

    @Test
    fun `old devices use weak biometrics`() {
        assertEquals(BIOMETRIC_WEAK, BiometricAuth.authenticatorsFor(29))
    }

    @Test
    fun `modern devices allow biometric or device credential`() {
        assertEquals(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
            BiometricAuth.authenticatorsFor(30),
        )
    }

    @Test
    fun `cancelling is not shown as a scary error`() {
        assertEquals("Autenticación cancelada.", BiometricAuth.friendlyError(BiometricPrompt.ERROR_USER_CANCELED))
        assertEquals("Autenticación cancelada.", BiometricAuth.friendlyError(BiometricPrompt.ERROR_NEGATIVE_BUTTON))
    }

    @Test
    fun `lockout errors explain how to recover`() {
        assertTrue(BiometricAuth.friendlyError(BiometricPrompt.ERROR_LOCKOUT).contains("Demasiados intentos"))
        assertTrue(BiometricAuth.friendlyError(BiometricPrompt.ERROR_LOCKOUT_PERMANENT).contains("PIN"))
        assertTrue(BiometricAuth.friendlyError(BiometricPrompt.ERROR_NO_BIOMETRICS).contains("huellas"))
    }
}
