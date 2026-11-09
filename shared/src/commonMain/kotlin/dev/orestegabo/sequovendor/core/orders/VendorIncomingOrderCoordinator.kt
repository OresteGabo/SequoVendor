package dev.orestegabo.sequovendor.core.orders

import androidx.compose.runtime.Composable
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import kotlin.time.Instant

/** Device-local presentation receipts, never credentials or a server order transition. */
interface VendorIncomingOrderReceiptStore {
    fun consumed(scope: String): Set<String>
    /** Complete the durable write before returning; throw if it cannot be recorded. */
    fun consume(scope: String, orderId: String)
}

@Composable
expect fun rememberVendorIncomingOrderReceiptStore(): VendorIncomingOrderReceiptStore

data class IncomingOrderPresentation(
    val order: MerchantSubOrder,
    val remainingRingMillis: Long,
    val remainingResponseMillis: Long,
)

/**
 * Serial, deterministic presentation policy for one authenticated merchant workspace.
 * Call update only with a successful complete scoped queue, and invalidate on background,
 * failed/stale reads or logout. Call current on the UI clock; it records a receipt before
 * first presentation. No local wall clock creates a deadline or cancels/accepts an order.
 */
class VendorIncomingOrderCoordinator(
    private val merchantId: String,
    private val scope: String,
    private val store: VendorIncomingOrderReceiptStore,
    private val monotonicNowMillis: () -> Long,
) {
    init { require(merchantId.isNotBlank() && scope.isNotBlank()) }

    private data class Window(val alertUntil: Long, val responseDeadline: Long, val createdAt: Long)
    private data class TimedOrder(val order: MerchantSubOrder, val window: Window)
    private data class Anchor(val serverNow: Long, val elapsed: Long)

    private var elapsedFloor = 0L
    private var anchor: Anchor? = null
    private var fresh = false
    private var receiptsLoaded = false
    private val consumed = mutableSetOf<String>()
    private val suppressed = mutableSetOf<String>()
    private val windows = mutableMapOf<String, Window>()
    private var orders = emptyMap<String, TimedOrder>()
    private var activeId: String? = null
    var receiptStorageAvailable: Boolean = true
        private set

    /** Full measured request time is a conservative upper bound for server-to-device latency. */
    fun update(snapshot: List<MerchantSubOrder>, requestElapsedMillis: Long = 0L) {
        val elapsed = elapsed()
        val requestDuration = requestElapsedMillis.coerceAtLeast(0L)
        // Mixed/duplicate identities are not a trustworthy complete queue for this workspace.
        if (snapshot.any { it.merchantId != merchantId || it.id.isBlank() } ||
            snapshot.map { it.id }.distinct().size != snapshot.size) {
            invalidate()
            return
        }
        if (!loadReceipts()) return
        val parsed = snapshot.mapNotNull { order ->
            if (!eligible(order)) return@mapNotNull null
            val created = parse(order.createdAt) ?: return@mapNotNull null
            val server = parse(order.serverNow) ?: return@mapNotNull null
            val alert = parse(order.alertUntilAt) ?: return@mapNotNull null
            val deadline = parse(order.responseDeadlineAt) ?: return@mapNotNull null
            if (created > server || alert <= created || deadline < alert) return@mapNotNull null
            Triple(order, Window(alert, deadline, created), advance(server, requestDuration))
        }
        parsed.maxOfOrNull { it.third }?.let { server ->
            anchor = Anchor(maxOf(server, serverNow(elapsed) ?: server), elapsed)
        }
        val next = parsed.associate { (order, window, _) ->
            val prior = windows[order.id]
            // A repeated or out-of-order read cannot give an order another alert/response window.
            val bounded = if (prior == null) window else Window(
                minOf(prior.alertUntil, window.alertUntil),
                minOf(prior.responseDeadline, window.responseDeadline),
                minOf(prior.createdAt, window.createdAt),
            )
            windows[order.id] = bounded
            order.id to TimedOrder(order, bounded)
        }
        suppressed += orders.keys - next.keys
        orders = next
        activeId?.let { if (it !in orders) activeId = null }
        fresh = true
    }

    fun current(): IncomingOrderPresentation? {
        if (!fresh || !receiptStorageAvailable) return null
        val now = serverNow(elapsed()) ?: return null
        activeId?.let { id ->
            orders[id]?.takeIf { it.window.alertUntil > now && it.window.responseDeadline > now }
                ?.let { return presentation(it, now) }
            activeId = null
        }
        val next = orders.values.asSequence()
            .filter { it.order.id !in consumed && it.order.id !in suppressed &&
                it.window.alertUntil > now && it.window.responseDeadline > now }
            .sortedWith(compareBy<TimedOrder> { it.window.createdAt }.thenBy { it.order.id })
            .firstOrNull() ?: return null
        if (!recordReceipt(next.order.id)) return null
        val afterReceipt = serverNow(elapsed()) ?: return null
        if (next.window.alertUntil <= afterReceipt || next.window.responseDeadline <= afterReceipt) return null
        activeId = next.order.id
        return presentation(next, afterReceipt)
    }

    /** Close this incoming presentation only; the server order stays in its pending queue. */
    fun silence(orderId: String) {
        suppressed += orderId
        if (activeId == orderId) activeId = null
        recordReceipt(orderId)
    }

    /** Use only after the existing server action and its canonical readback are confirmed. */
    fun onHandled(orderId: String) {
        silence(orderId)
        orders = orders - orderId
    }

    fun invalidate() {
        fresh = false
        activeId = null
        orders = emptyMap()
    }

    /** Timed pending parts outside the current incoming presentation; legacy rows are separate. */
    fun waitingCount(): Int {
        if (!fresh) return 0
        val now = serverNow(elapsed()) ?: return 0
        val ringingId = activeId?.takeIf { id -> orders[id]?.window?.let { it.alertUntil > now && it.responseDeadline > now } == true }
        return orders.values.count { it.order.id != ringingId && it.window.responseDeadline > now }
    }

    private fun eligible(order: MerchantSubOrder): Boolean =
        order.status == MerchantSubOrderStatus.MERCHANT_PENDING && order.responseState == "PENDING" &&
            order.responseExpiredAt == null && order.responseExpiryDisposition == null && order.responseDisposition == null &&
            order.orderPaymentStatus in setOf("Validated", "VALIDATED") &&
            order.orderStatus in setOf("ACCEPTED_FOR_FULFILLMENT", "AcceptedForFulfillment")

    private fun loadReceipts(): Boolean {
        if (receiptsLoaded) return true
        return try {
            consumed += store.consumed(scope)
            receiptsLoaded = true
            receiptStorageAvailable = true
            true
        } catch (_: Exception) {
            receiptStorageAvailable = false
            invalidate()
            false
        }
    }

    private fun recordReceipt(orderId: String): Boolean {
        if (orderId in consumed) return true
        return try {
            store.consume(scope, orderId)
            consumed += orderId
            receiptStorageAvailable = true
            true
        } catch (_: Exception) {
            receiptStorageAvailable = false
            receiptsLoaded = false
            invalidate()
            false
        }
    }

    private fun presentation(order: TimedOrder, now: Long) = IncomingOrderPresentation(
        order.order, positiveDifference(order.window.alertUntil, now), positiveDifference(order.window.responseDeadline, now),
    )
    private fun elapsed(): Long = maxOf(elapsedFloor, monotonicNowMillis().coerceAtLeast(0L)).also { elapsedFloor = it }
    private fun serverNow(elapsed: Long): Long? = anchor?.let {
        val delta = (elapsed - it.elapsed).coerceAtLeast(0L)
        advance(it.serverNow, delta)
    }
    private fun advance(timestamp: Long, duration: Long): Long =
        if (timestamp > Long.MAX_VALUE - duration) Long.MAX_VALUE else timestamp + duration
    private fun parse(value: String?): Long? = value?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() }
    private fun positiveDifference(end: Long, now: Long): Long =
        if (end <= now) 0L else if (now < 0 && end > Long.MAX_VALUE + now) Long.MAX_VALUE else end - now
}
