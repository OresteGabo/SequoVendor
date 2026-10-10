package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.fulfillment.MarkPackedRequestDto
import dev.orestegabo.sequovendor.core.fulfillment.MerchantActionRequestDto
import dev.orestegabo.sequovendor.core.fulfillment.RejectMerchantSubOrderRequestDto
import dev.orestegabo.sequovendor.core.fulfillment.VendorOrderAction
import dev.orestegabo.sequovendor.core.fulfillment.canRejectByMerchant
import dev.orestegabo.sequovendor.core.fulfillment.primaryVendorAction
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.SequoVendorApiException
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.core.network.defaultNetworkJson
import dev.orestegabo.sequovendor.core.network.retryAfterDelayMillis
import dev.orestegabo.sequovendor.core.network.toApiFailure
import dev.orestegabo.sequovendor.core.store.isValidOpeningHours
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.OpeningHoursDay
import dev.orestegabo.sequovendor.model.formatCfa
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString

class VendorLogicAndroidHostTest {

    @Test
    fun statusMapsToExpectedPrimaryAction() {
        assertEquals(VendorOrderAction.Accept, MerchantSubOrderStatus.MERCHANT_PENDING.primaryVendorAction())
        assertEquals(VendorOrderAction.StartPreparation, MerchantSubOrderStatus.ACCEPTED.primaryVendorAction())
        assertEquals(VendorOrderAction.MarkPacked, MerchantSubOrderStatus.PREPARING.primaryVendorAction())
        assertEquals(VendorOrderAction.Handoff, MerchantSubOrderStatus.PACKED_READY.primaryVendorAction())
        assertNull(MerchantSubOrderStatus.HANDED_TO_COURIER.primaryVendorAction())
        assertNull(MerchantSubOrderStatus.REJECTED.primaryVendorAction())
        assertNull(MerchantSubOrderStatus.CANCELLED.primaryVendorAction())

        assertTrue(MerchantSubOrderStatus.MERCHANT_PENDING.canRejectByMerchant())
        assertTrue(MerchantSubOrderStatus.ACCEPTED.canRejectByMerchant())
        assertFalse(MerchantSubOrderStatus.PREPARING.canRejectByMerchant())
    }

    @Test
    fun safeErrorMappingHidesRawBackendMessages() {
        val failure = SequoVendorApiException(
            statusCode = 429,
            responseBody = "{\"debug\":\"database detail that must not be shown\"}",
            retryAfterMillis = 12_000,
            message = "raw backend message",
        ).toApiFailure()

        assertEquals(ApiFailureKind.RateLimited, failure.kind)
        assertEquals(429, failure.statusCode)
        assertEquals(12_000, failure.retryAfterMillis)
        assertEquals(failure.kind.userMessage, failure.userMessage)
        assertEquals(30_000, retryAfterDelayMillis("30"))
        assertEquals(0, retryAfterDelayMillis("-4"))
        assertNull(retryAfterDelayMillis("not-seconds"))
    }

    @Test
    fun moneyFormattingUsesIntegerCfaGrouping() {
        assertEquals("0 CFA", 0.formatCfa())
        assertEquals("500 CFA", 500.formatCfa())
        assertEquals("16 280 CFA", 16_280.formatCfa())
        assertEquals("1 234 567 CFA", 1_234_567.formatCfa())
    }

    @Test
    fun openingHoursValidationAcceptsClosedDaysAndValidRangesOnly() {
        assertTrue(OpeningHoursDay(dayOfWeek = 1, opensAt = "", closesAt = "", closed = true).isValidOpeningHours())
        assertTrue(OpeningHoursDay(dayOfWeek = 1, opensAt = "08:00", closesAt = "18:00").isValidOpeningHours())
        assertFalse(OpeningHoursDay(dayOfWeek = 1, opensAt = "", closesAt = "18:00").isValidOpeningHours())
        assertFalse(OpeningHoursDay(dayOfWeek = 1, opensAt = "18:00", closesAt = "08:00").isValidOpeningHours())
        assertFalse(OpeningHoursDay(dayOfWeek = 1, opensAt = "24:00", closesAt = "25:00").isValidOpeningHours())
    }

    @Test
    fun orderActionRequestBodiesMatchBackendContract() {
        assertEquals(
            """{"merchantId":"merchant-1"}""",
            defaultNetworkJson.encodeToString(MerchantActionRequestDto("merchant-1")),
        )
        assertEquals(
            """{"merchantId":"merchant-1","packageCount":3}""",
            defaultNetworkJson.encodeToString(MarkPackedRequestDto("merchant-1", 3)),
        )
        assertEquals(
            """{"merchantId":"merchant-1","reason":"Out of stock"}""",
            defaultNetworkJson.encodeToString(RejectMerchantSubOrderRequestDto("merchant-1", "Out of stock")),
        )
    }

    @Test
    fun bearerAuthRefreshRetriesOnceWithNewToken() = runBlocking {
        var token = "expired-token"
        var refreshCount = 0
        val observedAuthorizations = mutableListOf<String?>()
        val engine = MockEngine { request ->
            observedAuthorizations += request.headers[HttpHeaders.Authorization]
            if (observedAuthorizations.size == 1) {
                respond("{}", HttpStatusCode.Unauthorized)
            } else {
                respond("""{"ok":true}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        val client = createSequoVendorHttpClient(
            baseUrl = "https://test.sequoservice.local",
            accessTokenProvider = { token },
            refreshAccessToken = {
                refreshCount += 1
                token = "fresh-token"
                true
            },
            engine = engine,
        )

        val body = client.get(NetworkConfig.apiUrl("protected", "https://test.sequoservice.local")).bodyAsText()

        assertEquals("""{"ok":true}""", body)
        assertEquals(1, refreshCount)
        assertEquals(listOf<String?>("Bearer expired-token", "Bearer fresh-token"), observedAuthorizations)
        client.close()
    }
}
