package dev.orestegabo.sequovendor.core.fulfillment

import dev.orestegabo.sequovendor.core.network.ApiResult
import dev.orestegabo.sequovendor.model.DemoVendorData
import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus

class FakeMerchantFulfillmentRepository(
    initialOrders: List<MerchantSubOrder> = DemoVendorData.orders(),
) : MerchantFulfillmentRepository {
    private var orders: List<MerchantSubOrder> = initialOrders

    override suspend fun listSubOrders(
        merchantId: String,
        status: MerchantSubOrderStatus?,
    ): ApiResult<List<MerchantSubOrder>> =
        ApiResult.Success(
            orders
                .filter { it.merchantId == null || it.merchantId == merchantId }
                .filter { status == null || it.status == status },
        )

    override suspend fun getSubOrder(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        ApiResult.Success(findOrder(subOrderId, merchantId))

    override suspend fun getSla(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSlaInfo> =
        ApiResult.Success(findOrder(subOrderId, merchantId).sla)

    override suspend fun accept(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        updateOrder(subOrderId, merchantId) {
            copy(status = MerchantSubOrderStatus.ACCEPTED, acceptedAt = DemoInstant, updatedAt = DemoInstant)
        }

    override suspend fun startPreparation(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        updateOrder(subOrderId, merchantId) {
            copy(status = MerchantSubOrderStatus.PREPARING, preparingAt = DemoInstant, updatedAt = DemoInstant)
        }

    override suspend fun markPacked(
        subOrderId: String,
        merchantId: String,
        packageCount: Int,
    ): ApiResult<MerchantSubOrder> =
        updateOrder(subOrderId, merchantId) {
            copy(
                status = MerchantSubOrderStatus.PACKED_READY,
                packageCount = packageCount.coerceAtLeast(1),
                packedReadyAt = DemoInstant,
                updatedAt = DemoInstant,
            )
        }

    override suspend fun handoff(
        subOrderId: String,
        merchantId: String,
    ): ApiResult<MerchantSubOrder> =
        updateOrder(subOrderId, merchantId) {
            copy(status = MerchantSubOrderStatus.HANDED_TO_COURIER, handedToCourierAt = DemoInstant, updatedAt = DemoInstant)
        }

    override suspend fun reject(
        subOrderId: String,
        merchantId: String,
        reason: String,
    ): ApiResult<MerchantSubOrder> =
        updateOrder(subOrderId, merchantId) {
            copy(status = MerchantSubOrderStatus.REJECTED, rejectionReason = reason.trim(), updatedAt = DemoInstant)
        }

    private fun findOrder(subOrderId: String, merchantId: String): MerchantSubOrder =
        orders.first { it.id == subOrderId && (it.merchantId == null || it.merchantId == merchantId) }

    private fun updateOrder(
        subOrderId: String,
        merchantId: String,
        transform: MerchantSubOrder.() -> MerchantSubOrder,
    ): ApiResult<MerchantSubOrder> {
        val updated = findOrder(subOrderId, merchantId).transform()
        orders = orders.map { if (it.id == subOrderId) updated else it }
        return ApiResult.Success(updated)
    }

    private companion object {
        const val DemoInstant = "2026-10-10T12:45:00Z"
    }
}
