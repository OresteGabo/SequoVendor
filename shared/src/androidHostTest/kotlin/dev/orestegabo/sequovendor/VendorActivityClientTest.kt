package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.activity.MerchantActivityApiClient
import dev.orestegabo.sequovendor.core.activity.MerchantActivitySnapshot
import dev.orestegabo.sequovendor.core.activity.MerchantSettlementStage
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VendorActivityClientTest {
    private val baseUrl = "https://merchant-activity.sequo.test"
    private val headers = headersOf(HttpHeaders.ContentType, "application/json")
    private val order = """
        {"id":"sub-A","subOrderCode":"SQ-1:A","orderId":"SQ-1","merchantId":"merchant-A",
         "status":"HANDED_TO_COURIER","itemSubtotalCfa":2000,"commissionRateBps":1500,"commissionCfa":300,
         "merchantNetCfa":1700,"packageCount":1,"updatedAt":"2026-11-08T10:00:00Z",
         "orderStatus":"DELIVERED","orderPaymentStatus":"Validated","orderCreatedAt":"2026-11-01T10:00:00Z",
         "deliveredAt":"2026-11-08T10:00:00Z",
         "lines":[{"id":"line-A","productId":"bag","productName":"Fixture bag","quantity":2,"unitPriceCfa":1000,"lineTotalCfa":2000}]}
    """.trimIndent()
    private val returnRecord = """
        {"id":"return-1","publicReference":"R98-765-4Q4","orderReference":"C12-345-6E6","orderId":"SQ-1","status":"RefundPending","requestedAt":"2026-11-08T11:00:00Z",
         "droppedAt":"2026-11-08T12:00:00Z","collectedBySequoAt":"2026-11-08T13:00:00Z",
         "receivedBySequoAt":"2026-11-08T14:00:00Z","inspectionDecision":"ACCEPTED",
         "refundInstructionState":"REQUESTED","refundPaymentStatus":"NOT_SUBMITTED","orderPaymentStatus":"Validated",
         "lines":[{"productId":"bag","productName":"Fixture bag","quantity":2}]}
    """.trimIndent()
    private fun payout(status: String) = """
        {"id":"payout-$status","publicReference":"F00-000-09J","sourceOrderItemReference":"J00-11Z-ZHV","orderReference":"C12-345-6E6","merchantId":"merchant-A","orderId":"SQ-1","sourceOrderItemId":"line-A",
         "merchantNetCfa":1700,"commissionCfa":300,"platformMarginCfa":0,
         "packageReceivedAt":"2026-11-08T10:00:00Z","payoutEligibleAt":"2026-11-11T10:00:00Z",
         "payoutDueBy":"2026-11-15T10:00:00Z","status":"$status","workflowType":"DeliveryConfirmed",
         "activeReturnHold":true,"activeDisputeHold":false,"ledgerEntries":[]}
    """.trimIndent()

    @Test
    fun authenticatedHistoryReturnsAndSettlementReadsKeepDistinctServerStates() = runBlocking<Unit> {
        val paths = mutableListOf<String>()
        val statuses = listOf("Accrued", "HeldReturnWindow", "HeldDispute", "Eligible", "Batched", "Approved", "SentToProvider", "Paid", "Failed", "Adjusted", "NEW_STATE")
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, accessTokenProvider = { "merchant-fixture" }, engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("merchant-A", request.url.parameters["merchantId"])
            assertEquals("Bearer merchant-fixture", request.headers[HttpHeaders.Authorization])
            paths += request.url.encodedPath
            val body = when (request.url.encodedPath) {
                "/api/merchant/sub-orders" -> { assertEquals("true", request.url.parameters["history"]); "[$order]" }
                "/api/returns/merchant" -> "[$returnRecord]"
                "/api/settlements/merchant-payouts" -> statuses.joinToString(",", "[", "]") { payout(it) }
                else -> error("Unexpected route")
            }
            respond(body, HttpStatusCode.OK, headers)
        }))
        try {
            val data = assertIs<ApiResult.Success<MerchantActivitySnapshot>>(client.load("merchant-A")).value
            assertEquals(listOf("/api/merchant/sub-orders", "/api/returns/merchant", "/api/settlements/merchant-payouts"), paths)
            assertEquals("DELIVERED", data.orders.single().orderStatus)
            assertEquals("R98-765-4Q4", data.returns.single().publicReference)
            assertEquals("C12-345-6E6", data.returns.single().orderReference)
            assertEquals("F00-000-09J", data.payouts.first().publicReference)
            assertEquals("J00-11Z-ZHV", data.payouts.first().sourceOrderItemReference)
            assertEquals("line-A", data.payouts.first().sourceOrderItemId)
            assertEquals("Validated", data.orders.single().orderPaymentStatus)
            assertEquals("2026-11-01T10:00:00Z", data.orders.single().orderCreatedAt)
            assertEquals(2, data.orders.single().lines.single().quantity)
            assertEquals("REQUESTED", data.returns.single().refundInstructionState)
            assertEquals("NOT_SUBMITTED", data.returns.single().refundPaymentStatus)
            assertEquals("Validated", data.returns.single().orderPaymentStatus)
            assertEquals("ACCEPTED", data.returns.single().inspectionDecision)
            assertEquals("2026-11-08T13:00:00Z", data.returns.single().collectedBySequoAt)
            assertEquals(List(6) { MerchantSettlementStage.PENDING } + listOf(MerchantSettlementStage.ISSUED, MerchantSettlementStage.SETTLED, MerchantSettlementStage.FAILED, MerchantSettlementStage.ADJUSTED, MerchantSettlementStage.UNKNOWN), data.payouts.map { it.recordedStage })
            assertTrue(data.payouts.all { it.merchantNetCfa == 1700 && it.activeReturnHold })
        } finally { client.close() }
    }

    @Test
    fun nullableLegacyOrderAndReturnMetadataDoNotBecomeValidatedPayments() = runBlocking<Unit> {
        val legacyOrder = """{"id":"legacy-sub","subOrderCode":"legacy","orderId":"old-order","merchantId":"merchant-A","status":"CANCELLED","itemSubtotalCfa":0,"commissionRateBps":1500,"commissionCfa":0,"merchantNetCfa":0,"packageCount":0,"updatedAt":"2026-11-08T10:00:00Z"}"""
        val legacyReturn = """{"id":"legacy-return","orderId":"old-order","status":"RefundPending","requestedAt":"2026-11-08T10:00:00Z","refundInstructionState":"LEGACY_REVIEW_REQUIRED","refundPaymentStatus":"UNCONFIRMED","lines":[]}"""
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { request ->
            respond(when (request.url.encodedPath) {
                "/api/merchant/sub-orders" -> "[$legacyOrder]"
                "/api/returns/merchant" -> "[$legacyReturn]"
                else -> "[]"
            }, HttpStatusCode.OK, headers)
        }))
        try {
            val data = assertIs<ApiResult.Success<MerchantActivitySnapshot>>(client.load("merchant-A")).value
            assertNull(data.orders.single().orderPaymentStatus)
            assertNull(data.orders.single().orderStatus)
            assertNull(data.orders.single().deliveredAt)
            assertEquals("LEGACY_REVIEW_REQUIRED", data.returns.single().refundInstructionState)
            assertNull(data.returns.single().orderPaymentStatus)
            assertTrue(data.payouts.isEmpty())
        } finally { client.close() }
    }

    @Test
    fun failureInAnyModuleDoesNotPublishAnEmptyOrPartialFinancialSnapshot() = runBlocking<Unit> {
        var failingPath = "/api/merchant/sub-orders"
        var status = HttpStatusCode.Unauthorized
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { request ->
            if (request.url.encodedPath == failingPath) respond("{}", status, headers)
            else respond("[]", HttpStatusCode.OK, headers)
        }))
        try {
            for (path in listOf("/api/merchant/sub-orders", "/api/returns/merchant", "/api/settlements/merchant-payouts")) {
                failingPath = path
                for ((code, kind) in listOf(HttpStatusCode.Unauthorized to ApiFailureKind.SessionExpired, HttpStatusCode.Forbidden to ApiFailureKind.PermissionDenied, HttpStatusCode.ServiceUnavailable to ApiFailureKind.ServerUnavailable)) {
                    status = code
                    assertEquals(kind, assertIs<ApiResult.Failure>(client.load("merchant-A")).error.kind)
                }
            }
            failingPath = "none"
            val recovered = assertIs<ApiResult.Success<MerchantActivitySnapshot>>(client.load("merchant-A")).value
            assertTrue(recovered.orders.isEmpty() && recovered.returns.isEmpty() && recovered.payouts.isEmpty())
        } finally { client.close() }
    }

    @Test
    fun expiredSessionRefreshRetriesTheSameAuthorizedReadAndUsesNewBearer() = runBlocking<Unit> {
        var token = "expired-fixture"
        var reads = 0
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, accessTokenProvider = { token }, refreshAccessToken = { token = "refreshed-fixture"; true }, engine = MockEngine { request ->
            assertEquals("merchant-A", request.url.parameters["merchantId"])
            reads++
            if (reads == 1) respond("{}", HttpStatusCode.Unauthorized, headers)
            else { assertEquals("Bearer refreshed-fixture", request.headers[HttpHeaders.Authorization]); respond("[]", HttpStatusCode.OK, headers) }
        }))
        try {
            assertIs<ApiResult.Success<MerchantActivitySnapshot>>(client.load("merchant-A"))
            assertEquals(4, reads)
        } finally { client.close() }
    }

    @Test
    fun merchantMismatchedOrderOrPayoutPayloadFailsClosed() = runBlocking<Unit> {
        var mismatchOrders = true
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { request ->
            respond(when (request.url.encodedPath) {
                "/api/merchant/sub-orders" -> "[${if (mismatchOrders) order.replace("merchant-A", "merchant-B") else order}]"
                "/api/returns/merchant" -> "[]"
                else -> "[${if (mismatchOrders) payout("Accrued") else payout("Accrued").replace("merchant-A", "merchant-B")}]"
            }, HttpStatusCode.OK, headers)
        }))
        try {
            assertEquals(ApiFailureKind.PermissionDenied, assertIs<ApiResult.Failure>(client.load("merchant-A")).error.kind)
            mismatchOrders = false
            assertEquals(ApiFailureKind.PermissionDenied, assertIs<ApiResult.Failure>(client.load("merchant-A")).error.kind)
        } finally { client.close() }
    }

    @Test
    fun cancelledReadIsPropagatedInsteadOfPublishingUnavailableMoney() = runBlocking<Unit> {
        val client = MerchantActivityApiClient(baseUrl, createSequoVendorHttpClient(baseUrl, engine = MockEngine { throw CancellationException("Fixture workspace changed") }))
        try { assertFailsWith<CancellationException> { client.load("merchant-A") } } finally { client.close() }
    }
}
