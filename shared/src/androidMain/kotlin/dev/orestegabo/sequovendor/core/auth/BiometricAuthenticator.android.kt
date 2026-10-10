package dev.orestegabo.sequovendor.core.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    val activity = context.findActivity()
    return remember(context, activity) {
        AndroidBiometricAuthenticator(
            context = context.applicationContext,
            activity = activity,
        )
    }
}

private class AndroidBiometricAuthenticator(
    private val context: Context,
    private val activity: Activity?,
) : BiometricAuthenticator {
    override val availability: BiometricAvailability
        get() = context.biometricAvailability()

    override suspend fun authenticate(config: BiometricPromptConfig): BiometricAuthResult {
        val currentAvailability = availability
        if (currentAvailability != BiometricAvailability.Available) {
            return BiometricAuthResult.Unavailable(currentAvailability)
        }
        val hostActivity = activity ?: return BiometricAuthResult.Unavailable(BiometricAvailability.NotSupported)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return BiometricAuthResult.Unavailable(BiometricAvailability.NotSupported)
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }

            val executor = Executor { command -> hostActivity.runOnUiThread(command) }
            val prompt = BiometricPrompt.Builder(hostActivity)
                .setTitle(config.title)
                .setSubtitle(config.subtitle)
                .setNegativeButton(config.negativeButtonText, executor) { _, _ ->
                    if (continuation.isActive) {
                        continuation.resume(BiometricAuthResult.Cancelled)
                    }
                }
                .build()

            prompt.authenticate(
                cancellationSignal,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        if (!continuation.isActive) return
                        val result = when (errorCode) {
                            BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED,
                            BiometricPrompt.BIOMETRIC_ERROR_CANCELED,
                            -> BiometricAuthResult.Cancelled
                            else -> BiometricAuthResult.Failed()
                        }
                        continuation.resume(result)
                    }

                    override fun onAuthenticationFailed() {
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Failed())
                        }
                    }
                },
            )
        }
    }
}

@SuppressLint("MissingPermission")
private fun Context.biometricAvailability(): BiometricAvailability {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return BiometricAvailability.NotSupported
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        val fingerprintManager = getSystemService(FingerprintManager::class.java)
            ?: return BiometricAvailability.NoHardware
        return when {
            !packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ||
                !fingerprintManager.isHardwareDetected -> BiometricAvailability.NoHardware
            !fingerprintManager.hasEnrolledFingerprints() -> BiometricAvailability.NoneEnrolled
            else -> BiometricAvailability.NotSupported
        }
    }

    val biometricManager = getSystemService(android.hardware.biometrics.BiometricManager::class.java)
        ?: return BiometricAvailability.NoHardware
    return when (biometricManager.canAuthenticate()) {
        android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
        android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
        android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.TemporarilyUnavailable
        android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
        else -> BiometricAvailability.TemporarilyUnavailable
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
