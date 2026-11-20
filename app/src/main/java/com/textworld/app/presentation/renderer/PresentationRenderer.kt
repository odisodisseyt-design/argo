package com.textworld.app.presentation.renderer

import androidx.compose.ui.graphics.Color
import com.textworld.app.core.Constants
import com.textworld.app.data.model.Mood
import com.textworld.app.data.model.Palette
import com.textworld.app.data.model.Particles
import com.textworld.app.data.model.Presentation
import com.textworld.app.data.model.SystemTheme
import com.textworld.app.data.model.Typography
import com.textworld.app.data.model.UiManifest
import com.textworld.app.data.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Resolved visual state. Screens read this and never re-interpret raw fields. */
data class RenderState(
    val bg: Color,
    val bgAlt: Color,
    val accent: Color,
    val text: Color,
    val muted: Color,
    val vignette: Color,
    val vignetteAlpha: Float,
    val desaturation: Float,
    val pulse: Boolean,
    val particles: Particles,
    val parallaxFar: String,
    val parallaxMid: String,
    val parallaxNear: String,
    val audioCues: List<String>,
    val systemTheme: String,
    val typography: Typography,
    val haptic: String,
    val emotionPlayer: String,
    val npcEmotions: Map<String, String>,
    val illustrationSeed: Long,
    val illustrationSubject: String,
    val illustrationStyle: String,
    val illustrationUrl: String?,
    val portraitHints: Map<String, Pair<Long, String?>>,
    val sceneKey: String,
    val transitionMs: Int,
    val ambientMotion: Boolean,
    val dropcap: Boolean,
    val statusGlow: Boolean,
    val imagesEnabled: Boolean,
    val themeSignature: Int,
)

/**
 * ONE class parses presentation into theme, particles, parallax and haptics for
 * every screen. Adding a new mood, weather or environment is a single table row.
 */
class PresentationRenderer {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<RenderState> = _state.asStateFlow()

    private var lastPresentation: Presentation = Presentation()
    private var lastManifest: UiManifest = UiManifest()
    private var lastSettings: AppSettings = AppSettings()

    fun submit(presentation: Presentation, manifest: UiManifest, settings: AppSettings) {
        lastPresentation = presentation
        lastManifest = manifest
        lastSettings = settings
        _state.value = resolve(presentation, manifest, settings)
    }

    fun onSettings(settings: AppSettings) = submit(lastPresentation, lastManifest, settings)

    private fun initialState(): RenderState {
        val p = Presentation()
        return resolve(p, UiManifest(), AppSettings())
    }

    private fun resolve(p: Presentation, m: UiManifest, s: AppSettings): RenderState {
        val mood = moodTable[p.mood] ?: moodTable[Mood.CALM]!!
        val weather = weatherTable[p.weather] ?: weatherTable["clear"]!!
        val environment = resolveEnvironment(p)
        val palette = Palette(
            bg = environment.bg ?: p.palette.bg,
            bgAlt = environment.bgAlt ?: p.palette.bgAlt,
            accent = environment.accent ?: p.palette.accent,
            text = p.palette.text,
            muted = p.palette.muted,
            vignette = p.palette.vignette,
        )

        val particleType = if (s.particles && (environment.particles?.type ?: p.particles.type) != "none") {
            environment.particles?.type ?: p.particles.type
        } else {
            "none"
        }
        val particles = Particles(
            type = particleType,
            density = (environment.particles?.density ?: p.particles.density) * weather.densityMul,
            speed = (environment.particles?.speed ?: p.particles.speed) * weather.speedMul,
        )

        val parallaxAllowed = s.parallax && m.enabled(com.textworld.app.data.model.Feature.PARALLAX)
        val parallaxFar = if (parallaxAllowed) p.parallax.far else "none"
        val parallaxMid = if (parallaxAllowed) p.parallax.mid else "none"
        val parallaxNear = if (parallaxAllowed) p.parallax.near else "none"

        val cues = if (m.enabled(com.textworld.app.data.model.Feature.AUDIO) && s.sound) {
            (environment.audioCues + p.audioCues).distinct()
        } else emptyList()

        val accentColor = mood.accentTint?.let { color(it) } ?: color(palette.accent)
        val ambient = s.animations && !s.reduceMotion
        val transitionMs = when (p.mood) {
            Mood.DREAD, Mood.TENSION -> Constants.THEME_INTERPOLATION_MAX_MS.toInt()
            else -> 700
        }.coerceIn(
            Constants.THEME_INTERPOLATION_MIN_MS.toInt(),
            Constants.THEME_INTERPOLATION_MAX_MS.toInt(),
        )

        val illustration = p.illustration
        val sceneKey = listOf(
            illustration?.seed, illustration?.subject, p.mood, p.weather, p.timeOfDay,
        ).joinToString("|")

        return RenderState(
            bg = color(palette.bg),
            bgAlt = color(palette.bgAlt),
            accent = accentColor,
            text = color(palette.text),
            muted = color(palette.muted),
            vignette = color(palette.vignette),
            vignetteAlpha = mood.vignetteAlpha,
            desaturation = mood.desaturation,
            pulse = mood.pulse && ambient,
            particles = particles,
            parallaxFar = parallaxFar,
            parallaxMid = parallaxMid,
            parallaxNear = parallaxNear,
            audioCues = cues,
            systemTheme = p.systemTheme.ifBlank { SystemTheme.HOLOGRAM },
            typography = p.typography,
            haptic = if (m.enabled(com.textworld.app.data.model.Feature.HAPTICS) && s.haptics) p.haptic else "none",
            emotionPlayer = p.emotion.player,
            npcEmotions = p.emotion.npcs,
            illustrationSeed = illustration?.seed ?: 0L,
            illustrationSubject = illustration?.subject ?: environment.subjectHint,
            illustrationStyle = illustration?.styleHint.orEmpty(),
            illustrationUrl = illustration?.url.takeIf { s.images && m.enabled(com.textworld.app.data.model.Feature.IMAGES) },
            portraitHints = p.portraitHints.mapValues { it.value.seed to it.value.url }
                .filterValues { (_, url) -> url == null || (s.images && m.enabled(com.textworld.app.data.model.Feature.IMAGES)) },
            sceneKey = sceneKey,
            transitionMs = transitionMs,
            ambientMotion = ambient,
            dropcap = p.typography.dropcap,
            statusGlow = s.statusGlow,
            imagesEnabled = s.images && m.enabled(com.textworld.app.data.model.Feature.IMAGES),
            themeSignature = sceneKey.hashCode(),
        )
    }

    /** Keys are "<far>/<time_of_day>" or a subject substring. One row = one look. */
    private fun resolveEnvironment(p: Presentation): EnvironmentTreatment {
        val far = p.parallax.far
        val time = p.timeOfDay
        val subject = p.illustration?.subject.orEmpty()
        return when {
            far == "forest" && time == "day" -> EnvironmentTreatment(
                bg = "#0B120D", bgAlt = "#121A12", accent = "#9FB75A",
                particles = Particles("motes", 0.35f, 0.25f),
                audioCues = listOf("birds", "wind_low"), subjectHint = "forest_day",
            )
            far == "forge" && time == "night" -> EnvironmentTreatment(
                bg = "#120C08", bgAlt = "#1D110A", accent = "#E08A2C",
                particles = Particles("ember", 0.6f, 0.45f),
                audioCues = listOf("forge_hammer", "fire_low"), subjectHint = "forge_night",
            )
            far == "mountains" && time == "dusk" -> EnvironmentTreatment(
                bg = "#0B1018", bgAlt = "#111A26", accent = "#A9C4D8",
                particles = Particles("snow", 0.45f, 0.3f),
                audioCues = listOf("wind_high"), subjectHint = "snow_dusk",
            )
            subject.contains("library", true) || far == "library" -> EnvironmentTreatment(
                bg = "#14100A", bgAlt = "#1C160E", accent = "#D4A94B",
                particles = Particles("dust", 0.3f, 0.2f),
                audioCues = listOf("page_rustle"), subjectHint = "library",
            )
            else -> EnvironmentTreatment()
        }
    }

    private data class MoodTreatment(
        val vignetteAlpha: Float,
        val desaturation: Float,
        val pulse: Boolean,
        val accentTint: String? = null,
    )

    private data class WeatherTreatment(
        val densityMul: Float = 1f,
        val speedMul: Float = 1f,
    )

    private data class EnvironmentTreatment(
        val bg: String? = null,
        val bgAlt: String? = null,
        val accent: String? = null,
        val particles: Particles? = null,
        val audioCues: List<String> = emptyList(),
        val subjectHint: String = "",
    )

    companion object {
        fun defaultRenderState(): RenderState = RenderState(
            bg = color("#0E0D0B"), bgAlt = color("#17130F"),
            accent = color("#C9A227"), text = color("#E8DECC"),
            muted = color("#8C8272"), vignette = Color.Black,
            vignetteAlpha = 0.1f, desaturation = 0f, pulse = false,
            particles = Particles(), parallaxFar = "none", parallaxMid = "none", parallaxNear = "none",
            audioCues = emptyList(), systemTheme = SystemTheme.HOLOGRAM,
            typography = Typography(), haptic = "none", emotionPlayer = "calm", npcEmotions = emptyMap(),
            illustrationSeed = 0L, illustrationSubject = "", illustrationStyle = "",
            illustrationUrl = null, portraitHints = emptyMap(), sceneKey = "default",
            transitionMs = 700, ambientMotion = true, dropcap = true, statusGlow = true,
            imagesEnabled = true, themeSignature = 0,
        )

        private val moodTable = mapOf(
            Mood.WONDER to MoodTreatment(0.06f, 0f, false),
            Mood.DREAD to MoodTreatment(0.72f, 0.65f, true, "#8E2A2A"),
            Mood.WARM to MoodTreatment(0.12f, 0f, false, "#C9A227"),
            Mood.MELANCHOLY to MoodTreatment(0.38f, 0.35f, false, "#9A93A8"),
            Mood.TENSION to MoodTreatment(0.5f, 0.4f, true, "#A8573C"),
            Mood.CALM to MoodTreatment(0.1f, 0f, false),
        )

        private val weatherTable = mapOf(
            "fog" to WeatherTreatment(1.2f, 0.7f),
            "rain" to WeatherTreatment(1.5f, 1.4f),
            "snow" to WeatherTreatment(1.3f, 0.8f),
            "ember" to WeatherTreatment(1.4f, 1.1f),
            "clear" to WeatherTreatment(1f, 1f),
            "wind" to WeatherTreatment(1.1f, 1.6f),
        )

        fun color(hex: String): Color {
            val raw = hex.trim().removePrefix("#")
            val value = runCatching {
                when (raw.length) {
                    6 -> "FF$raw".toLong(16)
                    8 -> raw.toLong(16)
                    else -> 0L
                }
            }.getOrDefault(0L)
            return Color(value)
        }
    }
}
