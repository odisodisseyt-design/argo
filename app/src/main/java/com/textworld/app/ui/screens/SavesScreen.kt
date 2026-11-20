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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/** Glowing thread tree. No local save state — branches live on the server. */
@Composable
fun SavesScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val step = state.campaign?.stepNo ?: 1

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.saves_title))
        Spacer(Modifier.height(8.dp))
        Text("${stringResource(R.string.saves_current)} · $step", color = colors.muted, style = typography.caption)
        Spacer(Modifier.height(12.dp))

        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                val x = size.width * 0.5f
                drawLine(
                    color = colors.accent.copy(alpha = 0.5f),
                    start = Offset(x, 0f), end = Offset(x, size.height),
                    strokeWidth = 3f,
                )
                listOf(0.2f, 0.45f, 0.7f, 0.92f).forEachIndexed { index, y ->
                    val dx = if (index % 2 == 0) -60f else 60f
                    drawLine(
                        color = colors.accent.copy(alpha = 0.25f),
                        start = Offset(x, size.height * y - 40f),
                        end = Offset(x + dx, size.height * y),
                        strokeWidth = 2f,
                    )
                    drawCircle(colors.accent.copy(alpha = 0.8f), radius = 8f, center = Offset(x + dx, size.height * y))
                }
            }
        }

        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.rewind() }, colors = ButtonDefaults.buttonColors(containerColor = colors.bgAlt)) {
                Text(stringResource(R.string.saves_rewind))
            }
            Button(onClick = { vm.saveBranch() }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg)) {
                Text(stringResource(R.string.saves_branch))
            }
        }
    }
}
