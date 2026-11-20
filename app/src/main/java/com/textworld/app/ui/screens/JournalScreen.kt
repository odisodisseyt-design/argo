package com.textworld.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.JournalEntry
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun JournalScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val entries = state.journal.ifEmpty { state.campaign?.journal.orEmpty() }

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.journal_title))
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
            items(entries) { entry -> JournalRow(entry, vm) }
            if (entries.isEmpty()) {
                item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
            }
        }
    }
}

@Composable
private fun JournalRow(entry: JournalEntry, vm: MainViewModel) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val render by vm.renderState.collectAsStateWithLifecycle()
    val shimmer by androidx.compose.runtime.rememberInfiniteTransition(label = "shimmer").animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(1_600, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmer",
    )
    val alpha = if (entry.status == "pending" && render.ambientMotion) shimmer else 1f

    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        // Wax seal in the margin for canon facts.
        Text(
            if (entry.status == "canon") "●" else "○",
            color = if (entry.status == "canon") androidx.compose.ui.graphics.Color(0xFF8E2A2A) else colors.muted,
            style = typography.prose,
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.text,
                color = if (entry.status == "rejected") colors.muted else colors.text,
                style = typography.proseSmall,
                modifier = Modifier.alpha(alpha),
            )
            Text(
                "${entry.source} · ${statusLabel(entry.status)}",
                color = colors.muted,
                style = typography.caption,
            )
            if (entry.status == "pending") {
                Row {
                    TextButton(onClick = { vm.acceptDetail(entry.id, true) }) {
                        Text(stringResource(R.string.journal_accept_canon), color = colors.accent)
                    }
                    TextButton(onClick = { vm.acceptDetail(entry.id, false) }) {
                        Text(stringResource(R.string.reject), color = colors.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: String) = when (status) {
    "canon" -> stringResource(R.string.journal_canon)
    "rejected" -> stringResource(R.string.journal_rejected)
    else -> stringResource(R.string.journal_pending)
}
