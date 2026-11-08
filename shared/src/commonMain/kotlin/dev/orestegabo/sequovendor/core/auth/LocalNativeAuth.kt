package dev.orestegabo.sequovendor.core.auth

import kotlinx.serialization.Serializable

fun localNativeAuthAllowed(environment: String, baseUrl: String): Boolean {
    if (environment != "LOCAL_FIXTURE") return false
    val match = Regex("^https?://(?:127\\.0\\.0\\.1|localhost|10\\.0\\.2\\.2|\\[::1\\])(?::([0-9]{1,5}))?/?$").matchEntire(baseUrl) ?: return false
    return match.groupValues[1].let { it.isEmpty() || it.toInt() in 1..65535 }
}
@Serializable data class LocalNativeAccount(val key: String, val label: String)
@Serializable data class LocalNativeAccess(val enabled: Boolean, val mode: String? = null, val accounts: List<LocalNativeAccount> = emptyList())
@Serializable internal data class LocalNativeSession(val accessToken: String, val refreshToken: String, val expiresIn: Long, val user: CurrentUser)
@Serializable internal data class LocalNativeSessionRequest(val appSource: String, val accountKey: String, val deviceId: String, val deviceLabel: String = "SequoVendor local acceptance")
