package com.textworld.app.presentation.gateways

/**
 * Extension gateways. All of them ship DISABLED and render no UI until a pack
 * and the player enable them. Screens talk only to these interfaces, so replacing
 * procedural art/audio/haptics with real implementations never changes a screen.
 */
interface VoiceGateway {
    val isAvailable: Boolean get() = false
    suspend fun transcribe(audio: ByteArray): String = ""
    suspend fun speak(text: String) = Unit
}

interface ImageGateway {
    val isAvailable: Boolean get() = false
    /** Resolves a slot ("scene_bg" / "portrait:<id>") to an optional URL. */
    fun resolve(slot: String, seed: Long, url: String?): String? = url
}

interface AmbientAudioGateway {
    val isAvailable: Boolean get() = false
    fun applyCues(cues: List<String>) = Unit
    fun stop() = Unit
}

interface HapticsGateway {
    val isAvailable: Boolean get() = false
    fun play(profile: String) = Unit
}

class DisabledVoiceGateway : VoiceGateway
class DisabledImageGateway : ImageGateway
class DisabledAmbientAudioGateway : AmbientAudioGateway
class DisabledHapticsGateway : HapticsGateway

/** Real haptics; enabled only when the pack exposes haptics and the player opts in. */
class AndroidHapticsGateway(
    private val vibrator: android.os.Vibrator?,
    override val isAvailable: Boolean,
) : HapticsGateway {
    override fun play(profile: String) {
        if (!isAvailable || vibrator == null) return
        val pattern = when (profile) {
            "soft_tick", "soft_impact" -> longArrayOf(0, 12)
            "heavy_impact" -> longArrayOf(0, 45)
            "long_pulse" -> longArrayOf(0, 120, 60, 120)
            else -> longArrayOf(0, 12)
        }
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    android.os.VibrationEffect.createWaveform(pattern, -1),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
}
