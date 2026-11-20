package com.textworld.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.textworld.app.R
import com.textworld.app.presentation.renderer.PresentationRenderer
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.presentation.theme.LocalRenderState
import kotlinx.coroutines.delay

/** Golden ring breathes while the world is thinking. */
@Composable
fun BreathingActionInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    waiting: Boolean,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBookColors.current
    val render = LocalRenderState.current
    val ring by rememberInfiniteTransition(label = "ring").animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(1_800, easing = LinearEasing), RepeatMode.Reverse),
        label = "ring",
    )
    val ringAlpha = if (waiting && render.ambientMotion) ring else if (waiting) 0.7f else 0.35f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bgAlt.copy(alpha = 0.65f))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.accent.copy(alpha = ringAlpha), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(size.width, 0f),
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, 2f),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            textStyle = LocalBookTypography.current.prose.copy(color = colors.text),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(hint, color = colors.muted, style = LocalBookTypography.current.caption)
                inner()
            },
        )
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = onSend, enabled = enabled && value.isNotBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = stringResource(R.string.cd_send),
                tint = if (enabled) colors.accent else colors.muted,
            )
        }
    }
}

/** A paper slip flies into the scene when an action is sent. */
@Composable
fun PaperSlip(trigger: Int) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, tween(520, easing = LinearEasing))
    }
    val p = progress.value
    val render = LocalRenderState.current
    if (render.ambientMotion && p > 0f && p < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * (0.82f - 0.5f * p)
            val x = size.width * (0.5f + 0.18f * (1f - p))
            drawRect(
                color = Color(0xFFE8DECC).copy(alpha = 0.75f * (1f - p)),
                topLeft = Offset(x - 40f, y - 10f),
                size = androidx.compose.ui.geometry.Size(80f, 20f),
            )
        }
    }
}

/** Page turn with a curling shadow; used for entry and branch switches. */
@Composable
fun PageTurnOverlay(trigger: Int, onDone: () -> Unit = {}) {
    val progress = remember { Animatable(0f) }
    val render = LocalRenderState.current
    LaunchedEffect(trigger) {
        if (trigger == 0 || !render.ambientMotion) {
            if (trigger != 0) onDone()
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(760, easing = LinearEasing))
        onDone()
    }
    val p = progress.value
    if (p > 0f && p < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * p
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    start = Offset(x - size.width * 0.25f, 0f), end = Offset(x, 0f),
                ),
                topLeft = Offset(x - size.width * 0.25f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.25f, size.height),
            )
        }
    }
}

/** Pages fly backwards with a slight desaturation when the branch is rewound. */
@Composable
fun RewindEffect(trigger: Int) {
    val progress = remember { Animatable(0f) }
    val render = LocalRenderState.current
    LaunchedEffect(trigger) {
        if (trigger == 0 || !render.ambientMotion) return@LaunchedEffect
        progress.snapTo(1f)
        progress.animateTo(0f, tween(880, easing = LinearEasing))
    }
    val p = progress.value
    if (trigger > 0 && p > 0f && p < 1f && render.ambientMotion) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * p
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Gray.copy(alpha = 0.18f), Color.Transparent),
                    start = Offset(x, 0f), end = Offset(x - size.width * 0.3f, 0f),
                ),
                topLeft = Offset(x - size.width * 0.3f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height),
            )
        }
    }
}

/** End of story: the world desaturates and the vignette closes in. */
@Composable
fun DeathVeil(visible: Boolean, onRewind: () -> Unit, onBranch: () -> Unit) {
    val colors = LocalBookColors.current
    val transition = updateTransition(visible, label = "death")
    val alpha by transition.animateFloat({ tween(1_400) }, label = "veil") { if (it) 1f else 0f }
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(1_200)), exit = fadeOut()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f * alpha)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.death_line),
                    color = colors.text.copy(alpha = alpha),
                    style = LocalBookTypography.current.display,
                )
                Spacer(Modifier.height(40.dp))
                Row {
                    Button(onClick = onRewind, colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                        Text(stringResource(R.string.death_rewind))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = onBranch, colors = ButtonDefaults.buttonColors(containerColor = colors.bgAlt)) {
                        Text(stringResource(R.string.death_branch))
                    }
                }
            }
        }
    }
}

/** Co-op waiting state: pulsing rune, not a spinner. */
@Composable
fun WaitingState(name: String?, frozenStep: Int?) {
    val colors = LocalBookColors.current
    val render = LocalRenderState.current
    val pulse by rememberInfiniteTransition(label = "wait").animateFloat(
        0.25f, 0.9f,
        infiniteRepeatable(tween(1_600, easing = LinearEasing), RepeatMode.Reverse),
        label = "wait",
    )
    val a = if (render.ambientMotion) pulse else 0.6f
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("ᛟ", color = colors.accent.copy(alpha = a), style = LocalBookTypography.current.display)
        Spacer(Modifier.height(8.dp))
        val text = if (frozenStep != null) {
            stringResource(R.string.scene_frozen, frozenStep)
        } else {
            stringResource(R.string.scene_waiting_for, name.orEmpty())
        }
        Text(text, color = colors.muted, style = LocalBookTypography.current.caption)
    }
}

/** Neutral rune fallback for any unknown status/feature icon string. */
@Composable
fun RuneIcon(name: String, tint: Color, size: Int = 18) {
    val glyph = when (name.lowercase()) {
        "drop", "bleeding" -> "◈"
        "moon", "unconscious" -> "☾"
        "crutch", "wounded_leg" -> "†"
        "heart", "vit" -> "♥"
        "muscle", "str" -> "◆"
        "feather", "agi" -> "➶"
        "brain", "int" -> "✧"
        "fire", "ember" -> "♨"
        else -> "ᛟ"
    }
    Text(text = glyph, color = tint, fontSize = androidx.compose.ui.unit.TextUnit(size.dp.value, androidx.compose.ui.unit.TextUnitType.Sp))
}
