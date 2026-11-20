package com.textworld.app.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.textworld.app.presentation.renderer.PresentationRenderer
import com.textworld.app.presentation.renderer.RenderState

data class BookColors(
    val bg: Color,
    val bgAlt: Color,
    val accent: Color,
    val text: Color,
    val muted: Color,
    val vignette: Color,
)

data class BookTypography(
    val prose: androidx.compose.ui.text.TextStyle,
    val proseSmall: androidx.compose.ui.text.TextStyle,
    val display: androidx.compose.ui.text.TextStyle,
    val caption: androidx.compose.ui.text.TextStyle,
    val mono: androidx.compose.ui.text.TextStyle,
)

val LocalBookColors = staticCompositionLocalOf {
    BookColors(
        bg = Color(0xFF0E0D0B), bgAlt = Color(0xFF17130F),
        accent = Color(0xFFC9A227), text = Color(0xFFE8DECC),
        muted = Color(0xFF8C8272), vignette = Color.Black,
    )
}

val LocalRenderState = staticCompositionLocalOf { PresentationRenderer.defaultRenderState() }

val LocalBookTypography = staticCompositionLocalOf {
    BookTypography(
        prose = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 29.sp),
        proseSmall = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 15.sp, lineHeight = 25.sp),
        display = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 30.sp),
        caption = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 13.sp),
        mono = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
    )
}

/**
 * Warm book skin. Two visual languages stay separate: screens in the BOOK use
 * these colors and serif type; SYSTEM WINDOW screens use [SystemWindow].
 */
@Composable
fun BookTheme(render: RenderState, fontScale: Float = 1f, content: @Composable () -> Unit) {
    val spec = tween<Color>(render.transitionMs)
    val bg by animateColorAsState(render.bg, spec, label = "bg")
    val bgAlt by animateColorAsState(render.bgAlt, spec, label = "bgAlt")
    val accent by animateColorAsState(render.accent, spec, label = "accent")
    val text by animateColorAsState(render.text, spec, label = "text")
    val muted by animateColorAsState(render.muted, spec, label = "muted")
    val vignette by animateColorAsState(render.vignette, spec, label = "vignette")

    val scale = fontScale.coerceIn(0.8f, 2f)
    val typography = BookTypography(
        prose = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Serif, fontSize = 17.sp * scale, lineHeight = 29.sp * scale,
        ),
        proseSmall = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Serif, fontSize = 15.sp * scale, lineHeight = 25.sp * scale,
        ),
        display = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp * scale,
        ),
        caption = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 13.sp * scale,
        ),
        mono = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace, fontSize = 14.sp * scale,
        ),
    )

    val colors = BookColors(bg, bgAlt, accent, text, muted, vignette)

    CompositionLocalProvider(
        LocalBookColors provides colors,
        LocalRenderState provides render,
        LocalBookTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = bg,
                surface = bgAlt,
                primary = accent,
                onPrimary = bg,
                onBackground = text,
                onSurface = text,
                secondary = muted,
                error = Color(0xFFB45A4A),
            ),
            content = content,
        )
    }
}

@Composable
fun isDarkBook(): Boolean = isSystemInDarkTheme()
