package dev.orestegabo.sequovendor.core.activity

import dev.orestegabo.sequovendor.core.fulfillment.MerchantSubOrderDto
import dev.orestegabo.sequovendor.core.fulfillment.toDomain
import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.ApiFailure
import dev.orestegabo.sequovendor.core.network.ApiFailureKind
import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.core.network.toApiFailure
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

class MerchantActivityApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
) {
    suspend fun load(merchantId: String): ApiResult<MerchantActivitySnapshot> = try {
        val orders = httpClient.get(NetworkConfig.apiUrl("merchant/sub-orders", baseUrl)) {
            parameter("merchantId", merchantId)
            parameter("history", true)
        }.body<List<MerchantSubOrderDto>>().map { it.toDomain() }
        val returns = httpClient.get(NetworkConfig.apiUrl("returns/merchant", baseUrl)) {
            parameter("merchantId", merchantId)
        }.body<List<MerchantReturnSnapshot>>()
        val payouts = httpClient.get(NetworkConfig.apiUrl("settlements/merchant-payouts", baseUrl)) {
            parameter("merchantId", merchantId)
        }.body<List<MerchantPayoutSnapshot>>()
        if (orders.any { it.merchantId != merchantId } || payouts.any { it.merchantId != merchantId }) {
            ApiResult.Failure(ApiFailure(ApiFailureKind.PermissionDenied))
        } else ApiResult.Success(MerchantActivitySnapshot(orders, returns, payouts))
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        ApiResult.Failure(error.toApiFailure())
    }

    fun close() = httpClient.close()
}

data class MerchantActivitySnapshot(
    val orders: List<MerchantSubOrder>,
    val returns: List<MerchantReturnSnapshot>,
    val payouts: List<MerchantPayoutSnapshot>,
)

@Serializable
data class MerchantReturnSnapshot(
    val id: String,
    val orderId: String,
    val status: String,
    val requestedAt: String,
    val droppedAt: String? = null,
    val collectedBySequoAt: String? = null,
    val receivedBySequoAt: String? = null,
    val inspectionDecision: String? = null,
    val refundInstructionState: String? = null,
    val refundPaymentStatus: String? = null,
    val orderPaymentStatus: String? = null,
    val lines: List<MerchantReturnLine> = emptyList(),
)

@Serializable
data class MerchantReturnLine(val productId: String, val productName: String, val quantity: Int)

@Serializable
data class MerchantPayoutSnapshot(
    val id: String,
    val merchantId: String,
    val orderId: String,
    val sourceOrderItemId: String,
    val merchantNetCfa: Int,
    val commissionCfa: Int,
    val platformMarginCfa: Int,
    val packageReceivedAt: String? = null,
    val payoutEligibleAt: String? = null,
    val payoutDueBy: String? = null,
    val status: String,
    val workflowType: String? = null,
    val activeReturnHold: Boolean = false,
    val activeDisputeHold: Boolean = false,
) {
    val recordedStage: MerchantSettlementStage
        get() = when (status) {
            "Accrued", "HeldReturnWindow", "HeldDispute", "Eligible", "Batched", "Approved" -> MerchantSettlementStage.PENDING
            "SentToProvider" -> MerchantSettlementStage.ISSUED
            "Paid" -> MerchantSettlementStage.SETTLED
            "Failed" -> MerchantSettlementStage.FAILED
            "Adjusted" -> MerchantSettlementStage.ADJUSTED
            else -> MerchantSettlementStage.UNKNOWN
        }
}

enum class MerchantSettlementStage { PENDING, ISSUED, SETTLED, FAILED, ADJUSTED, UNKNOWN }
