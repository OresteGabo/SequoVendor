package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.negotiation.NetworkVendorNegotiationRepository
import dev.orestegabo.sequovendor.core.negotiation.VendorNegotiationApiClient
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.VendorNegotiationThread
import dev.orestegabo.sequovendor.model.VendorNegotiationStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VendorNegotiationClientTest {
    private val baseUrl = "https://bargaining.sequo.test"
    private val headers = headersOf(HttpHeaders.ContentType, "application/json")
    private val pending = """{"id":"thread-1","merchantId":"merchant-1","productId":"product-1","productName":"Fixture bag","status":"OPEN","bargainingEnabled":true,"nextActor":"MERCHANT","customerAttemptsUsed":2,"merchantAttemptsUsed":1,"maxCustomerAttempts":3,"maxMerchantAttempts":3,"offers":[{"id":"offer-c1","actorType":"CUSTOMER","offerType":"OFFER","amountCfa":7500,"message":"Fixture offer","createdAt":"2026-11-08T12:00:00Z"}]}"""
    private val accepted get() = pending.replace("\"status\":\"OPEN\"", "\"status\":\"ACCEPTED\",\"acceptedPriceCfa\":7500,\"expiresAt\":\"2026-11-09T12:00:00Z\"")

    @Test
    fun merchantInboxUsesAuthenticatedScopeAndServerAttemptsHistoryAndEligibility() = runBlocking<Unit> {
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, accessTokenProvider = { "merchant-fixture" }, engine = MockEngine { request ->
            assertEquals("Bearer merchant-fixture", request.headers[HttpHeaders.Authorization])
            assertEquals("merchant-1", request.url.parameters["merchantId"])
            assertEquals("/api/bargaining/threads", request.url.encodedPath)
            respond("[$pending]", HttpStatusCode.OK, headers)
        })).use { client ->
            val result = NetworkVendorNegotiationRepository(client, "merchant-1").listThreads("merchant-1")
            val thread = assertIs<ApiResult.Success<List<VendorNegotiationThread>>>(result).value.single()
            assertEquals(2, thread.merchantAttemptsRemaining)
            assertEquals(1, thread.customerAttemptsRemaining)
            assertEquals("Fixture offer", thread.messages.single().message)
            assertEquals("offer-c1", thread.pendingCustomerOfferId)
            assertTrue(thread.merchantActionAllowed)
            assertFalse(thread.localDemo)
        }
    }

    @Test
    fun counterKeepsExpectedOfferAndRetryKeyThroughAuthRefreshAndReadsBack() = runBlocking<Unit> {
        val bodies = mutableListOf<String>()
        var token = "expired-fixture"
        val counter = pending.replace("\"nextActor\":\"MERCHANT\"", "\"nextActor\":\"CUSTOMER\"")
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, accessTokenProvider = { token }, refreshAccessToken = { token = "refreshed-fixture"; true }, engine = MockEngine { request ->
            when {
                request.method == HttpMethod.Post -> {
                    bodies += (request.body as TextContent).text
                    if (bodies.size == 1) respond("{}", HttpStatusCode.Unauthorized, headers)
                    else { assertEquals("Bearer refreshed-fixture", request.headers[HttpHeaders.Authorization]); respond(counter, HttpStatusCode.OK, headers) }
                }
                request.url.encodedPath.endsWith("thread-1") -> respond(counter, HttpStatusCode.OK, headers)
                else -> respond("[$pending]", HttpStatusCode.OK, headers)
            }
        })).use { client ->
            val repository = NetworkVendorNegotiationRepository(client, "merchant-1")
            repository.listThreads("merchant-1")
            val saved = assertIs<ApiResult.Success<VendorNegotiationThread>>(repository.counter("thread-1", 8000)).value
            assertEquals(VendorNegotiationStatus.MERCHANT_COUNTERED, saved.status)
            assertFalse(saved.merchantActionAllowed)
            assertEquals(bodies[0], bodies[1])
            val body = Json.parseToJsonElement(bodies[0]).jsonObject
            assertEquals("COUNTER_OFFER", body.getValue("offerType").jsonPrimitive.content)
            assertEquals("offer-c1", body.getValue("expectedOfferId").jsonPrimitive.content)
            assertEquals("8000", body.getValue("amountCfa").jsonPrimitive.content)
            assertTrue(body.getValue("idempotencyKey").jsonPrimitive.content.isNotBlank())
        }
    }

    @Test
    fun successfulPostWithFailedReadbackRemainsFailureAndRetryUsesSameIntentKey() = runBlocking<Unit> {
        val keys = mutableListOf<String>()
        var readbackCalls = 0
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { request ->
            when {
                request.method == HttpMethod.Post -> {
                    val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals("ACCEPT", body.getValue("offerType").jsonPrimitive.content)
                    keys += body.getValue("idempotencyKey").jsonPrimitive.content
                    respond(accepted, HttpStatusCode.OK, headers)
                }
                request.url.encodedPath.endsWith("thread-1") -> {
                    readbackCalls++
                    if (readbackCalls == 1) respond("{}", HttpStatusCode.ServiceUnavailable, headers)
                    else respond(accepted, HttpStatusCode.OK, headers)
                }
                else -> respond("[$pending]", HttpStatusCode.OK, headers)
            }
        })).use { client ->
            val repository = NetworkVendorNegotiationRepository(client, "merchant-1")
            repository.listThreads("merchant-1")
            val failure = assertIs<ApiResult.Failure>(repository.accept("thread-1"))
            assertEquals(ApiFailureKind.ServerUnavailable, failure.error.kind)
            val saved = assertIs<ApiResult.Success<VendorNegotiationThread>>(repository.accept("thread-1")).value
            assertEquals(keys[0], keys[1])
            assertEquals(VendorNegotiationStatus.ACCEPTED, saved.status)
            assertEquals(7500, saved.acceptedPriceCfa)
            assertEquals("2026-11-09T12:00:00Z", saved.lockExpiresAt)
            assertFalse(saved.merchantActionAllowed)
        }
    }

    @Test
    fun nullableLegacyProductNameAndUnknownStatusRemainReadableAndFailClosed() = runBlocking<Unit> {
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine {
            respond("[${pending.replace("\"productName\":\"Fixture bag\"", "\"productName\":null").replace("\"status\":\"OPEN\"", "\"status\":\"LEGACY_STATE\"")}]", HttpStatusCode.OK, headers)
        })).use { client ->
            val result = NetworkVendorNegotiationRepository(client, "merchant-1").listThreads("merchant-1")
            val thread = assertIs<ApiResult.Success<List<VendorNegotiationThread>>>(result).value.single()
            assertEquals("product-1", thread.productName)
            assertEquals(VendorNegotiationStatus.UNKNOWN, thread.status)
            assertFalse(thread.merchantActionAllowed)
        }
    }

    @Test
    fun deniedAndCrossMerchantReadsNeverReturnDemoOrAnotherMerchantsThread() = runBlocking<Unit> {
        var denied = true
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine {
            if (denied) respond("{}", HttpStatusCode.Forbidden, headers)
            else respond("[${pending.replace("merchant-1", "merchant-2")}]", HttpStatusCode.OK, headers)
        })).use { client ->
            val repository = NetworkVendorNegotiationRepository(client, "merchant-1")
            assertEquals(ApiFailureKind.PermissionDenied, assertIs<ApiResult.Failure>(repository.listThreads("merchant-1")).error.kind)
            denied = false
            assertIs<ApiResult.Failure>(repository.listThreads("merchant-1"))
            assertIs<ApiResult.Failure>(repository.accept("thread-1"))
            assertIs<ApiResult.Failure>(repository.listThreads("merchant-2"))
        }
    }

    @Test
    fun messagesAndRejectUseDifferentKeysAndAuthoritativeReadback() = runBlocking<Unit> {
        val requests = mutableListOf<kotlinx.serialization.json.JsonObject>()
        VendorNegotiationApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { request ->
            if (request.method == HttpMethod.Post) {
                requests += Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                respond(pending, HttpStatusCode.OK, headers)
            } else if (request.url.encodedPath.endsWith("thread-1")) respond(pending, HttpStatusCode.OK, headers)
            else respond("[$pending]", HttpStatusCode.OK, headers)
        })).use { client ->
            val repository = NetworkVendorNegotiationRepository(client, "merchant-1")
            repository.listThreads("merchant-1")
            assertIs<ApiResult.Success<*>>(repository.message("thread-1", " Fixture reply "))
            assertIs<ApiResult.Success<*>>(repository.reject("thread-1", "Fixture rejection"))
            assertEquals("MESSAGE", requests[0].getValue("offerType").jsonPrimitive.content)
            assertEquals("Fixture reply", requests[0].getValue("message").jsonPrimitive.content)
            assertFalse(requests[0].containsKey("expectedOfferId"))
            assertEquals("REJECT", requests[1].getValue("offerType").jsonPrimitive.content)
            assertEquals("offer-c1", requests[1].getValue("expectedOfferId").jsonPrimitive.content)
            assertNotEquals(requests[0].getValue("idempotencyKey"), requests[1].getValue("idempotencyKey"))
        }
    }

    private inline fun <T> VendorNegotiationApiClient.use(block: (VendorNegotiationApiClient) -> T): T = try { block(this) } finally { close() }
}
