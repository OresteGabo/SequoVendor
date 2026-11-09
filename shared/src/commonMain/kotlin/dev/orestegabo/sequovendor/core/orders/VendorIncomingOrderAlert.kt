package dev.orestegabo.sequovendor.core.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.time.TimeSource

/** Foreground attention only. Accept/reject and the response deadline remain server-owned. */
interface VendorIncomingOrderAlert {
    fun start(orderId: String, remainingMillis: Long = 60_000)
    fun stop()
}

@Composable
expect fun rememberVendorIncomingOrderAlert(): VendorIncomingOrderAlert

internal interface VendorIncomingOrderPlayback {
    fun start()
    fun stop()
}

internal fun interface VendorAlertCancellation { fun cancel() }

/** A repeated poll can shorten the server window, but cannot restart or extend an active ring. */
internal class FiniteVendorIncomingOrderAlert(
    private val nowMillis: () -> Long,
    private val schedule: (Long, () -> Unit) -> VendorAlertCancellation,
    private val playback: VendorIncomingOrderPlayback,
    private val isForeground: () -> Boolean = { true },
) : VendorIncomingOrderAlert {
    private var disposed = false
    private var orderId: String? = null
    private var deadlineMillis = 0L
    private var generation = 0L
    private var cancellation: VendorAlertCancellation? = null

    override fun start(orderId: String, remainingMillis: Long) {
        val duration = remainingMillis.coerceIn(0L, 60_000L)
        if (disposed || orderId.isBlank() || duration == 0L || !isForeground()) {
            stop()
            return
        }
        val now = nowMillis()
        if (this.orderId == orderId) {
            if (deadlineMillis <= now) { stop(); return }
            deadlineMillis = minOf(deadlineMillis, now + duration)
            scheduleExpiry()
            return
        }
        stop()
        this.orderId = orderId
        deadlineMillis = now + duration
        try {
            playback.start()
            scheduleExpiry()
        } catch (_: Exception) {
            // Audio availability must never block the real order queue or its actions.
            stop()
        }
    }

    private fun scheduleExpiry() {
        cancellation?.cancel()
        val token = ++generation
        val remaining = (deadlineMillis - nowMillis()).coerceAtLeast(0L)
        cancellation = schedule(remaining) {
            if (token == generation && orderId != null) {
                if (nowMillis() >= deadlineMillis || !isForeground()) stop()
                else scheduleExpiry()
            }
        }
    }

    fun dispose() {
        disposed = true
        stop()
    }

    override fun stop() {
        ++generation
        cancellation?.cancel()
        cancellation = null
        orderId = null
        deadlineMillis = 0L
        playback.stop()
    }
}

@Composable
internal fun rememberFiniteVendorIncomingOrderAlert(
    playback: VendorIncomingOrderPlayback,
): VendorIncomingOrderAlert {
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val origin = remember { TimeSource.Monotonic.markNow() }
    val alert = remember(playback, lifecycle, scope) {
        FiniteVendorIncomingOrderAlert(
            nowMillis = { origin.elapsedNow().inWholeMilliseconds },
            schedule = { duration, elapsed ->
                val job = scope.launch { delay(duration); elapsed() }
                VendorAlertCancellation { job.cancel() }
            },
            playback = playback,
            isForeground = { scope.isActive && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) },
        )
    }
    DisposableEffect(alert, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP ||
                event == Lifecycle.Event.ON_DESTROY) alert.stop()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); alert.dispose() }
    }
    return alert
}
