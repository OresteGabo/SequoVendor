package dev.orestegabo.sequovendor.core.fulfillment

import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus

enum class VendorOrderAction {
    Accept,
    StartPreparation,
    MarkPacked,
    Handoff,
    Reject,
}

fun MerchantSubOrderStatus.primaryVendorAction(): VendorOrderAction? =
    when (this) {
        MerchantSubOrderStatus.MERCHANT_PENDING -> VendorOrderAction.Accept
        MerchantSubOrderStatus.ACCEPTED -> VendorOrderAction.StartPreparation
        MerchantSubOrderStatus.PREPARING -> VendorOrderAction.MarkPacked
        MerchantSubOrderStatus.PACKED_READY -> VendorOrderAction.Handoff
        MerchantSubOrderStatus.HANDED_TO_COURIER,
        MerchantSubOrderStatus.REJECTED,
        MerchantSubOrderStatus.CANCELLED,
        -> null
    }

fun MerchantSubOrderStatus.canRejectByMerchant(): Boolean =
    this == MerchantSubOrderStatus.MERCHANT_PENDING || this == MerchantSubOrderStatus.ACCEPTED
