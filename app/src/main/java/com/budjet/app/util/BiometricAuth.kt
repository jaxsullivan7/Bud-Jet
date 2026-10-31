package com.budjet.app.util

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuth {

    fun allowedAuthenticators(): Int =
        authenticatorsFor(Build.VERSION.SDK_INT)

    internal fun authenticatorsFor(sdk: Int): Int =
        if (sdk >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK
        }

    internal fun friendlyError(errorCode: Int): String = when (errorCode) {
        BiometricPrompt.ERROR_USER_CANCELED,
        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "Autenticación cancelada."

        BiometricPrompt.ERROR_LOCKOUT ->
            "Demasiados intentos. Espera un momento e inténtalo de nuevo."

        BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
            "El sensor está bloqueado. Desbloquea el móvil con tu PIN e inténtalo otra vez."

        BiometricPrompt.ERROR_NO_BIOMETRICS ->
            "No hay huellas o rostro registrados en este dispositivo."

        BiometricPrompt.ERROR_HW_NOT_PRESENT,
        BiometricPrompt.ERROR_HW_UNAVAILABLE -> "El sensor biométrico no está disponible ahora mismo."

        BiometricPrompt.ERROR_TIMEOUT ->
            "Se agotó el tiempo. Inténtalo de nuevo."

        else -> "No se pudo verificar tu identidad. Inténtalo de nuevo."
    }

    fun canAuthenticate(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(allowedAuthenticators()) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun unavailableReason(activity: FragmentActivity): String =
        when (BiometricManager.from(activity).canAuthenticate(allowedAuthenticators())) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Este dispositivo no tiene lector de huellas."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "El lector no está disponible ahora mismo."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Añade una huella o un PIN de seguridad en los ajustes del móvil."
            else -> "La autenticación no está disponible en este dispositivo."
        }

    fun request(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (!canAuthenticate(activity)) {
            onError(unavailableReason(activity))
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(friendlyError(errorCode))
                }

                override fun onAuthenticationFailed() {
                    onError("No te hemos reconocido. Inténtalo de nuevo.")
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowedAuthenticators())
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setNegativeButtonText("Cancelar")
                }
            }
            .build()
        prompt.authenticate(info)
    }
}
