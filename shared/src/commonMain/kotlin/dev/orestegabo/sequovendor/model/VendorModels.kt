package dev.orestegabo.sequovendor.model

import kotlinx.serialization.Serializable

data class VendorSession(
    val merchantId: String,
    val displayName: String,
    val accessToken: String? = null,
    val authProvider: String? = null,
    val localMode: Boolean = true,
    val userId: String = merchantId,
    val email: String? = null,
    val status: String? = null,
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
    val stockSummary: String? = null,
    val updatedAt: String? = null,
    val stockCount: Int? = null,
    val preparationTimeMinutes: Int? = null,
    val returnEligible: Boolean = false,
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
    DRAFT,
    PAUSED,
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
    val temporaryExceptions: List<OpeningHoursException> = emptyList(),
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
data class OpeningHoursException(
    val id: String? = null,
    val date: String,
    val closed: Boolean,
    val opensAt: String? = null,
    val closesAt: String? = null,
    val reason: String? = null,
)

@Serializable
data class TemporaryClosure(
    val id: String? = null,
    val startsAt: String,
    val endsAt: String,
    val reason: String? = null,
)

@Serializable
data class VendorNotification(
    val id: String,
    val eventId: String,
    val appFamily: String = "SEQUO_MERCHANT",
    val eventType: VendorNotificationEventType,
    val severity: VendorNotificationSeverity = VendorNotificationSeverity.INFO,
    val title: String,
    val body: String,
    val actionUrl: String? = null,
    val readAt: String? = null,
    val archivedAt: String? = null,
    val createdAt: String,
) {
    val unread: Boolean
        get() = readAt == null

    val archived: Boolean
        get() = archivedAt != null
}

@Serializable
enum class VendorNotificationSeverity {
    INFO,
    ACTION_REQUIRED,
    URGENT,
    SECURITY,
    FINANCIAL,
}

@Serializable
enum class VendorNotificationEventType {
    ORDER_CREATED,
    PAYMENT_CONFIRMED,
    BARGAINING_PROPOSAL_CREATED,
    BARGAINING_COUNTERED,
    BARGAINING_ACCEPTED,
    MERCHANT_ACCEPTED_ORDER,
    MERCHANT_REJECTED_ORDER,
    MERCHANT_SLA_WARNING,
    ORDER_PREPARING,
    ORDER_READY_FOR_PICKUP,
    RIDER_MISSION_OFFERED,
    RIDER_ACCEPTED_MISSION,
    RIDER_PICKED_UP,
    RIDER_ARRIVED,
    DIRECT_DELIVERED,
    RELAY_PARCEL_DEPOSITED,
    RELAY_PICKUP_CODE_CREATED,
    RELAY_PARCEL_DELAYED,
    RETURN_REQUESTED,
    RETURN_PIN_CREATED,
    RETURN_RECEIVED_BY_SEQUO,
    REFUND_TRIGGERED,
    MERCHANT_PAYOUT_ELIGIBLE,
    PAYOUT_SENT,
    DELIVERY_PROBLEM_REPORTED,
    MISSING_DEPOT_BLOCKED_TOUR,
}

@Serializable
data class VendorNotificationPreference(
    val id: String? = null,
    val appFamily: String = "SEQUO_MERCHANT",
    val eventType: VendorNotificationPreferenceEventType = VendorNotificationPreferenceEventType.ALL,
    val pushEnabled: Boolean = true,
    val inAppEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
)

@Serializable
enum class VendorNotificationPreferenceEventType {
    ALL,
    ORDER_CREATED,
    MERCHANT_SLA_WARNING,
    ORDER_READY_FOR_PICKUP,
    DELIVERY_PROBLEM_REPORTED,
}

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
        CatalogProduct(
            id = "prod-demo-001",
            name = "Fresh tilapia box",
            priceCfa = 4_500,
            status = CatalogProductStatus.ACTIVE,
            cameraVerified = true,
            merchantId = "merchant-demo-sequo",
            category = "food",
            subcategory = "Fresh fish",
            detail = "Prepared morning batch",
            stockSummary = "Available",
            updatedAt = "2026-10-10T09:15:00Z",
            stockCount = 18,
            preparationTimeMinutes = 25,
            returnEligible = false,
        ),
        CatalogProduct(
            id = "prod-demo-002",
            name = "Local cassava flour",
            priceCfa = 2_200,
            status = CatalogProductStatus.ACTIVE,
            cameraVerified = false,
            merchantId = "merchant-demo-sequo",
            category = "grocery",
            subcategory = "Pantry",
            detail = "Needs live seller photo",
            stockSummary = "Available",
            updatedAt = "2026-10-09T17:40:00Z",
            stockCount = 42,
            returnEligible = true,
        ),
        CatalogProduct(
            id = "prod-demo-003",
            name = "Weekend bundle",
            priceCfa = 12_000,
            status = CatalogProductStatus.PAUSED,
            cameraVerified = true,
            merchantId = "merchant-demo-sequo",
            category = "food",
            subcategory = "Bundles",
            detail = "Paused locally until stock returns",
            stockSummary = "Out of stock",
            updatedAt = "2026-10-08T11:00:00Z",
            stockCount = 0,
            preparationTimeMinutes = 30,
            returnEligible = false,
        ),
        CatalogProduct(
            id = "prod-demo-004",
            name = "New supplier basket",
            priceCfa = null,
            status = CatalogProductStatus.DRAFT,
            cameraVerified = false,
            merchantId = "merchant-demo-sequo",
            category = "grocery",
            subcategory = "Draft",
            detail = "Local draft only",
            stockSummary = "Draft",
            updatedAt = "2026-10-10T12:00:00Z",
            stockCount = null,
            returnEligible = true,
        ),
    )

    fun notifications() = listOf(
        VendorNotification(
            id = "notif-demo-new-order",
            eventId = "event-demo-new-order",
            eventType = VendorNotificationEventType.ORDER_CREATED,
            severity = VendorNotificationSeverity.ACTION_REQUIRED,
            title = "New merchant order",
            body = "SQ-M-1001 is waiting for your response.",
            createdAt = "2026-10-10T09:20:00Z",
        ),
        VendorNotification(
            id = "notif-demo-sla",
            eventId = "event-demo-sla",
            eventType = VendorNotificationEventType.MERCHANT_SLA_WARNING,
            severity = VendorNotificationSeverity.URGENT,
            title = "SLA warning",
            body = "A preparation deadline needs attention.",
            createdAt = "2026-10-10T10:15:00Z",
        ),
        VendorNotification(
            id = "notif-demo-delivery",
            eventId = "event-demo-delivery",
            eventType = VendorNotificationEventType.DELIVERY_PROBLEM_REPORTED,
            severity = VendorNotificationSeverity.ACTION_REQUIRED,
            title = "Delivery problem reported",
            body = "Support may contact the merchant for package details.",
            createdAt = "2026-10-10T12:30:00Z",
            readAt = "2026-10-10T12:35:00Z",
        ),
    )

    fun store() = StoreStatus(
        merchantId = "merchant-demo-sequo",
        open = true,
        weekdayHours = "08:00-18:00",
        weekendHours = "09:00-14:00",
        openingHours = listOf(
            OpeningHoursDay(1, "08:00", "18:00"),
            OpeningHoursDay(2, "08:00", "18:00"),
            OpeningHoursDay(3, "08:00", "18:00"),
            OpeningHoursDay(4, "08:00", "18:00"),
            OpeningHoursDay(5, "08:00", "18:00"),
            OpeningHoursDay(6, "09:00", "14:00"),
            OpeningHoursDay(7, "", "", closed = true),
        ),
    )
}
