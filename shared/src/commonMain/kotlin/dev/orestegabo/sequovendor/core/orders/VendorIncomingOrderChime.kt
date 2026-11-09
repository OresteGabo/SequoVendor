package dev.orestegabo.sequovendor.core.orders

import kotlin.math.PI
import kotlin.math.sin

/** Original, small PCM chime; no platform ringtone files or audio licensing dependency. */
internal fun vendorIncomingOrderChime(): ByteArray {
    val rate = 22_050
    val frames = rate * 2
    val bytes = ByteArray(44 + frames * 2)
    fun text(offset: Int, value: String) { value.forEachIndexed { i, c -> bytes[offset + i] = c.code.toByte() } }
    fun number(offset: Int, value: Int, size: Int) {
        repeat(size) { i -> bytes[offset + i] = (value ushr (i * 8)).toByte() }
    }
    text(0, "RIFF"); number(4, bytes.size - 8, 4); text(8, "WAVE")
    text(12, "fmt "); number(16, 16, 4); number(20, 1, 2); number(22, 1, 2)
    number(24, rate, 4); number(28, rate * 2, 4); number(32, 2, 2); number(34, 16, 2)
    text(36, "data"); number(40, frames * 2, 4)
    repeat(frames) { frame ->
        val seconds = frame.toDouble() / rate
        val local = when {
            seconds < 0.32 -> seconds
            seconds in 0.48..<0.80 -> seconds - 0.48
            else -> -1.0
        }
        val value = if (local < 0) 0 else {
            val envelope = sin(PI * local / 0.32)
            ((sin(2 * PI * 660 * local) + sin(2 * PI * 880 * local)) *
                envelope * 0.16 * Short.MAX_VALUE).toInt()
        }
        number(44 + frame * 2, value, 2)
    }
    return bytes
}
