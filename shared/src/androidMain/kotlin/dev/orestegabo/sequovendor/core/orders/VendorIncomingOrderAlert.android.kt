package dev.orestegabo.sequovendor.core.orders

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberVendorIncomingOrderAlert(): VendorIncomingOrderAlert {
    val context = LocalContext.current.applicationContext
    val playback = remember(context) { AndroidVendorIncomingOrderPlayback(context) }
    return rememberFiniteVendorIncomingOrderAlert(playback)
}

/** Notification audio, never telephony, volume changes, DND bypass, or a background service. */
@Suppress("DEPRECATION")
private class AndroidVendorIncomingOrderPlayback(private val context: Context) : VendorIncomingOrderPlayback {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private var player: MediaPlayer? = null

    override fun start() {
        stop()
        if (notifications.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL ||
            audio.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            val tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (tone != null) {
                val candidate = MediaPlayer()
                player = candidate
                try {
                    candidate.setAudioAttributes(attributes)
                    candidate.setDataSource(context, tone)
                    candidate.isLooping = true
                    candidate.prepare()
                    candidate.start()
                } catch (_: Exception) { releasePlayer() }
            }
        }
        if (vibrator.hasVibrator() && audio.shouldVibrate(AudioManager.VIBRATE_TYPE_NOTIFICATION)) {
            val pattern = longArrayOf(0, 300, 250, 300, 1_150)
            try {
                if (Build.VERSION.SDK_INT >= 26)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0), attributes)
                else vibrator.vibrate(pattern, 0, attributes)
            } catch (_: SecurityException) { /* Optional hardware/permission; the order UI remains usable. */ }
        }
    }

    override fun stop() {
        releasePlayer()
        try { vibrator.cancel() } catch (_: SecurityException) { }
    }

    private fun releasePlayer() {
        player?.let { current ->
            try { if (current.isPlaying) current.stop() } catch (_: IllegalStateException) { }
            current.release()
        }
        player = null
    }
}
