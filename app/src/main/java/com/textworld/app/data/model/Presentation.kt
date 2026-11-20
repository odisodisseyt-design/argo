package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pure presentation data. The client never invents values here; it only maps
 * these strings to visual treatments and ignores anything it does not know.
 */
@Serializable
data class Presentation(
    val mood: String = "calm",
    val palette: Palette = Palette(),
    val weather: String = "clear",
    @SerialName("time_of_day") val timeOfDay: String = "day",
    val particles: Particles = Particles(),
    val parallax: Parallax = Parallax(),
    @SerialName("audio_cues") val audioCues: List<String> = emptyList(),
    val emotion: Emotion = Emotion(),
    val illustration: Illustration? = null,
    @SerialName("portrait_hints") val portraitHints: Map<String, PortraitHint> = emptyMap(),
    val haptic: String = "none",
    val typography: Typography = Typography(),
    @SerialName("system_theme") val systemTheme: String = "hologram",
    @SerialName("schema_version") val schemaVersion: Int = 1,
)

@Serializable
data class Palette(
    val bg: String = "#0E0D0B",
    @SerialName("bg_alt") val bgAlt: String = "#17130F",
    val accent: String = "#C9A227",
    val text: String = "#E8DECC",
    val muted: String = "#8C8272",
    val vignette: String = "#000000",
)

@Serializable
data class Particles(
    val type: String = "none",
    val density: Float = 0f,
    val speed: Float = 0f,
)

@Serializable
data class Parallax(
    val far: String = "none",
    val mid: String = "none",
    val near: String = "none",
)

@Serializable
data class Emotion(
    val player: String = "calm",
    @SerialName("npcs") val npcs: Map<String, String> = emptyMap(),
)

@Serializable
data class Illustration(
    val seed: Long = 0L,
    val subject: String = "",
    @SerialName("style_hint") val styleHint: String = "",
    val slot: String = "scene_bg",
    val url: String? = null,
)

@Serializable
data class PortraitHint(
    val expression: String = "neutral",
    val seed: Long = 0L,
    val url: String? = null,
)

@Serializable
data class Typography(
    val dropcap: Boolean = true,
    @SerialName("pace_ms") val paceMs: Int = 18,
)

object Mood {
    const val WONDER = "wonder"
    const val DREAD = "dread"
    const val WARM = "warm"
    const val MELANCHOLY = "melancholy"
    const val TENSION = "tension"
    const val CALM = "calm"
}

object SystemTheme {
    const val HOLOGRAM = "hologram"
    const val RUNIC = "runic"
    const val SYMBIOTE = "symbiote"
}
