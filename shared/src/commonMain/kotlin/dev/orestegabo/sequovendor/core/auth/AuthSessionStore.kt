package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable

interface AuthSessionStore {
    suspend fun save(session: AuthSession)
    suspend fun get(): AuthSession?
    suspend fun saveRememberedUser(user: CurrentUser)
    suspend fun getRememberedUser(): CurrentUser?
    suspend fun clear()
}

@Composable
expect fun rememberAuthSessionStore(): AuthSessionStore
