package dev.orestegabo.sequovendor

import dev.orestegabo.sequovendor.core.orders.FiniteVendorIncomingOrderAlert
import dev.orestegabo.sequovendor.core.orders.VendorAlertCancellation
import dev.orestegabo.sequovendor.core.orders.VendorIncomingOrderPlayback
import dev.orestegabo.sequovendor.core.orders.decodeVendorIncomingReceipts
import dev.orestegabo.sequovendor.core.orders.encodeVendorIncomingReceipt
import dev.orestegabo.sequovendor.core.orders.vendorIncomingReceiptKey
import kotlin.test.*

class VendorIncomingOrderAlertTest {
    @Test fun serverRemainingWindowIsBoundedAndSameOrderCannotExtendIt() {
        val fixture = AlertFixture()
        fixture.alert.start("paid-order", 90_000)
        assertEquals(1, fixture.starts)
        fixture.advanceTo(30_000)
        fixture.alert.start("paid-order", 45_000)
        assertEquals(1, fixture.starts)
        fixture.advanceTo(44_999)
        assertTrue(fixture.playing)
        fixture.advanceTo(45_000)
        assertFalse(fixture.playing)
    }

    @Test fun updatedServerDeadlineCanShortenAnActiveAlert() {
        val fixture = AlertFixture()
        fixture.alert.start("paid-order", 45_000)
        fixture.advanceTo(5_000)
        fixture.alert.start("paid-order", 1_000)
        fixture.advanceTo(6_000)
        assertFalse(fixture.playing)
    }

    @Test fun cancelledOldTimerCannotStopAnotherOrderAndStopIsIdempotent() {
        val fixture = AlertFixture()
        fixture.alert.start("first", 10_000)
        val stale = fixture.timers.last().callback
        fixture.advanceTo(1_000)
        fixture.alert.start("second", 20_000)
        stale()
        assertTrue(fixture.playing)
        fixture.alert.stop()
        fixture.alert.stop()
        fixture.advanceTo(30_000)
        assertFalse(fixture.playing)
        assertEquals(2, fixture.starts)
    }

    @Test fun expiredBlankOrBackgroundRequestsNeverPlay() {
        val fixture = AlertFixture()
        fixture.alert.start("paid-order", 0)
        fixture.alert.start(" ", 45_000)
        fixture.foreground = false
        fixture.alert.start("paid-order", 45_000)
        assertEquals(0, fixture.starts)
        assertFalse(fixture.playing)
    }

    @Test fun disposedAlertCannotBeRestartedByAnOldUiCallback() {
        val fixture = AlertFixture()
        fixture.alert.start("paid-order", 45_000)
        fixture.alert.dispose()
        fixture.alert.start("another-order", 45_000)
        fixture.advanceTo(50_000)
        assertFalse(fixture.playing)
        assertEquals(1, fixture.starts)
    }

    @Test fun receiptsAreScopedBoundedAndCorruptionFailsClosed() {
        assertNotEquals(vendorIncomingReceiptKey("owner|merchant-a|api"), vendorIncomingReceiptKey("owner|merchant-b|api"))
        var encoded: String? = null
        repeat(260) { encoded = encodeVendorIncomingReceipt(decodeVendorIncomingReceipts(encoded), "order-$it") }
        val ids = decodeVendorIncomingReceipts(encoded)
        assertEquals(256, ids.size)
        assertFalse("order-0" in ids)
        assertEquals("order-259", ids.last())
        assertEquals(256, decodeVendorIncomingReceipts(encodeVendorIncomingReceipt(ids, "order-259")).size)
        assertFailsWith<IllegalStateException> { decodeVendorIncomingReceipts("not-json") }
        assertFailsWith<IllegalStateException> { decodeVendorIncomingReceipts("[\"same\",\"same\"]") }
    }

    private class AlertFixture {
        data class Timer(val at: Long, val callback: () -> Unit, var cancelled: Boolean = false)
        var now = 0L
        var starts = 0
        var playing = false
        var foreground = true
        val timers = mutableListOf<Timer>()
        val alert = FiniteVendorIncomingOrderAlert(
            nowMillis = { now },
            schedule = { delay, callback ->
                val timer = Timer(now + delay, callback)
                timers += timer
                VendorAlertCancellation { timer.cancelled = true }
            },
            playback = object : VendorIncomingOrderPlayback {
                override fun start() { starts++; playing = true }
                override fun stop() { playing = false }
            },
            isForeground = { foreground },
        )
        fun advanceTo(time: Long) {
            now = time
            timers.toList().filter { !it.cancelled && it.at <= now }.forEach {
                it.cancelled = true
                it.callback()
            }
        }
    }
}
