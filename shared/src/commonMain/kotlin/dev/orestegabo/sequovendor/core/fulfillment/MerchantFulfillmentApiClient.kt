package dev.orestegabo.sequovendor.core.fulfillment

import dev.orestegabo.sequovendor.core.network.NetworkConfig
import dev.orestegabo.sequovendor.core.network.createSequoVendorHttpClient
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class MerchantFulfillmentApiClient(
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoVendorHttpClient(baseUrl),
) {
    suspend fun listSubOrders(
        merchantId: String,
        status: MerchantSubOrderStatus? = null,
    ): List<MerchantSubOrder> =
        httpClient.get(NetworkConfig.apiUrl("merchant/sub-orders", baseUrl)) {
            parameter("merchantId", merchantId)
            status?.let { parameter("status", it.name) }
        }.body<List<MerchantSubOrderDto>>().map { it.toDomain() }

    suspend fun getSubOrder(
        subOrderId: String,
        merchantId: String,
    ): MerchantSubOrder =
        httpClient.get(NetworkConfig.apiUrl("merchant/sub-orders/$subOrderId", baseUrl)) {
            parameter("merchantId", merchantId)
        }.body<MerchantSubOrderDto>().toDomain()

    suspend fun getSla(
        subOrderId: String,
        merchantId: String,
    ): MerchantSlaInfo =
        httpClient.get(NetworkConfig.apiUrl("merchant/sub-orders/$subOrderId/sla", baseUrl)) {
            parameter("merchantId", merchantId)
        }.body<MerchantFulfillmentSlaDto>().toDomain()

    suspend fun accept(
        subOrderId: String,
        merchantId: String,
    ): MerchantSubOrder =
        postMerchantAction("merchant/sub-orders/$subOrderId/accept", MerchantActionRequestDto(merchantId))

    suspend fun startPreparation(
        subOrderId: String,
        merchantId: String,
    ): MerchantSubOrder =
        postMerchantAction("merchant/sub-orders/$subOrderId/start-preparation", MerchantActionRequestDto(merchantId))

    suspend fun markPacked(
        subOrderId: String,
        merchantId: String,
        packageCount: Int,
    ): MerchantSubOrder =
        postMerchantAction("merchant/sub-orders/$subOrderId/mark-packed", MarkPackedRequestDto(merchantId, packageCount))

    suspend fun handoff(
        subOrderId: String,
        merchantId: String,
    ): MerchantSubOrder =
        postMerchantAction("merchant/sub-orders/$subOrderId/handoff", MerchantActionRequestDto(merchantId))

    suspend fun reject(
        subOrderId: String,
        merchantId: String,
        reason: String,
    ): MerchantSubOrder =
        postMerchantAction("merchant/sub-orders/$subOrderId/reject", RejectMerchantSubOrderRequestDto(merchantId, reason))

    fun close() {
        httpClient.close()
    }

    private suspend inline fun <reified TRequest : Any> postMerchantAction(
        path: String,
        body: TRequest,
    ): MerchantSubOrder =
        httpClient.post(NetworkConfig.apiUrl(path, baseUrl)) {
            setBody(body)
        }.body<MerchantSubOrderDto>().toDomain()
}
