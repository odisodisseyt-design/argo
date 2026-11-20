package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.NpcDossier
import com.textworld.app.data.model.readableStatus
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun CharactersScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val npcs = state.campaign?.npcs.orEmpty()

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.characters_title))
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            items(npcs) { npc ->
                NpcCard(npc, state.presentation.emotion.npcs[npc.id].orEmpty())
            }
            if (npcs.isEmpty()) {
                item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
            }
        }
    }
}

@Composable
private fun NpcCard(npc: NpcDossier, emotion: String) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val tint = when (emotion.lowercase()) {
        "suspicious", "hostile", "angry" -> androidx.compose.ui.graphics.Color(0xFFB45A4A)
        "friendly", "warm" -> colors.accent
        else -> colors.muted
    }
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row {
            Text("ᛟ", color = tint, style = typography.display)
            Spacer(Modifier.padding(4.dp))
            Column {
                Text(npc.name, color = colors.text, style = typography.prose)
                Text(
                    if (npc.alive) npc.emotion else stringResource(R.string.book_dead),
                    color = tint,
                    style = typography.caption,
                )
            }
        }
        if (npc.description.isNotBlank()) Text(npc.description, color = colors.muted, style = typography.proseSmall)
        npc.goals.forEach { Text("• $it", color = colors.muted, style = typography.caption) }
        npc.known.forEach { Text("◦ $it", color = colors.muted, style = typography.caption) }
        npc.relations.forEach { (faction, value) ->
            Text("$faction: $value", color = colors.accent, style = typography.caption)
        }
        npc.conditions.map { readableStatus(it) }.filter { it.isNotBlank() }.forEach {
            Text(it, color = tint, style = typography.caption)
        }
    }
}
