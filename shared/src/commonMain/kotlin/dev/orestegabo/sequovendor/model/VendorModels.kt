package dev.orestegabo.sequovendor.model

data class VendorSession(
    val merchantId: String,
    val displayName: String,
    val accessToken: String? = null,
    val localMode: Boolean = true,
)

enum class VendorSection {
    Orders,
    Catalog,
    Store,
}

enum class MerchantSubOrderStatus {
    MerchantPending,
    Accepted,
    Preparing,
    PackedReady,
    HandedToCourier,
    Rejected,
    Cancelled,
}

data class MerchantSubOrder(
    val id: String,
    val code: String,
    val status: MerchantSubOrderStatus,
    val merchantNetCfa: Int,
    val packageCount: Int,
    val updatedAt: String,
    val sellerResponseDueAt: String?,
    val packingDueAt: String?,
)

data class CatalogProduct(
    val id: String,
    val name: String,
    val priceCfa: Int,
    val status: String,
    val cameraVerified: Boolean,
)

data class StoreStatus(
    val open: Boolean,
    val weekdayHours: String,
    val weekendHours: String,
)

object DemoVendorData {
    fun session() = VendorSession(
        merchantId = "merchant-demo-sequo",
        displayName = "Demo merchant",
    )

    fun orders() = listOf(
        MerchantSubOrder(
            id = "mso-demo-001",
            code = "SQ-M-1001",
            status = MerchantSubOrderStatus.MerchantPending,
            merchantNetCfa = 16_280,
            packageCount = 0,
            updatedAt = "2026-10-10T09:20:00Z",
            sellerResponseDueAt = "2026-10-10T18:00:00Z",
            packingDueAt = "2026-10-11T18:00:00Z",
        ),
        MerchantSubOrder(
            id = "mso-demo-002",
            code = "SQ-M-1002",
            status = MerchantSubOrderStatus.Preparing,
            merchantNetCfa = 8_910,
            packageCount = 1,
            updatedAt = "2026-10-10T10:15:00Z",
            sellerResponseDueAt = null,
            packingDueAt = "2026-10-11T10:15:00Z",
        ),
    )

    fun products() = listOf(
        CatalogProduct("prod-demo-001", "Fresh tilapia box", 4_500, "ACTIVE", cameraVerified = true),
        CatalogProduct("prod-demo-002", "Local cassava flour", 2_200, "ACTIVE", cameraVerified = false),
        CatalogProduct("prod-demo-003", "Weekend bundle", 12_000, "ARCHIVED", cameraVerified = true),
    )

    fun store() = StoreStatus(
        open = true,
        weekdayHours = "08:00-18:00",
        weekendHours = "09:00-14:00",
    )
}
