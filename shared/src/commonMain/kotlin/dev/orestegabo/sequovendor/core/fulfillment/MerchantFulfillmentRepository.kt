package dev.orestegabo.sequovendor.core.fulfillment

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.core.network.safeApiCall
import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus

interface MerchantFulfillmentRepository {
    suspend fun listSubOrders(
        merchantId: String,
        status: MerchantSubOrderStatus? = null,
    ): ApiResult<List<MerchantSubOrder>>

    suspend fun getSubOrder(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder>

    suspend fun getSla(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSlaInfo>

    suspend fun accept(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder>

    suspend fun startPreparation(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder>

    suspend fun markPacked(
        subOrderId: String,
        merchantId: String,
        packageCount: Int,
    ): ApiResult<MerchantSubOrder>

    suspend fun handoff(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder>

    suspend fun reject(
        subOrderId: String,
        merchantId: String,
        reason: String,
    ): ApiResult<MerchantSubOrder>
}

class NetworkMerchantFulfillmentRepository(
    private val apiClient: MerchantFulfillmentApiClient,
) : MerchantFulfillmentRepository {
    override suspend fun listSubOrders(
        merchantId: String,
        status: MerchantSubOrderStatus?,
    ): ApiResult<List<MerchantSubOrder>> =
        safeApiCall { apiClient.listSubOrders(merchantId, status) }

    override suspend fun getSubOrder(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.getSubOrder(subOrderId, merchantId) }

    override suspend fun getSla(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSlaInfo> =
        safeApiCall { apiClient.getSla(subOrderId, merchantId) }

    override suspend fun accept(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.accept(subOrderId, merchantId) }

    override suspend fun startPreparation(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.startPreparation(subOrderId, merchantId) }

    override suspend fun markPacked(
        subOrderId: String,
        merchantId: String,
        packageCount: Int,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.markPacked(subOrderId, merchantId, packageCount) }

    override suspend fun handoff(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.handoff(subOrderId, merchantId) }

    override suspend fun reject(
        subOrderId: String,
        merchantId: String,
        reason: String,
    ): ApiResult<MerchantSubOrder> =
        safeApiCall { apiClient.reject(subOrderId, merchantId, reason) }
}
