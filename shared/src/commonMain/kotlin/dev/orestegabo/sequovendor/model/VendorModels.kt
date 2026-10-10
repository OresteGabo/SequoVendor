package dev.orestegabo.sequovendor.model

import kotlinx.serialization.Serializable

data class VendorSession(
    val merchantId: String,
    val displayName: String,
    val accessToken: String? = null,
    val authProvider: String? = null,
    val localMode: Boolean = true,
)

enum class VendorSection {
    Dashboard,
    Orders,
    Catalog,
    Store,
    Notifications,
    Account,
}

@Serializable
data class VendorUser(
    val id: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val provider: AuthProvider,
    val status: UserStatus,
    val active: Boolean,
    val roles: Set<RoleCode> = emptySet(),
    val merchantScopeIds: Set<String> = emptySet(),
) {
    val hasMerchantAccess: Boolean
        get() = active && roles.any { it.isMerchantOperator }
}

@Serializable
enum class AuthProvider {
    EMAIL,
    GOOGLE,
    FACEBOOK,
    APPLE,
    PASSKEY,
}

@Serializable
enum class UserStatus {
    PENDING,
    ACTIVE,
    LOCKED,
    SUSPENDED,
    DELETED,
}

@Serializable
enum class RoleCode {
    CUSTOMER,
    MERCHANT_OWNER,
    MERCHANT_STAFF,
    COURIER,
    RELAY_PARTNER,
    SUPPORT_AGENT,
    ADMIN,
    SUPER_ADMIN,
}

val RoleCode.isMerchantOperator: Boolean
    get() = this == RoleCode.MERCHANT_OWNER ||
        this == RoleCode.MERCHANT_STAFF ||
        this == RoleCode.ADMIN ||
        this == RoleCode.SUPER_ADMIN

@Serializable
data class MerchantProfile(
    val id: String,
    val ownerUserId: String? = null,
    val name: String,
    val status: String = "ACTIVE",
    val commissionRateBps: Int = 1500,
    val walletProvider: String? = null,
    val walletAccountRef: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class VendorDashboardSummary(
    val merchant: MerchantProfile? = null,
    val activeOrders: Int = 0,
    val pendingOrders: Int = 0,
    val preparingOrders: Int = 0,
    val readyOrders: Int = 0,
    val overdueSlaOrders: Int = 0,
    val todayNetCfa: Int = 0,
    val catalogProducts: Int = 0,
    val productsNeedingPhotos: Int = 0,
    val availability: StoreAvailabilityState? = null,
)

@Serializable
data class MerchantSubOrder(
    val id: String,
    val subOrderCode: String,
    val orderId: String? = null,
    val merchantId: String? = null,
    val status: MerchantSubOrderStatus = MerchantSubOrderStatus.MERCHANT_PENDING,
    val itemSubtotalCfa: Int = 0,
    val commissionRateBps: Int = 1500,
    val commissionCfa: Int = 0,
    val merchantNetCfa: Int = 0,
    val packageCount: Int = 0,
    val acceptedAt: String? = null,
    val preparingAt: String? = null,
    val packedReadyAt: String? = null,
    val handedToCourierAt: String? = null,
    val rejectionReason: String? = null,
    val sellerResponseDueAt: String? = null,
    val packingDueAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val version: Long = 0,
    val lines: List<VendorOrderLine> = emptyList(),
    val handoffMethod: VendorOrderHandoffMethod? = null,
    val handoffNote: String? = null,
) {
    val code: String
        get() = subOrderCode

    val sla: MerchantSlaInfo
        get() = MerchantSlaInfo(
            sellerResponseDueAt = sellerResponseDueAt,
            packingDueAt = packingDueAt,
        )
}

@Serializable
enum class VendorOrderHandoffMethod {
    SEQUO_RIDER,
    INDEPENDENT_RIDER,
    CUSTOMER_PICKUP,
    RELAY_POINT,
}

@Serializable
enum class MerchantSubOrderStatus {
    MERCHANT_PENDING,
    ACCEPTED,
    PREPARING,
    PACKED_READY,
    HANDED_TO_COURIER,
    REJECTED,
    CANCELLED,
}

@Serializable
data class MerchantSlaInfo(
    val sellerResponseDueAt: String? = null,
    val packingDueAt: String? = null,
    val evaluatedAt: String? = null,
    val sellerResponseOverdue: Boolean = false,
    val packingOverdue: Boolean = false,
) {
    val nextDueAt: String?
        get() = sellerResponseDueAt ?: packingDueAt

    val isOverdue: Boolean
        get() = sellerResponseOverdue || packingOverdue
}

@Serializable
data class VendorOrderLine(
    val id: String,
    val productId: String? = null,
    val productName: String,
    val quantity: Int,
    val unitPriceCfa: Int,
    val lineTotalCfa: Int = unitPriceCfa * quantity,
    val optionLabel: String? = null,
)

@Serializable
data class CatalogProduct(
    val id: String,
    val name: String,
    val priceCfa: Int? = null,
    val status: CatalogProductStatus = CatalogProductStatus.ACTIVE,
    val cameraVerified: Boolean = false,
    val merchantId: String? = null,
    val kind: CatalogProductKind = CatalogProductKind.SellerSpecific,
    val category: String? = null,
    val subcategory: String? = null,
    val detail: String? = null,
    val optionHint: String? = null,
    val originalPriceCfa: Int? = null,
    val hasDiscount: Boolean = false,
    val bargainingEnabled: Boolean = false,
    val bargainFloorCfa: Int? = null,
    val capturedAtLabel: String? = null,
)

@Serializable
enum class CatalogProductKind {
    SellerSpecific,
    GenericSealedItem,
}

@Serializable
enum class CatalogProductStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED,
}

@Serializable
data class CatalogCategory(
    val key: String,
    val title: String,
    val supportLabel: String,
    val accentHex: String,
)

@Serializable
data class StoreAvailabilityState(
    val merchantId: String? = null,
    val open: Boolean,
    val acceptingOrders: Boolean = open,
    val openStatus: String? = null,
    val weekdayHours: String,
    val weekendHours: String,
    val openingHours: List<OpeningHoursDay> = emptyList(),
    val temporaryClosure: TemporaryClosure? = null,
    val updatedAt: String? = null,
)

typealias StoreStatus = StoreAvailabilityState

@Serializable
data class OpeningHoursDay(
    val dayOfWeek: Int,
    val opensAt: String,
    val closesAt: String,
    val closed: Boolean = false,
)

@Serializable
data class TemporaryClosure(
    val id: String? = null,
    val startsAt: String,
    val endsAt: String,
    val reason: String? = null,
)

sealed interface VendorUiError {
    data object Offline : VendorUiError
    data object SessionExpired : VendorUiError
    data object PermissionDenied : VendorUiError
    data object NotFound : VendorUiError
    data object RateLimited : VendorUiError
    data object ServerUnavailable : VendorUiError
    data object Unexpected : VendorUiError
}

fun Int.formatCfa(): String = "${formatGroupedInt()} CFA"

fun Int?.formatCfaOrDash(): String = this?.formatCfa() ?: "-"

fun String?.formatIsoTimestampOrDash(): String =
    this?.formatIsoTimestamp() ?: "-"

fun String.formatIsoTimestamp(): String {
    val trimmed = trim()
    if (trimmed.length < 10) return trimmed

    val date = trimmed.substring(0, 10)
    val time = trimmed
        .substringAfter('T', "")
        .take(5)
        .takeIf { it.length == 5 }

    val parts = date.split("-")
    if (parts.size != 3) return trimmed

    val year = parts[0]
    val month = parts[1].toIntOrNull()?.let { MonthLabels.getOrNull(it - 1) } ?: parts[1]
    val day = parts[2].trimStart('0').ifBlank { "0" }
    return if (time == null) "$day $month $year" else "$day $month $year, $time"
}

private fun Int.formatGroupedInt(): String =
    toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

private val MonthLabels = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)

object DemoVendorData {
    fun session() = VendorSession(
        merchantId = "merchant-demo-sequo",
        displayName = "Demo merchant",
    )

    fun orders() = listOf(
        MerchantSubOrder(
            id = "mso-demo-001",
            subOrderCode = "SQ-M-1001",
            orderId = "order-demo-001",
            merchantId = "merchant-demo-sequo",
            status = MerchantSubOrderStatus.MERCHANT_PENDING,
            merchantNetCfa = 16_280,
            packageCount = 0,
            updatedAt = "2026-10-10T09:20:00Z",
            sellerResponseDueAt = "2026-10-10T18:00:00Z",
            packingDueAt = "2026-10-11T18:00:00Z",
        ),
        MerchantSubOrder(
            id = "mso-demo-002",
            subOrderCode = "SQ-M-1002",
            orderId = "order-demo-002",
            merchantId = "merchant-demo-sequo",
            status = MerchantSubOrderStatus.PREPARING,
            merchantNetCfa = 8_910,
            packageCount = 1,
            updatedAt = "2026-10-10T10:15:00Z",
            packingDueAt = "2026-10-11T10:15:00Z",
        ),
        MerchantSubOrder(
            id = "mso-demo-003",
            subOrderCode = "SQ-M-1003",
            orderId = "order-demo-003",
            merchantId = "merchant-demo-sequo",
            status = MerchantSubOrderStatus.PACKED_READY,
            merchantNetCfa = 21_400,
            packageCount = 2,
            updatedAt = "2026-10-10T11:00:00Z",
            packingDueAt = "2026-10-10T16:00:00Z",
        ),
        MerchantSubOrder(
            id = "mso-demo-004",
            subOrderCode = "SQ-M-1004",
            orderId = "order-demo-004",
            merchantId = "merchant-demo-sequo",
            status = MerchantSubOrderStatus.HANDED_TO_COURIER,
            merchantNetCfa = 5_750,
            packageCount = 1,
            updatedAt = "2026-10-10T12:30:00Z",
            handedToCourierAt = "2026-10-10T12:30:00Z",
            handoffMethod = VendorOrderHandoffMethod.SEQUO_RIDER,
            handoffNote = "SequoRider pickup",
        ),
        MerchantSubOrder(
            id = "mso-demo-005",
            subOrderCode = "SQ-M-1005",
            orderId = "order-demo-005",
            merchantId = "merchant-demo-sequo",
            status = MerchantSubOrderStatus.REJECTED,
            merchantNetCfa = 0,
            packageCount = 0,
            updatedAt = "2026-10-10T08:45:00Z",
            rejectionReason = "Out of stock",
        ),
    )

    fun products() = listOf(
        CatalogProduct("prod-demo-001", "Fresh tilapia box", 4_500, CatalogProductStatus.ACTIVE, cameraVerified = true),
        CatalogProduct("prod-demo-002", "Local cassava flour", 2_200, CatalogProductStatus.ACTIVE, cameraVerified = false),
        CatalogProduct("prod-demo-003", "Weekend bundle", 12_000, CatalogProductStatus.ARCHIVED, cameraVerified = true),
    )

    fun store() = StoreStatus(
        merchantId = "merchant-demo-sequo",
        open = true,
        weekdayHours = "08:00-18:00",
        weekendHours = "09:00-14:00",
    )
}
