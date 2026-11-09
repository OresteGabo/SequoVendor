package dev.orestegabo.sequovendor.core.orders

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun vendorIncomingReceiptKey(scope: String): String {
    require(scope.isNotBlank()) { "An order alert workspace is required." }
    return "vendor_incoming_orders_v1:$scope"
}

internal fun decodeVendorIncomingReceipts(encoded: String?): List<String> {
    if (encoded == null) return emptyList()
    return try {
        Json.decodeFromString<List<String>>(encoded).also { ids ->
            check(ids.size <= 256 && ids.none(String::isBlank) && ids.distinct().size == ids.size)
        }
    } catch (_: Exception) {
        throw IllegalStateException("Saved order alert receipts are unavailable.")
    }
}

internal fun encodeVendorIncomingReceipt(ids: List<String>, orderId: String): String {
    require(orderId.isNotBlank()) { "An order is required." }
    return Json.encodeToString((ids.filterNot { it == orderId } + orderId).takeLast(256))
}
