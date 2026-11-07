package com.budjet.app.util

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.budjet.app.R

object BiometricAuth {

    fun allowedAuthenticators(): Int =
        authenticatorsFor(Build.VERSION.SDK_INT)

    internal fun authenticatorsFor(sdk: Int): Int =
        if (sdk >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK
        }

    internal fun friendlyErrorMessage(context: Context, errorCode: Int): String =
        context.getString(
            when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> R.string.biometric_cancelled

                BiometricPrompt.ERROR_LOCKOUT -> R.string.biometric_lockout

                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> R.string.biometric_lockout_permanent

                BiometricPrompt.ERROR_NO_BIOMETRICS -> R.string.biometric_no_biometrics

                BiometricPrompt.ERROR_HW_NOT_PRESENT,
                BiometricPrompt.ERROR_HW_UNAVAILABLE -> R.string.biometric_hw_unavailable

                BiometricPrompt.ERROR_TIMEOUT -> R.string.biometric_timeout

                else -> R.string.biometric_generic
            },
        )

    fun canAuthenticate(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(allowedAuthenticators()) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun unavailableReason(activity: FragmentActivity): String =
        activity.getString(
            when (BiometricManager.from(activity).canAuthenticate(allowedAuthenticators())) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> R.string.biometric_no_hardware
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> R.string.biometric_reader_unavailable
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> R.string.biometric_none_enrolled
                else -> R.string.biometric_unavailable
            },
        )

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
                    onError(friendlyErrorMessage(activity, errorCode))
                }

                override fun onAuthenticationFailed() {
                    onError(activity.getString(R.string.biometric_not_recognized))
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowedAuthenticators())
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setNegativeButtonText(activity.getString(R.string.action_cancel))
                }
            }
            .build()
        prompt.authenticate(info)
    }
}
