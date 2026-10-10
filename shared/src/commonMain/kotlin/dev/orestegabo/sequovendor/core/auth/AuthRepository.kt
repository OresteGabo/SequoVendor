package dev.orestegabo.sequovendor.core.auth

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val secureSessionCache: SecureSessionCache,
) {
    suspend fun loginWithGoogle(idToken: String): AuthSession {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        val response = authApiClient.loginWithGoogle(idToken)
        saveSession(response.session)
        return response.session
    }

    suspend fun loginWithFacebook(accessToken: String): AuthSession {
        if (accessToken.isBlank()) {
            throw FacebookAccessTokenMissingException()
        }

        val response = authApiClient.loginWithFacebook(accessToken)
        saveSession(response.session)
        return response.session
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthSession {
        val response = authApiClient.signUpWithEmail(email = email, password = password, name = name)
        saveSession(response.session)
        return response.session
    }

    suspend fun loginWithEmail(email: String, password: String): AuthSession {
        val response = authApiClient.loginWithEmail(email = email, password = password)
        saveSession(response.session)
        return response.session
    }

    suspend fun currentUser(): CurrentUser? {
        val session = sessionStore.get() ?: return null
        return authApiClient.currentUser(session.accessToken)
    }

    suspend fun restoreSavedSession(): CurrentUser? {
        val session = sessionStore.get() ?: return null

        return runCatching {
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

    suspend fun logout() {
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
