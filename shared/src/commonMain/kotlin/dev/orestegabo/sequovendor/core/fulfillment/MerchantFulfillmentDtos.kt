package dev.orestegabo.sequovendor.core.fulfillment

import dev.orestegabo.sequovendor.model.MerchantSlaInfo
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class MerchantSubOrderDto(
    val id: String,
    val subOrderCode: String,
    val orderId: String,
    val merchantId: String,
    val status: MerchantSubOrderStatus,
    val itemSubtotalCfa: Int,
    val commissionRateBps: Int,
    val commissionCfa: Int,
    val merchantNetCfa: Int,
    val packageCount: Int,
    val acceptedAt: String? = null,
    val preparingAt: String? = null,
    val packedReadyAt: String? = null,
    val handedToCourierAt: String? = null,
    val rejectionReason: String? = null,
    val sellerResponseDueAt: String? = null,
    val packingDueAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String,
)

@Serializable
data class MerchantFulfillmentSlaDto(
    val subOrderId: String,
    val status: MerchantSubOrderStatus,
    val sellerResponseDueAt: String? = null,
    val packingDueAt: String? = null,
    val overdue: Boolean,
    val overdueReason: String? = null,
)

@Serializable
data class MerchantActionRequestDto(
    val merchantId: String,
)

@Serializable
data class MarkPackedRequestDto(
    val merchantId: String,
    val packageCount: Int,
)

@Serializable
data class RejectMerchantSubOrderRequestDto(
    val merchantId: String,
    val reason: String,
)

fun MerchantSubOrderDto.toDomain(): MerchantSubOrder =
    MerchantSubOrder(
        id = id,
        subOrderCode = subOrderCode,
        orderId = orderId,
        merchantId = merchantId,
        status = status,
        itemSubtotalCfa = itemSubtotalCfa,
        commissionRateBps = commissionRateBps,
        commissionCfa = commissionCfa,
        merchantNetCfa = merchantNetCfa,
        packageCount = packageCount,
        acceptedAt = acceptedAt,
        preparingAt = preparingAt,
        packedReadyAt = packedReadyAt,
        handedToCourierAt = handedToCourierAt,
        rejectionReason = rejectionReason,
        sellerResponseDueAt = sellerResponseDueAt,
        packingDueAt = packingDueAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun MerchantFulfillmentSlaDto.toDomain(): MerchantSlaInfo =
    MerchantSlaInfo(
        sellerResponseDueAt = sellerResponseDueAt,
        packingDueAt = packingDueAt,
        sellerResponseOverdue = overdue && status == MerchantSubOrderStatus.MERCHANT_PENDING,
        packingOverdue = overdue && status in setOf(
            MerchantSubOrderStatus.ACCEPTED,
            MerchantSubOrderStatus.PREPARING,
        ),
    )
