package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorBiometryLockout
import platform.LocalAuthentication.LAErrorBiometryNotAvailable
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAErrorUserFallback
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator =
    remember { IosBiometricAuthenticator() }

private class IosBiometricAuthenticator : BiometricAuthenticator {
    override val availability: BiometricAvailability
        get() = LAContext().toAvailability()

    override suspend fun authenticate(config: BiometricPromptConfig): BiometricAuthResult {
        val context = LAContext()
        val currentAvailability = context.toAvailability()
        if (currentAvailability != BiometricAvailability.Available) {
            return BiometricAuthResult.Unavailable(currentAvailability)
        }

        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { context.invalidate() }
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = config.subtitle,
            ) { success, error ->
                dispatch_async(dispatch_get_main_queue()) {
                    if (!continuation.isActive) return@dispatch_async
                    val result = when {
                        success -> BiometricAuthResult.Success
                        error?.code == LAErrorUserCancel || error?.code == LAErrorUserFallback -> {
                            BiometricAuthResult.Cancelled
                        }
                        else -> BiometricAuthResult.Failed()
                    }
                    continuation.resume(result)
                }
            }
        }
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private fun LAContext.toAvailability(): BiometricAvailability {
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val canEvaluate = canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = error.ptr,
        )
        if (canEvaluate) return BiometricAvailability.Available

        return when (error.value?.code) {
            LAErrorBiometryNotAvailable -> BiometricAvailability.NoHardware
            LAErrorBiometryNotEnrolled -> BiometricAvailability.NoneEnrolled
            LAErrorBiometryLockout -> BiometricAvailability.TemporarilyUnavailable
            else -> BiometricAvailability.TemporarilyUnavailable
        }
    }
}
