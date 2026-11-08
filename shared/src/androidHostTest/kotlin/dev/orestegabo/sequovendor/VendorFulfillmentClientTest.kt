package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.fulfillment.MerchantFulfillmentApiClient
import dev.orestegabo.sequovendor.core.fulfillment.NetworkMerchantFulfillmentRepository
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VendorFulfillmentClientTest {
    private val baseUrl = "https://fulfillment.sequo.test"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val snapshot = """
        {"id":"sub-1","publicReference":"MAB-CDE-FJZ","orderReference":"C12-345-6E6","subOrderCode":"SQ-1:merchant-1","orderId":"SQ-1","merchantId":"merchant-1",
         "status":"PREPARING","itemSubtotalCfa":6000,"commissionRateBps":1500,"commissionCfa":900,
         "merchantNetCfa":5100,"packageCount":0,"preparingAt":"2026-11-07T10:00:00Z",
         "packingDueAt":"2026-11-09T09:00:00Z","updatedAt":"2026-11-07T10:00:00Z",
         "lines":[{"id":"line-1","productId":"rice","productName":"Rice plate","quantity":3,
                   "unitPriceCfa":2000,"lineTotalCfa":6000,"optionLabel":"Plantain"}]}
    """.trimIndent()

    @Test
    fun authenticatedQueueDecodesServerQuantitiesOptionsAndPreparationTarget() = runBlocking<Unit> {
        val client = MerchantFulfillmentApiClient(baseUrl, createSequoVendorHttpClient(
            baseUrl = baseUrl,
            accessTokenProvider = { "merchant-fixture" },
            engine = MockEngine { request ->
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("/api/merchant/sub-orders", request.url.encodedPath)
                assertEquals("merchant-1", request.url.parameters["merchantId"])
                assertEquals("Bearer merchant-fixture", request.headers[HttpHeaders.Authorization])
                respond("[$snapshot]", HttpStatusCode.OK, jsonHeaders)
            },
        ))
        try {
            val order = client.listSubOrders("merchant-1").single()
            assertEquals(MerchantSubOrderStatus.PREPARING, order.status)
            assertEquals("MAB-CDE-FJZ", order.displayReference)
            assertEquals("C12-345-6E6", order.displayOrderReference)
            assertEquals("sub-1", order.id)
            assertEquals("SQ-1:merchant-1", order.subOrderCode)
            assertEquals("SQ-legacy:merchant", MerchantSubOrder("legacy", "SQ-legacy:merchant").displayReference)
            assertEquals("2026-11-09T09:00:00Z", order.packingDueAt)
            assertEquals(3, order.lines.single().quantity)
            assertEquals("Plantain", order.lines.single().optionLabel)
        } finally {
            client.close()
        }
    }

    @Test
    fun readyRequestCarriesAuthorizedMerchantAndPackageCountAndPreservesErrors() = runBlocking<Unit> {
        var calls = 0
        val client = MerchantFulfillmentApiClient(baseUrl, createSequoVendorHttpClient(
            baseUrl = baseUrl,
            accessTokenProvider = { "merchant-fixture" },
            engine = MockEngine { request ->
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/api/merchant/sub-orders/sub-1/mark-packed", request.url.encodedPath)
                assertEquals(
                    Json.parseToJsonElement("""{"merchantId":"merchant-1","packageCount":2}"""),
                    Json.parseToJsonElement((request.body as TextContent).text),
                )
                calls++
                if (calls == 1) {
                    respond(snapshot.replace("PREPARING", "PACKED_READY").replace("\"packageCount\":0", "\"packageCount\":2"),
                        HttpStatusCode.OK, jsonHeaders)
                } else {
                    respond("""{"code":"merchant_scope_mismatch","message":"Merchant access denied."}""", HttpStatusCode.Forbidden, jsonHeaders)
                }
            },
        ))
        try {
            val repository = NetworkMerchantFulfillmentRepository(client)
            val saved = assertIs<ApiResult.Success<MerchantSubOrder>>(repository.markPacked("sub-1", "merchant-1", 2))
            assertEquals(MerchantSubOrderStatus.PACKED_READY,
                saved.value.status)
            assertIs<ApiResult.Failure>(repository.markPacked("sub-1", "merchant-1", 2))
        } finally {
            client.close()
        }
    }
}
