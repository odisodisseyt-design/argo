package com.textworld.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import kotlinx.coroutines.delay

/**
 * Prose appears at presentation.typography.pace_ms with a soft sheen. It is a
 * pure renderer: it receives already-written server text and never edits it.
 */
@Composable
fun StreamingProse(
    text: String,
    paceMs: Int,
    animate: Boolean,
    dropcap: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    var visible by remember(text) { mutableIntStateOf(if (animate) 0 else text.length) }

    LaunchedEffect(text, animate) {
        if (!animate) {
            visible = text.length
            return@LaunchedEffect
        }
        visible = 0
        val pace = paceMs.coerceIn(4, 80)
        while (visible < text.length) {
            delay(pace.toLong())
            visible = (visible + 2).coerceAtMost(text.length)
        }
    }

    val shown = text.take(visible)
    val paragraphs = shown.split("\n\n")
    val transition = rememberInfiniteTransition(label = "sheen")
    val sheen by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2_600, easing = LinearEasing), RepeatMode.Restart),
        label = "sheen",
    )

    ColumnWithDropcap(
        paragraphs = paragraphs,
        dropcap = dropcap && text.isNotBlank(),
        style = typography.prose,
        dropColor = colors.accent,
        textColor = colors.text,
        sheen = if (animate && visible < text.length) sheen else null,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ColumnWithDropcap(
    paragraphs: List<String>,
    dropcap: Boolean,
    style: TextStyle,
    dropColor: Color,
    textColor: Color,
    sheen: Float?,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        paragraphs.forEachIndexed { index, paragraph ->
            if (paragraph.isBlank()) return@forEachIndexed
            if (dropcap && index == 0 && paragraph.length > 1) {
                Row {
                    Text(
                        text = paragraph.first().toString(),
                        color = dropColor,
                        style = style.copy(fontSize = style.fontSize * 3.1f, lineHeight = style.fontSize * 3f),
                    )
                    Spacer(Modifier.width(8.dp))
                    ProseText(paragraph.drop(1), textColor, style, sheen, Modifier.weight(1f))
                }
            } else {
                ProseText(paragraph, textColor, style, if (index == paragraphs.lastIndex) sheen else null, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProseText(
    text: String,
    color: Color,
    style: TextStyle,
    sheen: Float?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        style = style,
        modifier = modifier.then(
            if (sheen != null) Modifier.drawBehind {
                val w = size.width
                val x = -w + (w * 2f) * sheen
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, color.copy(alpha = 0.10f), Color.Transparent),
                        start = Offset(x, 0f), end = Offset(x + w * 0.6f, size.height),
                    ),
                )
            } else Modifier,
        ),
    )
}
