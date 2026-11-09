package dev.orestegabo.sequovendor.core.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val secureSessionCache: SecureSessionCache,
) {
    private val sessionMutex = Mutex()

    suspend fun loginWithGoogle(idToken: String): AuthSession = sessionMutex.withLock {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        val response = authApiClient.loginWithGoogle(idToken)
        saveSession(response.session)
        response.session
    }

    suspend fun loginWithFacebook(accessToken: String): AuthSession = sessionMutex.withLock {
        if (accessToken.isBlank()) {
            throw FacebookAccessTokenMissingException()
        }

        val response = authApiClient.loginWithFacebook(accessToken)
        saveSession(response.session)
        response.session
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthSession = sessionMutex.withLock {
        val response = authApiClient.signUpWithEmail(email = email, password = password, name = name)
        saveSession(response.session)
        response.session
    }

    suspend fun loginWithEmail(email: String, password: String): AuthSession = sessionMutex.withLock {
        val response = authApiClient.loginWithEmail(email = email, password = password)
        saveSession(response.session)
        response.session
    }

    suspend fun testAccountsAvailable() = authApiClient.testAccountsAvailable()
    suspend fun loginWithTestAccount(username: String, password: String): AuthSession = sessionMutex.withLock {
        val session = authApiClient.loginWithTestAccount(username, password)
        require(authApiClient.currentUser(session.accessToken).authorizedMerchantIds().isNotEmpty()) { "No shop membership." }
        saveSession(session)
        session
    }

    suspend fun localDevelopmentAccess(): LocalNativeAccess = authApiClient.localDevelopmentAccess()

    suspend fun loginWithDevelopmentAccount(accountKey: String): CurrentUser = sessionMutex.withLock {
        val response = authApiClient.developmentSession(accountKey)
        val verified = authApiClient.currentUser(response.accessToken)
        if (verified.id != response.user.id || verified.authorizedMerchantIds().isEmpty())
            throw AuthApiException(403, AuthUserMessage.MerchantRoleRequired)
        val session = AuthSession(response.accessToken, response.refreshToken, response.expiresIn)
        saveSession(session)
        sessionStore.saveRememberedUser(verified)
        verified
    }

    suspend fun currentUser(): CurrentUser? {
        val session = sessionStore.get() ?: return null
        return authApiClient.currentUser(session.accessToken)
    }

    suspend fun restoreSavedSession(): CurrentUser? = sessionMutex.withLock {
        val session = sessionStore.get() ?: return@withLock null

        runCatching {
            authApiClient.currentUser(session.accessToken)
        }.getOrElse { firstError ->
            if ((firstError as? AuthApiException)?.statusCode != 401) throw firstError

            val refreshedSession = authApiClient.refreshSession(session.refreshToken)
            saveSession(refreshedSession)
            authApiClient.currentUser(refreshedSession.accessToken)
        }.also { user ->
            sessionStore.saveRememberedUser(user)
        }
    }

    suspend fun getRememberedUser(): CurrentUser? =
        sessionStore.getRememberedUser()

    suspend fun rememberUser(user: CurrentUser) {
        sessionStore.saveRememberedUser(user)
    }

    suspend fun getSavedSession(): AuthSession? =
        sessionStore.get()

    suspend fun hasCachedRefreshToken(): Boolean =
        secureSessionCache.hasCachedRefreshToken()

    suspend fun migrateSavedSessionToSecureCache() {
        val session = sessionStore.get() ?: return
        secureSessionCache.cache(session)
    }

    suspend fun unlockCachedSession(promptConfig: BiometricPromptConfig = BiometricPromptConfig()): SecureSessionUnlockResult =
        secureSessionCache.unlockCachedSession(promptConfig)

    suspend fun logout() = sessionMutex.withLock {
        sessionStore.get()?.let { session ->
            runCatching { authApiClient.logout(session.refreshToken) }
        }
        sessionStore.clear()
        secureSessionCache.clearSession()
    }

    fun close() {
        authApiClient.close()
    }

    private suspend fun saveSession(session: AuthSession) {
        sessionStore.save(session)
        secureSessionCache.cache(session)
    }
}

class GoogleIdTokenMissingException : Exception(
    AuthUserMessage.GoogleSignInTemporarilyUnavailable.name,
)

class FacebookAccessTokenMissingException : Exception(
    AuthUserMessage.SocialSignInUnavailable.name,
)
