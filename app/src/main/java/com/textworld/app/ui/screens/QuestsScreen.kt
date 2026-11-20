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
import com.textworld.app.data.model.Quest
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun QuestsScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val quests = state.campaign?.quests.orEmpty()

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.quests_title))
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            listOf("main" to R.string.quests_main, "side" to R.string.quests_side, "system" to R.string.quests_system)
                .forEach { (kind, label) ->
                    val group = quests.filter { it.kind == kind }
                    if (group.isNotEmpty()) {
                        item {
                            Text(stringResource(label), color = colors.accent, style = typography.caption)
                        }
                        items(group) { QuestCard(it) }
                    }
                }
            if (quests.isEmpty()) {
                item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
            }
        }
    }
}

@Composable
private fun QuestCard(quest: Quest) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text(quest.name, color = colors.text, style = typography.prose)
        if (quest.description.isNotBlank()) Text(quest.description, color = colors.muted, style = typography.caption)
        quest.objectives.forEach { objective ->
            Row {
                Text(if (objective.done) "✦" else "◇", color = if (objective.done) colors.accent else colors.muted)
                Spacer(Modifier.padding(4.dp))
                Text(
                    objective.text,
                    color = if (objective.done) colors.muted else colors.text,
                    style = typography.proseSmall,
                )
            }
        }
        if (quest.timeLeft.isNotBlank()) Text(quest.timeLeft, color = colors.accent, style = typography.caption)
        if (quest.rewards.isNotEmpty()) {
            Text(quest.rewards.joinToString(", "), color = colors.muted, style = typography.caption)
        }
    }
}
