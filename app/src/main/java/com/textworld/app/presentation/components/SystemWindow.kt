package com.textworld.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.presentation.theme.LocalRenderState
import com.textworld.app.data.model.SystemTheme

private data class SystemSkin(val accent: Color, val bg: Color, val line: Color)

@Composable
private fun skinFor(theme: String): SystemSkin = when (theme) {
    SystemTheme.RUNIC -> SystemSkin(Color(0xFFD8C7A1), Color(0xFF0B0C0A), Color(0x33D8C7A1))
    SystemTheme.SYMBIOTE -> SystemSkin(Color(0xFF8CE0A8), Color(0xFF060B08), Color(0x338CE0A8))
    else -> SystemSkin(Color(0xFF7FE3FF), Color(0xFF05090C), Color(0x337FE3FF))
}

/**
 * COLD visual language: geometric, thin lines, monospaced numerals, slow scan.
 * It deliberately contrasts with the warm BOOK screens. The three server themes
 * only swap colors; geometry stays consistent.
 */
@Composable
fun SystemWindow(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val render = LocalRenderState.current
    val skin = skinFor(render.systemTheme)
    val typography = LocalBookTypography.current
    val transition = rememberInfiniteTransition(label = "scan")
    val scan by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9_000, easing = LinearEasing), RepeatMode.Restart),
        label = "scanline",
    )
    val scanY = if (render.ambientMotion) scan else 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, skin.line)
            .drawBehind { drawRect(skin.bg.copy(alpha = 0.72f)) },
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "▣ $title",
                color = skin.accent,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                style = typography.mono,
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .drawBehind { drawLine(skin.line, Offset(0f, 0f), Offset(size.width, 0f), 1f) })
            content()
        }
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                color = skin.accent.copy(alpha = 0.10f),
                start = Offset(0f, size.height * scanY),
                end = Offset(size.width, size.height * scanY),
                strokeWidth = 1.5f,
            )
        }
    }
}

@Composable
fun SystemValue(text: String, color: Color? = null) {
    val render = LocalRenderState.current
    val skin = skinFor(render.systemTheme)
    Text(
        text = text,
        color = color ?: skin.accent,
        fontFamily = FontFamily.Monospace,
        fontSize = 18.sp,
        style = LocalBookTypography.current.mono,
    )
}
