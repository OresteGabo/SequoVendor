package dev.orestegabo.sequovendor.core.auth

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.VendorSession
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

/** A token refresh keeps the workspace and unsent drafts; another account/site does not. */
data class VendorWorkspaceKey(val userId: String, val merchantId: String, val localMode: Boolean)
fun VendorSession.workspaceKey() = VendorWorkspaceKey(userId, merchantId, localMode)

fun createVendorWorkspaceClient(
    expected: VendorWorkspaceKey,
    currentSession: () -> VendorSession?,
    restoreUser: suspend () -> CurrentUser?,
    savedAccessToken: suspend () -> String?,
    onRefreshed: (String) -> Unit,
    baseUrl: String = NetworkConfig.BaseUrl,
    engine: HttpClientEngine? = null,
): HttpClient {
    fun current() = currentSession()?.workspaceKey() == expected
    return createSequoVendorHttpClient(baseUrl,
        accessTokenProvider = { currentSession()?.takeIf { it.workspaceKey() == expected }?.accessToken },
        refreshAccessToken = {
            if (!current()) false else {
                val user = restoreUser()
                val token = savedAccessToken()
                if (current() && user?.id == expected.userId && expected.merchantId in user.authorizedMerchantIds() && !token.isNullOrBlank()) {
                    onRefreshed(token)
                    current()
                } else false
            }
        }, engine = engine)
}
