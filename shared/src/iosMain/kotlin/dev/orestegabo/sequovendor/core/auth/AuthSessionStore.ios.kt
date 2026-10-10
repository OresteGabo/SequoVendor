package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberAuthSessionStore(): AuthSessionStore =
    remember { IosAuthSessionStore() }

private class IosAuthSessionStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val json: Json = Json,
) : AuthSessionStore {
    override suspend fun save(session: AuthSession) {
        userDefaults.setObject(
            value = json.encodeToString(session),
            forKey = AuthSessionKey,
        )
    }

    override suspend fun get(): AuthSession? =
        userDefaults.stringForKey(AuthSessionKey)?.let { encodedSession ->
            runCatching { json.decodeFromString<AuthSession>(encodedSession) }.getOrNull()
        }

    override suspend fun saveRememberedUser(user: CurrentUser) {
        userDefaults.setObject(
            value = json.encodeToString(user),
            forKey = RememberedUserKey,
        )
    }

    override suspend fun getRememberedUser(): CurrentUser? =
        userDefaults.stringForKey(RememberedUserKey)?.let { encodedUser ->
            runCatching { json.decodeFromString<CurrentUser>(encodedUser) }.getOrNull()
        }

    override suspend fun clear() {
        userDefaults.removeObjectForKey(AuthSessionKey)
        userDefaults.removeObjectForKey(RememberedUserKey)
    }

    private companion object {
        const val AuthSessionKey = "auth_session"
        const val RememberedUserKey = "remembered_user"
    }
}
