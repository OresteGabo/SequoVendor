package dev.orestegabo.sequovendor.core.auth

class SecureSessionCache(
    private val tokenStorage: SecureTokenStorage,
    private val biometricAuthenticator: BiometricAuthenticator,
) {
    suspend fun cache(session: AuthSession) {
        tokenStorage.saveRefreshToken(session.refreshToken)
    }

    suspend fun hasCachedRefreshToken(): Boolean =
        tokenStorage.getRefreshToken()?.isNotBlank() == true

    suspend fun unlockCachedSession(
        promptConfig: BiometricPromptConfig = BiometricPromptConfig(),
    ): SecureSessionUnlockResult {
        val token = tokenStorage.getRefreshToken()?.takeIf { it.isNotBlank() }
            ?: return SecureSessionUnlockResult.NoCachedSession

        return when (val result = biometricAuthenticator.authenticate(promptConfig)) {
            BiometricAuthResult.Success -> SecureSessionUnlockResult.Unlocked(token)
            BiometricAuthResult.Cancelled -> SecureSessionUnlockResult.Cancelled
            is BiometricAuthResult.Unavailable -> SecureSessionUnlockResult.Unavailable(result.availability)
            is BiometricAuthResult.Failed -> SecureSessionUnlockResult.Failed(result.message)
        }
    }

    suspend fun clearSession() {
        tokenStorage.clearSession()
    }
}

sealed interface SecureSessionUnlockResult {
    data class Unlocked(val refreshToken: String) : SecureSessionUnlockResult
    data object NoCachedSession : SecureSessionUnlockResult
    data object Cancelled : SecureSessionUnlockResult
    data class Unavailable(val availability: BiometricAvailability) : SecureSessionUnlockResult
    data class Failed(val message: String? = null) : SecureSessionUnlockResult
}
