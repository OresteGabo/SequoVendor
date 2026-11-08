package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.auth.AuthApiClient
import dev.orestegabo.sequovendor.core.auth.AuthApiException
import dev.orestegabo.sequovendor.core.network.ApiHealthClient
import dev.orestegabo.sequovendor.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import dev.orestegabo.sequovendor.core.activity.MerchantActivityApiClient
import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentApiClient
import dev.orestegabo.sequovendor.core.network.ApiResult

/** The transport is mocked: this regression must never contact a configured deployment. */
class ReleaseTargetClientTest {
    @Test
    fun defaultAuthAndOperationalClientsUseTheSameGeneratedOrigin() = runBlocking<Unit> {
        val requests = mutableListOf<Url>()
        val client = HttpClient(MockEngine { request ->
            requests += request.url
            when (request.url.encodedPath) {
                "/api/auth/login" -> respond("{}", HttpStatusCode.Unauthorized,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
                "/actuator/health" -> respond("{\"status\":\"UP\"}", HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
                else -> respond("[]", HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        }) {
            install(ContentNegotiation) { json() }
        }
        try {
            assertFailsWith<AuthApiException> {
                AuthApiClient(httpClient = client, deviceIdProvider = { "fixture-device" })
                    .loginWithEmail("release@example.test", "fixture-only")
            }
            assertTrue(ApiHealthClient(httpClient = client).checkHealth().healthy)
            assertTrue(MerchantFulfillmentApiClient(httpClient = client).listSubOrders("fixture-merchant").isEmpty())
            assertTrue(MerchantActivityApiClient(httpClient = client).load("fixture-merchant") is ApiResult.Success)
            assertEquals(listOf("/api/auth/login", "/actuator/health", "/api/merchant/sub-orders", "/api/merchant/sub-orders", "/api/returns/merchant", "/api/settlements/merchant-payouts"), requests.map { it.encodedPath })
            val target = Url(NetworkConfig.BaseUrl)
            requests.forEach {
                assertEquals(target.protocol, it.protocol)
                assertEquals(target.host, it.host)
                assertEquals(target.port, it.port)
            }
            assertTrue(NetworkConfig.ReleaseId.isNotBlank())
            assertTrue(NetworkConfig.Environment in setOf("PRODUCTION", "STAGING", "LOCAL_FIXTURE"))
        } finally {
            client.close()
        }
    }
}
