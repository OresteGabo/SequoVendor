package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable

enum class BiometricAvailability {
    Available,
    NoHardware,
    NoneEnrolled,
    TemporarilyUnavailable,
    NotSupported,
}

data class BiometricPromptConfig(
    val title: String = "Unlock Sequo",
    val subtitle: String = "Use your device unlock to continue.",
    val negativeButtonText: String = "Cancel",
)

sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult
    data object Cancelled : BiometricAuthResult
    data class Unavailable(val availability: BiometricAvailability) : BiometricAuthResult
    data class Failed(val message: String? = null) : BiometricAuthResult
}

interface BiometricAuthenticator {
    val availability: BiometricAvailability
    suspend fun authenticate(config: BiometricPromptConfig = BiometricPromptConfig()): BiometricAuthResult
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
