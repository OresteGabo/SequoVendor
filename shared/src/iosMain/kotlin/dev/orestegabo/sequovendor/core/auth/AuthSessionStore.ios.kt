package dev.orestegabo.sequovendor.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberAuthSessionStore(): AuthSessionStore =
    remember { IosAuthSessionStore() }

private class IosAuthSessionStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val keychain: AuthKeychainStore = AuthKeychainStore(),
    private val json: Json = Json,
) : AuthSessionStore {
    private val mutex = Mutex()

    override suspend fun save(session: AuthSession) = mutex.withLock {
        saveValue(AuthSessionKey, json.encodeToString(session))
    }

    override suspend fun get(): AuthSession? = mutex.withLock {
        readValue(AuthSessionKey) { runCatching { json.decodeFromString<AuthSession>(it) }.isSuccess }
            ?.let { json.decodeFromString<AuthSession>(it) }
    }

    override suspend fun saveRememberedUser(user: CurrentUser) = mutex.withLock {
        saveValue(RememberedUserKey, json.encodeToString(user))
    }

    override suspend fun getRememberedUser(): CurrentUser? = mutex.withLock {
        readValue(RememberedUserKey) { runCatching { json.decodeFromString<CurrentUser>(it) }.isSuccess }
            ?.let { json.decodeFromString<CurrentUser>(it) }
    }

    override suspend fun clear() = mutex.withLock {
        keychain.clear(AuthSessionKey)
        userDefaults.removeObjectForKey(AuthSessionKey)
        keychain.clear(RememberedUserKey)
        userDefaults.removeObjectForKey(RememberedUserKey)
    }

    private suspend fun saveValue(key: String, value: String) =
        saveMigratedAuthValue(value, { keychain.save(key, it) }, { keychain.get(key) },
            { userDefaults.removeObjectForKey(key) })

    private suspend fun readValue(key: String, valid: (String) -> Boolean): String? =
        readMigratingAuthValue(
            readSecure = { keychain.get(key) },
            readLegacy = { userDefaults.stringForKey(key) },
            writeSecure = { keychain.save(key, it) },
            removeLegacy = { userDefaults.removeObjectForKey(key) },
            valid = valid,
        )

    private companion object {
        const val AuthSessionKey = "auth_session"
        const val RememberedUserKey = "remembered_user"
    }
}
