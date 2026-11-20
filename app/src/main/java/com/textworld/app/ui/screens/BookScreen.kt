package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.readableStatus
import com.textworld.app.data.model.skillsAsModels
import com.textworld.app.presentation.components.RuneIcon
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel
import com.textworld.app.data.repository.Overlay

@Composable
fun BookScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val character = state.campaign?.character
    val manifest = state.manifest

    val tabs = buildList {
        if (manifest.enabled(com.textworld.app.data.model.Feature.STATS)) add(BookTab.STATS)
        if (manifest.enabled(com.textworld.app.data.model.Feature.SKILLS)) add(BookTab.SKILLS)
        if (manifest.enabled(com.textworld.app.data.model.Feature.EFFECTS)) add(BookTab.STATUS)
        add(BookTab.BODY)
        if (manifest.enabled(com.textworld.app.data.model.Feature.CURRENCY)) add(BookTab.LOOT)
    }
    var tabIndex by remember { mutableStateOf(0) }
    val current = tabs.getOrNull(tabIndex) ?: BookTab.BODY

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.nav_book))
        Spacer(Modifier.height(8.dp))
        if (character != null) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.book_level, character.level),
                    color = colors.accent,
                    style = typography.prose,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (character.alive) stringResource(R.string.book_alive) else stringResource(R.string.book_dead),
                    color = if (character.alive) colors.muted else androidx.compose.ui.graphics.Color(0xFFB45A4A),
                    style = typography.caption,
                )
            }
            if (manifest.enabled(com.textworld.app.data.model.Feature.STATS)) {
                val progress = if (character.xpToNext > 0) character.xp.toFloat() / character.xpToNext else 0f
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    color = colors.accent,
                    trackColor = colors.muted.copy(alpha = 0.25f),
                )
                Text(
                    "${stringResource(R.string.book_xp)} ${character.xp} / ${character.xpToNext}",
                    color = colors.muted,
                    style = typography.caption,
                )
            }
            if (state.levelUpAvailable) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.setOverlay(Overlay.LEVEL_UP) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg),
                ) {
                    Text("${stringResource(R.string.levelup_title)} · ${character.unspentPoints}")
                }
            }
        }

        if (tabs.size > 1) {
            TabRow(selectedTabIndex = tabIndex, containerColor = colors.bgAlt.copy(alpha = 0.6f)) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(tab.label(), style = typography.caption) },
                        selectedContentColor = colors.accent,
                        unselectedContentColor = colors.muted,
                    )
                }
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            when (current) {
                BookTab.STATS -> {
                    character?.attributes?.forEach { (id, value) ->
                        val schema = manifest.statSchema.find { it.id == id }
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            RuneIcon(schema?.icon.orEmpty(), colors.accent)
                            Spacer(Modifier.width(10.dp))
                            Text(schema?.name ?: id, color = colors.text, style = typography.prose)
                            Spacer(Modifier.weight(1f))
                            Text(value.toString(), color = colors.accent, style = typography.prose)
                        }
                    }
                    character?.derived?.forEach { (k, v) ->
                        Text("$k: $v", color = colors.muted, style = typography.caption)
                    }
                }
                BookTab.SKILLS -> {
                    character?.skillsAsModels()?.forEach { skill ->
                        Text(
                            "${skill.name} · ${skill.level}",
                            color = colors.text,
                            style = typography.prose,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        if (skill.description.isNotBlank()) {
                            Text(skill.description, color = colors.muted, style = typography.caption)
                        }
                    }
                }
                BookTab.STATUS -> {
                    val all = character?.conditions.orEmpty().map { readableStatus(it) } +
                        character?.statuses.orEmpty().map { readableStatus(it) }
                    all.filter { it.isNotBlank() }.forEach {
                        Row(Modifier.padding(vertical = 4.dp)) {
                            RuneIcon(manifest.statusIcons[it].orEmpty(), colors.accent)
                            Spacer(Modifier.width(10.dp))
                            Text(it, color = colors.text, style = typography.prose)
                        }
                    }
                }
                BookTab.BODY -> {
                    character?.body?.forEach { (part, value) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(part, color = colors.muted, style = typography.prose)
                            Spacer(Modifier.weight(1f))
                            Text(readableStatus(value, "цело"), color = colors.text, style = typography.prose)
                        }
                    }
                }
                BookTab.LOOT -> {
                    character?.currency?.forEach { (name, amount) ->
                        Text(
                            "$amount ${manifest.currencyLabel}",
                            color = colors.accent,
                            style = typography.prose,
                        )
                    }
                    character?.equipment?.forEach { (slot, item) ->
                        Text("$slot: $item", color = colors.muted, style = typography.caption)
                    }
                }
            }
        }
    }
}

private enum class BookTab {
    STATS, SKILLS, STATUS, BODY, LOOT;

    @Composable
    fun label() = when (this) {
        STATS -> stringResource(R.string.book_attributes)
        SKILLS -> stringResource(R.string.book_skills)
        STATUS -> stringResource(R.string.book_statuses)
        BODY -> stringResource(R.string.book_body)
        LOOT -> stringResource(R.string.book_currency)
    }
}
