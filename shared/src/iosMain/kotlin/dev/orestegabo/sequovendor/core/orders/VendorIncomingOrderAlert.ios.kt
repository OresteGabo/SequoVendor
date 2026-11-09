package dev.orestegabo.sequovendor.core.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium

@Composable
actual fun rememberVendorIncomingOrderAlert(): VendorIncomingOrderAlert {
    val scope = rememberCoroutineScope()
    val playback = remember(scope) { IosVendorIncomingOrderPlayback(scope) }
    return rememberFiniteVendorIncomingOrderAlert(playback)
}

/** Ambient audio respects the silent switch and lock; no VoIP/CallKit/background-audio entitlement. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosVendorIncomingOrderPlayback(private val scope: CoroutineScope) : VendorIncomingOrderPlayback {
    private var player: AVAudioPlayer? = null
    private var haptics: Job? = null
    private var audioSessionActive = false
    private val chime by lazy {
        vendorIncomingOrderChime().usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = pinned.get().size.toULong())
        }
    }

    override fun start() {
        stop()
        val audio = AVAudioSession.sharedInstance()
        if (audio.setCategory(AVAudioSessionCategoryAmbient, error = null) && audio.setActive(true, error = null)) {
            audioSessionActive = true
            player = AVAudioPlayer(data = chime, error = null)?.apply {
                numberOfLoops = -1
                prepareToPlay()
                play()
            }
        }
        haptics = scope.launch {
            val feedback = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyleMedium)
            while (isActive) {
                feedback.prepare()
                feedback.impactOccurred()
                delay(2_000)
            }
        }
    }

    override fun stop() {
        haptics?.cancel()
        haptics = null
        player?.stop()
        player = null
        if (audioSessionActive) {
            AVAudioSession.sharedInstance().setActive(false, error = null)
            audioSessionActive = false
        }
    }
}
