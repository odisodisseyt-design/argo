package com.textworld.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/** Golden-ink parchment for spending points. Bounds come from stat_schema. */
@Composable
fun LevelUpScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val character = state.campaign?.character
    val allocations = remember { mutableStateMapOf<String, Int>() }
    val spent = allocations.values.sum()
    val left = (character?.unspentPoints ?: 0) - spent
    val golden = androidx.compose.ui.graphics.Color(0xFFD8B24C)

    val sink by animateFloatAsState(if (left == 0) 1f else 0f, tween(600), label = "sink")

    Box(
        Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(colors.bgAlt)
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(stringResource(R.string.levelup_title), color = golden, style = typography.display)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.levelup_left, left.coerceAtLeast(0)),
                color = colors.text,
                style = typography.prose,
            )
            Spacer(Modifier.height(16.dp))

            state.manifest.statSchema.forEach { schema ->
                val current = (character?.attributes?.get(schema.id) ?: schema.min)
                val pending = allocations[schema.id] ?: 0
                if (schema.costBase != null && schema.costNext != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            schema.costBase.toString(),
                            color = colors.muted,
                            style = typography.caption.copy(textDecoration = TextDecoration.LineThrough),
                        )
                        Text(schema.costNext.toString(), color = golden, style = typography.prose)
                        schema.discountReason?.let {
                            Text(stringResource(R.string.levelup_discount, it), color = colors.muted, style = typography.caption)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(schema.name, color = colors.text, style = typography.prose, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { if (pending > 0) allocations[schema.id] = pending - 1 },
                        enabled = pending > 0,
                    ) { Text("−", color = if (pending > 0) golden else colors.muted, style = typography.prose) }
                    Text("${current + pending}", color = golden, style = typography.prose)
                    IconButton(
                        onClick = {
                            if (left > 0 && current + pending < schema.max) {
                                allocations[schema.id] = pending + 1
                            }
                        },
                        enabled = left > 0 && current + pending < schema.max,
                    ) { Text("+", color = golden, style = typography.prose) }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    vm.allocate(allocations.filter { it.value > 0 }.map { it.key to it.value })
                },
                enabled = left == 0 && spent > 0,
                modifier = Modifier.fillMaxWidth().alpha(1f - sink * 0.4f),
                colors = ButtonDefaults.buttonColors(containerColor = golden, contentColor = colors.bg),
            ) { Text(stringResource(R.string.levelup_accept)) }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextButton(onClick = { vm.setOverlay(com.textworld.app.data.repository.Overlay.NONE) }) {
                Text(stringResource(R.string.cancel), color = colors.muted)
            }
        }
    }
}
