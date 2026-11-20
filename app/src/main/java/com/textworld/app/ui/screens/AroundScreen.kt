package com.textworld.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.Nearby
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.unit.toSize

@Composable
fun AroundScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val nearby = state.campaign?.anchor?.nearby.orEmpty()
    var area by remember { mutableStateOf(Size.Zero) }
    var selected by remember { mutableStateOf<Nearby?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(screenModifier()) {
            ScreenTitle(stringResource(R.string.around_title))
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { area = it.toSize() },
            ) {
                // Compass ring
                Box(
                    Modifier
                        .size(260.dp, 260.dp)
                        .align(Alignment.Center)
                        .background(colors.accent.copy(alpha = 0.04f), CircleShape),
                )
                listOf(
                    stringResource(R.string.around_north) to 0f,
                    stringResource(R.string.around_east) to 90f,
                    stringResource(R.string.around_south) to 180f,
                    stringResource(R.string.around_west) to 270f,
                ).forEach { (label, angle) ->
                    val rad = Math.toRadians(angle.toDouble() - 90)
                    val r = 140f
                    Text(
                        label,
                        color = colors.muted,
                        style = typography.caption,
                        modifier = Modifier.offset {
                            IntOffset(
                                (area.width / 2 + cos(rad) * r).toInt(),
                                (area.height / 2 + sin(rad) * r).toInt(),
                            )
                        },
                    )
                }
                // Player
                Text(
                    "✦",
                    color = colors.accent,
                    style = typography.display,
                    modifier = Modifier.align(Alignment.Center),
                )
                // Objects positioned by distance and direction from the server.
                nearby.forEach { item ->
                    val angle = when (item.direction.trim().uppercase()) {
                        "С", "СЕВ", "N" -> 0f
                        "В", "ВОСТ", "E" -> 90f
                        "Ю", "ЮЖ", "S" -> 180f
                        "З", "ЗАП", "W" -> 270f
                        else -> 45f
                    }
                    val rad = Math.toRadians(angle.toDouble() - 90)
                    val radius = (40f + item.distanceM.coerceAtMost(40f) * 5f)
                    Text(
                        item.name,
                        color = colors.text,
                        style = typography.caption,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (area.width / 2 + cos(rad) * radius).toInt(),
                                    (area.height / 2 + sin(rad) * radius).toInt(),
                                )
                            }
                            .clickable { selected = item }
                            .padding(4.dp),
                    )
                }
            }
        }

        selected?.let { item ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                    .clickable { selected = null },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(colors.bgAlt)
                        .clickable(enabled = false) {}
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(item.name, color = colors.text, style = typography.prose)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.around_distance, item.distanceM.toInt()),
                        color = colors.muted,
                        style = typography.caption,
                    )
                    item.heightM?.let {
                        Text(stringResource(R.string.around_height, it), color = colors.muted, style = typography.caption)
                    }
                    item.properties.forEach { (k, v) ->
                        Text("$k: $v", color = colors.muted, style = typography.caption)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.around_can), color = colors.accent, style = typography.caption)
                    item.affordances.forEach { Text("· $it", color = colors.text, style = typography.proseSmall) }
                    if (item.forbidden.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.around_cannot), color = colors.muted, style = typography.caption)
                        item.forbidden.forEach { Text("· $it", color = colors.muted, style = typography.proseSmall) }
                    }
                }
            }
        }
    }
}
