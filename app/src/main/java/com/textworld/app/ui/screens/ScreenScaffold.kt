package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography

@Composable
fun screenModifier(): Modifier = Modifier
    .fillMaxSize()
    .windowInsetsPadding(
        androidx.compose.foundation.layout.WindowInsets.systemBars.only(
            androidx.compose.foundation.layout.WindowInsetsSides.Top +
                androidx.compose.foundation.layout.WindowInsetsSides.Horizontal,
        ),
    )
    .padding(horizontal = 20.dp, vertical = 12.dp)

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = LocalBookColors.current.text,
        style = LocalBookTypography.current.display,
        modifier = modifier,
    )
}
