package com.textworld.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textworld.app.R
import com.textworld.app.presentation.renderer.PresentationRenderer
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import kotlinx.coroutines.delay

/** Not a splash: a cover that breathes, with staggered parallax layers. */
@Composable
fun EntryScreen(onOpen: () -> Unit, onLongPressCover: () -> Unit) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(260)
        ready = true
    }
    val glow by animateFloatAsState(if (ready) 1f else 0f, tween(1_800), label = "coverGlow")
    val layer1 by animateFloatAsState(if (ready) 1f else 0f, tween(900, delayMillis = 120), label = "l1")
    val layer2 by animateFloatAsState(if (ready) 1f else 0f, tween(900, delayMillis = 320), label = "l2")
    val layer3 by animateFloatAsState(if (ready) 1f else 0f, tween(900, delayMillis = 520), label = "l3")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPressCover() }) },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                "ᛟ",
                color = colors.accent.copy(alpha = 0.5f * glow),
                style = typography.display.copy(fontSize = typography.display.fontSize * 2f),
                modifier = Modifier.alpha(layer1),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.entry_title).uppercase(),
                color = colors.text.copy(alpha = layer2),
                style = typography.display.copy(letterSpacing = 8f.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accent.copy(alpha = 0.12f * glow), Color.Transparent),
                        ),
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.entry_subtitle),
                color = colors.muted.copy(alpha = layer3),
                style = typography.caption,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(56.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.alpha(layer3),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent.copy(alpha = 0.85f),
                    contentColor = colors.bg,
                ),
            ) {
                Text(stringResource(R.string.entry_open))
            }
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.entry_settings_hint),
                color = colors.muted.copy(alpha = 0.5f),
                style = typography.caption,
                textAlign = TextAlign.Center,
            )
        }
    }
}
