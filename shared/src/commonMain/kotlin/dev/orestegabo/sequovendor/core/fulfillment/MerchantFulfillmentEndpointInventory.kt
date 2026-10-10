package dev.orestegabo.sequovendor.core.fulfillment

object MerchantFulfillmentEndpointInventory {
    val vendorReady = listOf(
        "GET /api/merchant/sub-orders?merchantId={merchantId}&status={status?}",
        "GET /api/merchant/sub-orders/{subOrderId}?merchantId={merchantId}",
        "GET /api/merchant/sub-orders/{subOrderId}/sla?merchantId={merchantId}",
        "POST /api/merchant/sub-orders/{subOrderId}/accept",
        "POST /api/merchant/sub-orders/{subOrderId}/start-preparation",
        "POST /api/merchant/sub-orders/{subOrderId}/mark-packed",
        "POST /api/merchant/sub-orders/{subOrderId}/handoff",
        "POST /api/merchant/sub-orders/{subOrderId}/reject",
    )

    val availableButNotVendorAppOwned = listOf(
        "POST /api/merchant/sub-orders",
        "POST /api/merchant/sub-orders/sla/publish-overdue",
        "GET /api/merchant/sub-orders/{subOrderId}/escalations",
        "POST /api/merchant/sub-orders/{subOrderId}/escalations",
        "GET /api/catalog/products?merchantId={merchantId}&includeArchived=true",
        "POST /api/catalog/products",
        "PATCH /api/catalog/products/{productId}",
        "POST /api/catalog/products/{productId}/archive",
        "GET /api/settlement/merchant-payouts",
        "GET /api/settlement/ledger",
    )
}
