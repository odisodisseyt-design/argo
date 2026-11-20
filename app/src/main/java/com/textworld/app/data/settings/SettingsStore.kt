package com.textworld.app.data.settings

import android.content.Context
import com.textworld.app.BuildConfig
import com.textworld.app.core.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val serverUrl: String = BuildConfig.DEFAULT_SERVER_URL,
    val reduceMotion: Boolean = false,
    val fontScale: Float = 1f,
    val theme: String = "system",
    val particles: Boolean = true,
    val parallax: Boolean = true,
    val sound: Boolean = true,
    val images: Boolean = true,
    val haptics: Boolean = true,
    val statusGlow: Boolean = true,
    val animations: Boolean = true,
    val voice: Boolean = false,
)

/** Persisted locally and (best-effort) mirrored to the server. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(Constants.SETTINGS_PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load() = AppSettings(
        serverUrl = prefs.getString("server_url", BuildConfig.DEFAULT_SERVER_URL).orEmpty(),
        reduceMotion = prefs.getBoolean("reduce_motion", false),
        fontScale = prefs.getFloat("font_scale", 1f),
        theme = prefs.getString("theme", "system") ?: "system",
        particles = prefs.getBoolean("particles", true),
        parallax = prefs.getBoolean("parallax", true),
        sound = prefs.getBoolean("sound", true),
        images = prefs.getBoolean("images", true),
        haptics = prefs.getBoolean("haptics", true),
        statusGlow = prefs.getBoolean("status_glow", true),
        animations = prefs.getBoolean("animations", true),
        voice = prefs.getBoolean("voice", false),
    )

    var activeCampaignId: String?
        get() = prefs.getString("active_campaign_id", null)
        set(value) = prefs.edit().putString("active_campaign_id", value).apply()

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        prefs.edit()
            .putString("server_url", next.serverUrl)
            .putBoolean("reduce_motion", next.reduceMotion)
            .putFloat("font_scale", next.fontScale)
            .putString("theme", next.theme)
            .putBoolean("particles", next.particles)
            .putBoolean("parallax", next.parallax)
            .putBoolean("sound", next.sound)
            .putBoolean("images", next.images)
            .putBoolean("haptics", next.haptics)
            .putBoolean("status_glow", next.statusGlow)
            .putBoolean("animations", next.animations)
            .putBoolean("voice", next.voice)
            .apply()
        _settings.value = next
    }

    /** Returns the settings payload to mirror via POST /api/settings. */
    fun payload(): Map<String, Any?> = with(_settings.value) {
        mapOf(
            "reduce_motion" to reduceMotion,
            "font_scale" to fontScale,
            "theme" to theme,
            "particles" to particles,
            "parallax" to parallax,
            "sound" to sound,
            "images" to images,
            "haptics" to haptics,
            "status_glow" to statusGlow,
            "animations" to animations,
            "voice" to voice,
        )
    }
}
