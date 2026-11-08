package dev.orestegabo.sequovendor.core.negotiation

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.VendorNegotiationMessage
import dev.orestegabo.sequovendor.model.VendorNegotiationStatus
import dev.orestegabo.sequovendor.model.VendorNegotiationThread
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.Serializable

@Serializable
internal data class NegotiationThreadDto(
    val id: String,
    val merchantId: String,
    val productId: String,
    val productName: String? = null,
    val status: String,
    val lastOfferCfa: Int? = null,
    val acceptedPriceCfa: Int? = null,
    val customerAttemptsUsed: Int = 0,
    val merchantAttemptsUsed: Int = 0,
    val maxCustomerAttempts: Int = 3,
    val maxMerchantAttempts: Int = 3,
    val expiresAt: String? = null,
    val nextActor: String? = null,
    val bargainingEnabled: Boolean = false,
    val offers: List<NegotiationOfferDto> = emptyList(),
) {
    fun toDomain(): VendorNegotiationThread {
        val pending = offers.lastOrNull { it.actorType == "CUSTOMER" && it.offerType == "OFFER" }
        val state = when (status) {
            "OPEN" -> when (nextActor) {
                "MERCHANT" -> VendorNegotiationStatus.CUSTOMER_OFFERED
                "CUSTOMER" -> VendorNegotiationStatus.MERCHANT_COUNTERED
                else -> VendorNegotiationStatus.OPEN
            }
            "ACCEPTED" -> VendorNegotiationStatus.ACCEPTED
            "REJECTED" -> VendorNegotiationStatus.REJECTED
            "EXPIRED" -> VendorNegotiationStatus.EXPIRED
            "CANCELLED" -> VendorNegotiationStatus.CANCELLED
            "LOCKED_ATTEMPTS_EXHAUSTED" -> VendorNegotiationStatus.LOCKED
            else -> VendorNegotiationStatus.UNKNOWN
        }
        return VendorNegotiationThread(
            id = id, merchantId = merchantId, productId = productId, productName = productName ?: productId,
            customerOfferCfa = pending?.amountCfa ?: 0,
            sellerCounterCfa = offers.lastOrNull { it.actorType == "MERCHANT" && it.offerType == "COUNTER_OFFER" }?.amountCfa,
            customerAttemptsRemaining = (maxCustomerAttempts - customerAttemptsUsed).coerceAtLeast(0),
            merchantAttemptsRemaining = (maxMerchantAttempts - merchantAttemptsUsed).coerceAtLeast(0),
            lockExpiresAt = expiresAt, status = state, localDemo = false,
            pendingCustomerOfferId = pending?.id,
            merchantActionAllowed = status == "OPEN" && nextActor == "MERCHANT" && bargainingEnabled && pending != null,
            messages = offers.map { VendorNegotiationMessage(it.id, it.actorType, it.offerType, it.amountCfa, it.message, it.createdAt) },
            acceptedPriceCfa = acceptedPriceCfa, maxMerchantAttempts = maxMerchantAttempts,
        )
    }
}

@Serializable
internal data class NegotiationOfferDto(
    val id: String,
    val actorType: String,
    val offerType: String,
    val amountCfa: Int? = null,
    val message: String? = null,
    val createdAt: String,
)

@Serializable
internal data class NegotiationActionDto(
    val offerType: String,
    val amountCfa: Int? = null,
    val message: String? = null,
    val expectedOfferId: String? = null,
    val idempotencyKey: String,
)

class VendorNegotiationApiClient(
    private val baseUrl: String = NetworkConfig.BaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
) {
    suspend fun list(merchantId: String): List<VendorNegotiationThread> =
        httpClient.get(NetworkConfig.apiUrl("bargaining/threads", baseUrl)) {
            parameter("merchantId", merchantId)
        }.body<List<NegotiationThreadDto>>().map { it.toDomain() }

    suspend fun get(threadId: String): VendorNegotiationThread =
        httpClient.get(NetworkConfig.apiUrl("bargaining/threads/${threadId.encodeURLPathPart()}", baseUrl))
            .body<NegotiationThreadDto>().toDomain()

    internal suspend fun submit(threadId: String, request: NegotiationActionDto) {
        httpClient.post(NetworkConfig.apiUrl("bargaining/threads/${threadId.encodeURLPathPart()}/offers", baseUrl)) {
            setBody(request)
        }.body<NegotiationThreadDto>()
    }

    fun close() = httpClient.close()
}
