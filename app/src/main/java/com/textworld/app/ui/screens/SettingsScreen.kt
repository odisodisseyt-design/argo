package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.repository.Overlay
import com.textworld.app.data.settings.AppSettings
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/** Two-level gating: manifest decides which toggles exist; player may disable any. */
@Composable
fun SettingsScreen(vm: MainViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val manifest = state.manifest
    Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
        Row {
            ScreenTitle(stringResource(R.string.settings_title), modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.setOverlay(Overlay.NONE) }) { Text(stringResource(R.string.close), color = colors.muted) }
        }
        Spacer(Modifier.height(12.dp))

        Toggle(stringResource(R.string.settings_reduce_motion), settings.reduceMotion) {
            vm.updateSettings { it.copy(reduceMotion = it.reduceMotion.not()) }
        }
        Toggle("${stringResource(R.string.settings_font_scale)} ×${"%.2f".format(settings.fontScale)}", false) {
            val next = if (settings.fontScale >= 1.6f) 0.9f else settings.fontScale + 0.1f
            vm.updateSettings { it.copy(fontScale = next) }
        }
        Toggle(stringResource(R.string.settings_theme), false) {
            val next = when (settings.theme) { "system" -> "dark"; "dark" -> "book"; else -> "system" }
            vm.updateSettings { it.copy(theme = next) }
        }
        Text(settings.theme, color = colors.muted, style = typography.caption)

        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.settings_effects), color = colors.accent, style = typography.caption)
        if (manifest.enabled(com.textworld.app.data.model.Feature.PARALLAX)) {
            Toggle(stringResource(R.string.settings_particles), settings.particles) {
                vm.updateSettings { it.copy(particles = it.particles.not()) }
            }
            Toggle(stringResource(R.string.settings_parallax), settings.parallax) {
                vm.updateSettings { it.copy(parallax = it.parallax.not()) }
            }
        }
        if (manifest.enabled(com.textworld.app.data.model.Feature.AUDIO)) {
            Toggle(stringResource(R.string.settings_sound), settings.sound) {
                vm.updateSettings { it.copy(sound = it.sound.not()) }
            }
        }
        if (manifest.enabled(com.textworld.app.data.model.Feature.IMAGES)) {
            Toggle(stringResource(R.string.settings_images), settings.images) {
                vm.updateSettings { it.copy(images = it.images.not()) }
            }
        }
        if (manifest.enabled(com.textworld.app.data.model.Feature.HAPTICS)) {
            Toggle(stringResource(R.string.settings_haptics), settings.haptics) {
                vm.updateSettings { it.copy(haptics = it.haptics.not()) }
            }
        }
        Toggle(stringResource(R.string.settings_status_glow), settings.statusGlow) {
            vm.updateSettings { it.copy(statusGlow = it.statusGlow.not()) }
        }
        Toggle(stringResource(R.string.settings_animations), settings.animations) {
            vm.updateSettings { it.copy(animations = it.animations.not()) }
        }
        if (manifest.enabled(com.textworld.app.data.model.Feature.VOICE)) {
            Toggle(stringResource(R.string.settings_voice), settings.voice) {
                vm.updateSettings { it.copy(voice = it.voice.not()) }
            }
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = { vm.setOverlay(Overlay.ABOUT) }) {
            Text(stringResource(R.string.about_title), color = colors.accent)
        }
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, onChange: () -> Unit) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = colors.text, style = typography.prose, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { onChange() },
            colors = SwitchDefaults.colors(checkedThumbColor = colors.accent, checkedTrackColor = colors.accent.copy(alpha = 0.4f)),
        )
    }
}
