package com.textworld.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.readableStatus
import com.textworld.app.presentation.components.BreathingActionInput
import com.textworld.app.presentation.components.RuneIcon
import com.textworld.app.presentation.components.StreamingProse
import com.textworld.app.presentation.components.WaitingState
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel
import com.textworld.app.data.model.anchorClock
import com.textworld.app.data.model.anchorCoordinates
import com.textworld.app.data.model.anchorElevation
import com.textworld.app.data.model.anchorLocationName
import com.textworld.app.data.model.LivingWorld

@Composable
fun SceneScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val render by vm.renderState.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    var expanded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val anchor = state.campaign?.anchor
    val character = state.campaign?.character
    val notMyTurn = state.waitingFor.isNotEmpty() && !state.waitingFor.contains(state.myUserId)
    val frozen = state.frozenStep != null && state.waitingFor.isNotEmpty()
    val canAct = !notMyTurn && !frozen && !state.dead

    Column(screenModifier()) {
        SceneAnchor(
            expanded = expanded,
            onToggle = { expanded = !expanded },
            location = anchor?.let { anchorLocationName(it) }.orEmpty(),
            coords = anchor?.let { anchorCoordinates(it) }.orEmpty(),
            elevation = anchor?.let { anchorElevation(it) }.orEmpty(),
            clock = anchor?.let { anchorClock(it) }.orEmpty(),
            weather = anchor?.weather.orEmpty(),
            visibility = anchor?.visibilityM ?: 0,
            conditions = character?.conditions.orEmpty().map { readableStatus(it) },
            statuses = character?.statuses.orEmpty().map { readableStatus(it) },
            statusIcons = state.manifest.statusIcons,
        )

        LivingStrip(
            era = state.era,
            eraName = state.eraName,
            living = state.living,
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            StreamingProse(
                text = state.prose,
                paceMs = render.typography.paceMs,
                animate = render.ambientMotion,
                dropcap = render.dropcap,
            )
            if (state.outcome.isNotBlank()) {
                Text(
                    state.outcome,
                    color = colors.muted,
                    style = typography.caption,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (notMyTurn || frozen) {
                WaitingState(
                    name = state.waitingName,
                    frozenStep = state.frozenStep,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (canAct) {
            BreathingActionInput(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    if (input.isNotBlank()) {
                        vm.submit(input.trim())
                        input = ""
                    }
                },
                enabled = !state.streaming,
                waiting = state.streaming,
                hint = stringResource(R.string.scene_input_hint),
            )
        }
    }
}

/** Era + living world strip: time, weather, temperature, refuge, rumors. */
@Composable
private fun LivingStrip(era: String, eraName: String, living: LivingWorld) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(era.ifBlank { "E0" }, color = colors.accent, style = typography.caption)
            Text(eraName.ifBlank { "" }, color = colors.muted, style = typography.caption)
            living.weather.let {
                if (it.isNotBlank()) Text(it, color = colors.muted, style = typography.caption)
            }
            living.temperatureC.let {
                if (it != 0) Text("$it °C", color = colors.muted, style = typography.caption)
            }
            if (living.time.hour > 0) {
                Text("%02d:00".format(living.time.hour), color = colors.muted, style = typography.caption)
            }
        }
        if (living.refuge.status != "none") {
            Text(
                "${living.refuge.name} · ур. ${living.refuge.level}",
                color = colors.accent,
                style = typography.caption,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        val tickers = living.events + living.rumors
        if (tickers.isNotEmpty()) {
            Text(tickers.take(2).joinToString(" · "), color = colors.muted, style = typography.caption)
        }
    }
}

@Composable
private fun SceneAnchor(
    expanded: Boolean,
    onToggle: () -> Unit,
    location: String,
    coords: String,
    elevation: String,
    clock: String,
    weather: String,
    visibility: Int,
    conditions: List<String>,
    statuses: List<String>,
    statusIcons: Map<String, String>,
) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(location.ifBlank { stringResource(R.string.scene_where) }, color = colors.text, style = typography.prose)
            Spacer(Modifier.weight(1f))
            Text(if (expanded) "⌃" else "⌄", color = colors.muted, style = typography.caption)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (coords.isNotBlank()) Text(coords, color = colors.muted, style = typography.caption)
            if (elevation.isNotBlank()) Text(elevation, color = colors.muted, style = typography.caption)
            if (clock.isNotBlank()) Text(clock, color = colors.muted, style = typography.caption)
        }
        AnimatedVisibility(expanded) {
            Column {
                if (weather.isNotBlank()) {
                    Text("${stringResource(R.string.scene_weather)}: $weather", color = colors.muted, style = typography.caption)
                }
                if (visibility > 0) {
                    Text("${stringResource(R.string.scene_visibility)}: $visibility м", color = colors.muted, style = typography.caption)
                }
                val all = (conditions + statuses).filter { it.isNotBlank() }
                if (all.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        all.forEach { label ->
                            val icon = statusIcons[label].orEmpty()
                            RuneIcon(icon.ifBlank { "moon" }, colors.accent)
                            Text(label, color = colors.muted, style = typography.caption)
                        }
                    }
                }
            }
        }
    }
}
