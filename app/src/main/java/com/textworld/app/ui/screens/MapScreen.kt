package com.textworld.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.MapNode
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/** Living map: undiscovered nodes stay in fog, paths grow between visited nodes. */
@Composable
fun MapScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val nodes = state.campaign?.mapNodes.orEmpty()
    val maxX = nodes.maxOfOrNull { it.x }?.coerceAtLeast(1f) ?: 1f
    val maxY = nodes.maxOfOrNull { it.y }?.coerceAtLeast(1f) ?: 1f

    Column(screenModifier()) {
        androidx.compose.material3.Text(stringResource(R.string.map_title), color = colors.text, style = typography.display)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().weight(1f).padding(8.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                nodes.filter { it.visited }.forEach { node ->
                    node.connections.forEach { targetId ->
                        val target = nodes.find { it.id == targetId } ?: return@forEach
                        if (!target.visited) return@forEach
                        drawLine(
                            color = colors.accent.copy(alpha = 0.35f),
                            start = Offset(node.x / maxX * size.width, node.y / maxY * size.height),
                            end = Offset(target.x / maxX * size.width, target.y / maxY * size.height),
                            strokeWidth = 2f,
                        )
                    }
                }
                nodes.forEach { node ->
                    val x = node.x / maxX * size.width
                    val y = node.y / maxY * size.height
                    drawCircle(
                        color = if (node.visited) colors.accent else colors.muted.copy(alpha = 0.25f),
                        radius = if (node.visited) 10f else 6f,
                        center = Offset(x, y),
                    )
                }
            }
        }
    }
}
