package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.network.*
import dev.orestegabo.sequovendor.core.store.*
import dev.orestegabo.sequovendor.model.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*

class VendorStoreClientTest {
    private val url = "http://127.0.0.1:55840"
    private fun state(merchant: String = "merchant-a", configured: Boolean = true, open: Boolean = false, key: String? = null) = """{
      "merchantId":"$merchant","configured":$configured,"open":$open,"acceptingOrders":$open,
      "openStatus":"${if (configured) if (open) "OPEN" else "PAUSED" else "NOT_CONFIGURED"}",
      "weekdayHours":"","weekendHours":"","openingHours":[],"temporaryExceptions":[],
      "temporaryClosure":null,"closureReason":null,"timezone":${if (configured) "\"Africa/Lome\"" else "null"},
      "updatedAt":"2026-11-08T10:00:00Z","canAcceptOrders":${!configured || open},
      "history":${if (key == null) "[]" else """[{"id":"change","actorUserId":"merchant-user","reason":"Reviewed","idempotencyKey":"$key","createdAt":"2026-11-08T10:00:00Z"}]"""}
    }"""
    private fun client(engine: MockEngine) = VendorStoreApiClient(createSequoVendorHttpClient(url, accessTokenProvider = { "merchant-fixture" }, engine = engine))

    @Test fun realFactoryUsesAuthenticatedMerchantReadWhileDemoIsExplicit() = runBlocking<Unit> {
        var calls = 0
        val repository = createVendorStoreRepository(false, DemoVendorData.store()) {
            client(MockEngine { request ->
                calls++; assertEquals("/api/merchant/store", request.url.encodedPath)
                assertEquals("merchant-a", request.url.parameters["merchantId"])
                assertEquals("Bearer merchant-fixture", request.headers[HttpHeaders.Authorization])
                respond(state(), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            })
        }
        val loaded = assertIs<ApiResult.Success<StoreAvailabilityState>>(repository.getAvailability("merchant-a")).value
        assertFalse(loaded.open); assertTrue(loaded.configured); assertTrue(loaded.openingHours.isEmpty()); assertEquals(1, calls)
        repository.close()
        val demo = createVendorStoreRepository(true, DemoVendorData.store()) { error("Demo must not create a network client") }
        assertIs<ApiResult.Success<StoreAvailabilityState>>(demo.getAvailability("demo-store"))
    }

    @Test fun deniedWrongMerchantAndCancelledReadsNeverBecomeLocalSuccess() = runBlocking<Unit> {
        val denied = NetworkVendorStoreRepository(client(MockEngine { respond("{}", HttpStatusCode.Forbidden, headersOf(HttpHeaders.ContentType, "application/json")) }))
        assertEquals(ApiFailureKind.PermissionDenied, assertIs<ApiResult.Failure>(denied.getAvailability("merchant-a")).error.kind)
        assertIs<ApiResult.Failure>(denied.setOpen("merchant-a", true)); denied.close()
        val wrong = NetworkVendorStoreRepository(client(MockEngine { respond(state("merchant-b"), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }))
        assertIs<ApiResult.Failure>(wrong.getAvailability("merchant-a")); wrong.close()
        val cancelled = NetworkVendorStoreRepository(client(MockEngine { throw CancellationException("Fixture account switched") }))
        assertFailsWith<CancellationException> { cancelled.getAvailability("merchant-a") }; cancelled.close()
    }

    @Test fun uncertainSaveRetainsExactKeyAndBodyAndRequiresServerHistoryReadback() = runBlocking<Unit> {
        val writes = mutableListOf<String>(); var acceptedKey: String? = null; var failReadback = false
        val repository = NetworkVendorStoreRepository(client(MockEngine { request ->
            if (request.method == HttpMethod.Put) {
                val body = (request.body as TextContent).text; writes += body
                val key = Json.parseToJsonElement(body).jsonObject.getValue("idempotencyKey").jsonPrimitive.content
                acceptedKey = key; failReadback = writes.size == 1
                respond("""{"record":${state(key = key)},"operation":{"id":"change","actorUserId":"merchant-user","reason":"Reviewed","idempotencyKey":"$key","createdAt":"2026-11-08T10:00:00Z"}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else if (failReadback) respond("{}", HttpStatusCode.ServiceUnavailable, headersOf(HttpHeaders.ContentType, "application/json"))
            else respond(state(open = acceptedKey == null, key = acceptedKey), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        assertIs<ApiResult.Success<StoreAvailabilityState>>(repository.getAvailability("merchant-a"))
        assertIs<ApiResult.Failure>(repository.setOpen("merchant-a", false, "Stock count"))
        assertTrue(repository.hasPending("merchant-a"))
        val retried = assertIs<ApiResult.Success<StoreAvailabilityState>>(repository.retryPending("merchant-a")).value
        assertFalse(retried.open); assertEquals(2, writes.size); assertEquals(writes[0], writes[1]); assertFalse(repository.hasPending("merchant-a"))
        repository.close()
    }

    @Test fun unconfiguredReadKeepsLegacyOrderingWithoutInventingWeeklyHours() = runBlocking<Unit> {
        var body: String? = null; var key: String? = null
        val repository = NetworkVendorStoreRepository(client(MockEngine { request ->
            if (request.method == HttpMethod.Put) {
                body = (request.body as TextContent).text
                key = Json.parseToJsonElement(requireNotNull(body)).jsonObject.getValue("idempotencyKey").jsonPrimitive.content
                respond("""{"record":${state(open = true, key = key)},"operation":{"id":"change","actorUserId":"merchant-user","reason":"Reviewed","idempotencyKey":"$key","createdAt":"2026-11-08T10:00:00Z"}}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else respond(if (key == null) state(configured = false) else state(open = true, key = key), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }))
        val initial = assertIs<ApiResult.Success<StoreAvailabilityState>>(repository.getAvailability("merchant-a")).value
        assertFalse(initial.configured); assertFalse(initial.open); assertTrue(initial.canAcceptOrders == true); assertTrue(initial.openingHours.isEmpty())
        assertIs<ApiResult.Success<StoreAvailabilityState>>(repository.setTemporaryClosureReason("merchant-a", "Contact before pickup"))
        val sent = Json.parseToJsonElement(requireNotNull(body)).jsonObject
        assertEquals("true", sent.getValue("open").jsonPrimitive.content)
        assertEquals("Africa/Lome", sent.getValue("timezone").jsonPrimitive.content)
        assertEquals("[]", sent.getValue("openingHours").toString())
        assertTrue(sent["temporaryClosure"] == null || sent["temporaryClosure"].toString() == "null")
        repository.close()
    }
}
