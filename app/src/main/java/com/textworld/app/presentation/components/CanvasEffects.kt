package com.textworld.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.textworld.app.presentation.renderer.RenderState
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural, deterministic backdrop. The same illustration seed always yields
 * the same picture. No bitmaps, no rules, no world state.
 */
@Composable
fun AmbientBackdrop(render: RenderState) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    val breathe by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3_600, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe",
    )
    val t = if (render.ambientMotion) drift else 0.25f
    val b = if (render.ambientMotion) breathe else 0.5f
    val seed = render.illustrationSeed

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(render.bgAlt, render.bg)))
        drawSceneGlow(seed, render, b)
        if (render.parallaxFar != "none") drawFarLayer(seed, render.parallaxFar, render, t)
        if (render.parallaxMid != "none") drawMidLayer(seed, render.parallaxMid, render, t)
        if (render.parallaxNear != "none") drawNearLayer(seed, render.parallaxNear, render, t)
        drawParticles(seed, render, t)
        drawVignette(render, b)
        if (render.desaturation > 0f) {
            drawRect(color = Color.Gray.copy(alpha = render.desaturation * 0.18f))
        }
    }
}

private fun DrawScope.drawSceneGlow(seed: Long, render: RenderState, b: Float) {
    val rnd = Random(seed xor 0x5EEDL)
    val x = size.width * (0.35f + rnd.nextFloat() * 0.3f)
    val y = size.height * (0.22f + rnd.nextFloat() * 0.16f)
    val radius = size.minDimension * (0.55f + b * 0.08f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(render.accent.copy(alpha = 0.10f + b * 0.05f), Color.Transparent),
            center = Offset(x, y),
            radius = radius,
        ),
        radius = radius,
        center = Offset(x, y),
    )
}

private fun DrawScope.drawFarLayer(seed: Long, kind: String, render: RenderState, t: Float) {
    val rnd = Random(seed xor 0xA11CEL)
    val shift = -size.width * 0.02f * t
    val baseY = size.height * 0.55f
    val path = Path().apply { moveTo(0f, size.height) }
    var x = -size.width * 0.1f
    while (x < size.width * 1.2f) {
        val w = size.width * (0.12f + rnd.nextFloat() * 0.14f)
        val h = size.height * (0.08f + rnd.nextFloat() * (if (kind == "mountains") 0.22f else 0.10f))
        path.lineTo(x + shift, baseY)
        path.lineTo(x + w / 2 + shift, baseY - h)
        path.lineTo(x + w + shift, baseY)
        x += w
    }
    path.lineTo(size.width, size.height)
    path.close()
    drawPath(path, color = render.bgAlt.copy(alpha = 0.85f))
}

private fun DrawScope.drawMidLayer(seed: Long, kind: String, render: RenderState, t: Float) {
    val rnd = Random(seed xor 0xB00CL)
    val baseY = size.height * 0.7f
    val shift = -size.width * 0.045f * t
    repeat(26) { i ->
        val x = (i / 25f) * size.width * 1.15f + shift
        val h = size.height * (0.08f + rnd.nextFloat() * 0.12f)
        val w = 2f + rnd.nextFloat() * 5f
        drawRect(
            color = render.bg.copy(alpha = 0.9f),
            topLeft = Offset(x, baseY - h),
            size = Size(w, h),
        )
        drawCircle(
            color = render.bg.copy(alpha = 0.85f),
            radius = h * (0.3f + rnd.nextFloat() * 0.18f),
            center = Offset(x + w / 2, baseY - h),
        )
    }
}

private fun DrawScope.drawNearLayer(seed: Long, kind: String, render: RenderState, t: Float) {
    val rnd = Random(seed xor 0x6EA5L)
    val shift = -size.width * 0.08f * t
    repeat(48) { i ->
        val x = (i / 47f) * size.width * 1.2f + shift
        val y = size.height * (0.86f + rnd.nextFloat() * 0.14f)
        val h = 10f + rnd.nextFloat() * 26f
        drawLine(
            color = render.accent.copy(alpha = 0.10f + rnd.nextFloat() * 0.10f),
            start = Offset(x, y),
            end = Offset(x + (rnd.nextFloat() - 0.5f) * 18f, y - h),
            strokeWidth = 1.2f,
        )
    }
}

private fun DrawScope.drawParticles(seed: Long, render: RenderState, t: Float) {
    val p = render.particles
    if (p.type == "none" || p.density <= 0f) return
    val count = (p.density * 90f).toInt().coerceAtMost(140)
    val color = when (p.type) {
        "ember" -> Color(0xFFE08A2C)
        "snow" -> Color(0xFFDCE8F2)
        "rain" -> Color(0xFF93A9BC)
        "motes", "dust" -> render.accent
        else -> render.text
    }
    repeat(count) { i ->
        val rnd = Random(seed xor (i.toLong() * 0x9E3779B9L))
        val px = rnd.nextFloat()
        val py0 = rnd.nextFloat()
        val sizePx = 1f + rnd.nextFloat() * 2.6f
        val speed = (0.3f + rnd.nextFloat()) * (0.4f + p.speed)
        val phase = rnd.nextFloat() * 6.28f
        val travel = ((py0 + t * speed) % 1f)
        val x = size.width * (px + sin(phase + t * 6.28f) * 0.01f)
        val y = size.height * travel
        when (p.type) {
            "rain" -> drawLine(
                color = color.copy(alpha = 0.18f),
                start = Offset(x, y), end = Offset(x - 4f, y + 18f), strokeWidth = 1f,
            )
            "ember" -> {
                val glow = 0.25f + 0.25f * sin(phase * 3f + t * 12f)
                drawCircle(color = color.copy(alpha = glow.coerceIn(0.05f, 0.6f)), radius = sizePx * 2.4f, center = Offset(x, y))
                drawCircle(color = color.copy(alpha = 0.85f), radius = sizePx, center = Offset(x, y))
            }
            else -> drawCircle(
                color = color.copy(alpha = 0.12f + 0.25f * (0.5f + 0.5f * cos(phase))),
                radius = sizePx, center = Offset(x, y),
            )
        }
    }
}

private fun DrawScope.drawVignette(render: RenderState, b: Float) {
    val alpha = render.vignetteAlpha * (0.9f + b * 0.1f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, render.vignette.copy(alpha = alpha)),
            center = Offset(size.width / 2, size.height / 2),
            radius = size.maxDimension * 0.75f,
        ),
    )
}
