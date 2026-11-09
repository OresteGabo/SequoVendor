package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.orders.VendorIncomingOrderCoordinator
import dev.orestegabo.sequovendor.core.orders.VendorIncomingOrderReceiptStore
import dev.orestegabo.sequovendor.model.MerchantSubOrder
import dev.orestegabo.sequovendor.model.MerchantSubOrderStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class VendorIncomingOrderCoordinatorTest {
    private class Clock(var elapsed: Long = 0L)
    private class Receipts : VendorIncomingOrderReceiptStore {
        val data = mutableMapOf<String, MutableSet<String>>()
        val writes = mutableListOf<Pair<String, String>>()
        var failLoad = false
        var failWrite = false
        var afterWrite: (() -> Unit)? = null
        override fun consumed(scope: String): Set<String> {
            check(!failLoad) { "Storage unavailable" }
            return data[scope].orEmpty().toSet()
        }
        override fun consume(scope: String, orderId: String) {
            check(!failWrite) { "Storage unavailable" }
            data.getOrPut(scope) { linkedSetOf() }.add(orderId)
            writes += scope to orderId
            afterWrite?.invoke()
        }
    }
    private val epoch = Instant.parse("2026-11-09T00:00:00Z").toEpochMilliseconds()
    private fun at(offset: Long) = Instant.fromEpochMilliseconds(epoch + offset).toString()
    private fun order(id: String = "part-1", created: Long = 0L, serverNow: Long = 0L,
        alertUntil: Long = created + 60_000L, responseDeadline: Long = created + 900_000L) = MerchantSubOrder(
        id = id, subOrderCode = "legacy-$id", merchantId = "merchant-a",
        status = MerchantSubOrderStatus.MERCHANT_PENDING,
        orderStatus = "ACCEPTED_FOR_FULFILLMENT", orderPaymentStatus = "Validated",
        createdAt = at(created), serverNow = at(serverNow), alertUntilAt = at(alertUntil),
        responseDeadlineAt = at(responseDeadline), responseState = "PENDING",
    )
    private fun coordinator(clock: Clock, receipts: Receipts = Receipts(), scope: String = "user-a:merchant-a:SEQUO_MERCHANT") =
        VendorIncomingOrderCoordinator("merchant-a", scope, receipts) { clock.elapsed }

    @Test fun ringEndsExactlyAtServerWindowWithoutCancellingThePendingOrder() {
        val clock = Clock(); val store = Receipts(); val coordinator = coordinator(clock, store); val row = order()
        coordinator.update(listOf(row))
        assertEquals(60_000L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 59_999
        assertEquals(1L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 60_000
        assertNull(coordinator.current()); assertEquals(1, coordinator.waitingCount())
        clock.elapsed = 60_001
        assertNull(coordinator.current()); assertEquals(1, store.writes.size)
        assertEquals(MerchantSubOrderStatus.MERCHANT_PENDING, row.status)
    }

    @Test fun oldTimedSnapshotRetainsFortyFiveSecondRingAndTenMinuteResponse() {
        val clock = Clock(); val coordinator = coordinator(clock)
        val row = order(alertUntil = 45_000, responseDeadline = 600_000)
        coordinator.update(listOf(row))
        assertEquals(45_000L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 44_999
        assertEquals(1L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 45_000
        assertNull(coordinator.current()); assertEquals(1, coordinator.waitingCount())
        clock.elapsed = 599_999
        assertEquals(1, coordinator.waitingCount())
        clock.elapsed = 600_000
        assertEquals(0, coordinator.waitingCount())
        assertEquals(MerchantSubOrderStatus.MERCHANT_PENDING, row.status)
    }

    @Test fun slowResponseDeductsItsEntireMeasuredRequestTimeFromTheServerWindow() {
        val clock = Clock(); val coordinator = coordinator(clock)
        coordinator.update(listOf(order()), requestElapsedMillis = 5_000L)
        assertEquals(55_000L, coordinator.current()?.remainingRingMillis)
        assertEquals(895_000L, coordinator.current()?.remainingResponseMillis)
        clock.elapsed = 54_999L
        assertEquals(1L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 55_000L
        assertNull(coordinator.current()); assertEquals(1, coordinator.waitingCount())
        val alreadyLate = coordinator(Clock())
        alreadyLate.update(listOf(order()), requestElapsedMillis = 60_000L)
        assertNull(alreadyLate.current()); assertEquals(1, alreadyLate.waitingCount())
    }

    @Test fun negativeOrOverflowingRequestDurationsCannotExtendTheServerWindow() {
        val negative = coordinator(Clock())
        negative.update(listOf(order()), requestElapsedMillis = -1L)
        assertEquals(60_000L, negative.current()?.remainingRingMillis)
        val excessive = coordinator(Clock())
        excessive.update(listOf(order()), requestElapsedMillis = Long.MAX_VALUE)
        assertNull(excessive.current()); assertEquals(0, excessive.waitingCount())
    }

    @Test fun responseBoundaryOnlyEndsLocalWaitingAndNeverInventsServerCancellation() {
        val clock = Clock(); val coordinator = coordinator(clock); val row = order()
        coordinator.update(listOf(row)); coordinator.current()
        clock.elapsed = 899_999
        assertEquals(1, coordinator.waitingCount())
        clock.elapsed = 900_000
        assertEquals(0, coordinator.waitingCount()); assertNull(coordinator.current())
        clock.elapsed = 900_001
        assertEquals(0, coordinator.waitingCount())
        assertEquals(MerchantSubOrderStatus.MERCHANT_PENDING, row.status)
    }

    @Test fun repeatedAndReorderedPollsDoNotResetTheRingOrWriteAnotherReceipt() {
        val clock = Clock(); val store = Receipts(); val coordinator = coordinator(clock, store)
        coordinator.update(listOf(order())); coordinator.current()
        clock.elapsed = 10_000
        coordinator.update(listOf(order().copy(itemSubtotalCfa = 3000)))
        val current = coordinator.current()
        assertEquals(50_000L, current?.remainingRingMillis); assertEquals(3000, current?.order?.itemSubtotalCfa)
        assertEquals(1, store.writes.size)
    }

    @Test fun seenOrderDoesNotRingAgainAfterRelaunchEvenInsideTheWindow() {
        val clock = Clock(); val store = Receipts(); val first = coordinator(clock, store)
        first.update(listOf(order())); assertEquals("part-1", first.current()?.order?.id)
        val restored = coordinator(Clock(), store)
        restored.update(listOf(order(serverNow = 5_000)))
        assertNull(restored.current()); assertEquals(1, restored.waitingCount()); assertEquals(1, store.writes.size)
    }

    @Test fun presentationReceiptsAreIsolatedByTheExactWorkspaceScope() {
        val store = Receipts(); val first = coordinator(Clock(), store)
        first.update(listOf(order())); first.current()
        val otherUser = coordinator(Clock(), store, "user-b:merchant-a:SEQUO_MERCHANT")
        otherUser.update(listOf(order())); assertEquals("part-1", otherUser.current()?.order?.id)
        assertEquals(2, store.writes.size)
    }

    @Test fun queuedArrivalsKeepTheirOwnDeadlineRatherThanGettingAnotherSixtySeconds() {
        val clock = Clock(); val coordinator = coordinator(clock)
        coordinator.update(listOf(order("later", created = 1000, serverNow = 1000), order("first", serverNow = 1000)))
        assertEquals("first", coordinator.current()?.order?.id)
        clock.elapsed = 59_000
        val later = coordinator.current()
        assertEquals("later", later?.order?.id); assertEquals(1000L, later?.remainingRingMillis)
        clock.elapsed = 60_000
        assertNull(coordinator.current())
    }

    @Test fun acceptanceElsewhereAndAbsentRowsStopAlertsAndCannotBeReintroducedByStaleReads() {
        val clock = Clock(); val coordinator = coordinator(clock)
        coordinator.update(listOf(order("active"), order("queued"))); coordinator.current()
        coordinator.update(listOf(order("active").copy(status = MerchantSubOrderStatus.ACCEPTED, responseState = "ACCEPTED")))
        assertNull(coordinator.current())
        coordinator.update(listOf(order("active"), order("queued")))
        assertNull(coordinator.current())
    }

    @Test fun backgroundOrFailedReadRequiresFreshScopeDataAndOnlyUnseenOrdersMayResume() {
        val clock = Clock(); val coordinator = coordinator(clock)
        coordinator.update(listOf(order())); coordinator.current()
        coordinator.invalidate(); assertNull(coordinator.current()); assertEquals(0, coordinator.waitingCount())
        clock.elapsed = 20_000
        coordinator.update(listOf(order(serverNow = 20_000), order("unseen", created = 10_000, serverNow = 20_000)))
        assertEquals("unseen", coordinator.current()?.order?.id)
        assertEquals(50_000L, coordinator.current()?.remainingRingMillis)
    }

    @Test fun legacyUnpaidCancelledAndMalformedRowsNeverCreateAnIncomingAlert() {
        val coordinator = coordinator(Clock())
        val base = order()
        for (row in listOf(base.copy(responseState = "LEGACY_UNTIMED"), base.copy(orderPaymentStatus = "Pending"),
            base.copy(orderStatus = "CANCELLED"), base.copy(status = MerchantSubOrderStatus.CANCELLED),
            base.copy(responseState = "EXPIRED"), base.copy(alertUntilAt = null), base.copy(serverNow = "invalid"),
            base.copy(createdAt = null), base.copy(responseExpiredAt = at(0)),
            base.copy(responseExpiryDisposition = "OPERATIONS_REVIEW_REQUIRED"),
            base.copy(responseDisposition = "OPERATIONS_REVIEW_REQUIRED"))) {
            coordinator.update(listOf(row)); assertNull(coordinator.current())
        }
    }

    @Test fun foreignOrDuplicateRowsInvalidateTheWholeUntrustedQueue() {
        val coordinator = coordinator(Clock()); val row = order()
        coordinator.update(listOf(row, row.copy(id = "foreign", merchantId = "merchant-b")))
        assertNull(coordinator.current()); assertEquals(0, coordinator.waitingCount())
        coordinator.update(listOf(row, row)); assertNull(coordinator.current())
    }

    @Test fun monotonicClockRollbackAndOlderServerSnapshotsCannotExtendAWindow() {
        val clock = Clock(); val coordinator = coordinator(clock)
        coordinator.update(listOf(order())); coordinator.current()
        clock.elapsed = 10_000; assertEquals(50_000L, coordinator.current()?.remainingRingMillis)
        clock.elapsed = 5_000
        coordinator.update(listOf(order(serverNow = 0, alertUntil = 120_000, responseDeadline = 1_800_000)))
        assertEquals(50_000L, coordinator.current()?.remainingRingMillis)
        assertEquals(890_000L, coordinator.current()?.remainingResponseMillis)
    }

    @Test fun badStorageFailsClosedAndCanRecoverOnTheNextFreshRead() {
        val clock = Clock(); val store = Receipts(); val coordinator = coordinator(clock, store)
        store.failLoad = true; coordinator.update(listOf(order()))
        assertFalse(coordinator.receiptStorageAvailable); assertNull(coordinator.current())
        store.failLoad = false; store.failWrite = true; coordinator.update(listOf(order()))
        assertNull(coordinator.current()); assertFalse(coordinator.receiptStorageAvailable)
        store.failWrite = false; coordinator.update(listOf(order()))
        assertEquals("part-1", coordinator.current()?.order?.id); assertTrue(coordinator.receiptStorageAvailable)
    }

    @Test fun slowDurableReceiptCannotPresentAnOrderAfterTheRingWindow() {
        val clock = Clock(); val store = Receipts(); val coordinator = coordinator(clock, store)
        coordinator.update(listOf(order())); store.afterWrite = { clock.elapsed = 60_000 }
        assertNull(coordinator.current()); assertEquals(1, store.writes.size)
        assertEquals(1, coordinator.waitingCount())
    }

    @Test fun silencingAndConfirmedHandlingNeverChangeTheServerOwnedOrder() {
        val clock = Clock(); val coordinator = coordinator(clock); val row = order()
        coordinator.update(listOf(row)); coordinator.current(); coordinator.silence(row.id)
        assertNull(coordinator.current()); assertEquals(1, coordinator.waitingCount())
        coordinator.onHandled(row.id); assertEquals(0, coordinator.waitingCount())
        coordinator.update(listOf(row)); assertNull(coordinator.current())
        assertEquals(MerchantSubOrderStatus.MERCHANT_PENDING, row.status)
    }
}
