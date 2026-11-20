package com.textworld.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.repository.Overlay
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun OverlayHost(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    when {
        state.bootError -> BookClosed(vm)
        state.updateRequired || state.overlay == Overlay.UPDATE -> UpdateRequired(vm)
        state.overlay == Overlay.SETTINGS -> Scrim { SettingsScreen(vm) }
        state.overlay == Overlay.ABOUT -> Scrim { AboutScreen(vm) }
        state.overlay == Overlay.LEVEL_UP -> LevelUpScreen(vm)
        state.overlay == Overlay.SAVES -> Scrim { SavesScreen(vm) }
    }
}

@Composable
private fun Scrim(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(com.textworld.app.presentation.theme.LocalBookColors.current.bgAlt)
                .padding(8.dp),
        ) { content() }
    }
}

@Composable
private fun BookClosed(vm: MainViewModel) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text("ᛟ", color = colors.accent.copy(alpha = 0.6f), style = typography.display)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.book_closed), color = colors.text, style = typography.display)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.book_closed_body), color = colors.muted, style = typography.caption)
        Spacer(Modifier.height(24.dp))
        androidx.compose.material3.Button(
            onClick = { vm.retryBootstrap() },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = colors.accent, contentColor = colors.bg,
            ),
        ) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun UpdateRequired(vm: MainViewModel) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text(stringResource(R.string.update_required), color = colors.accent, style = typography.display)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.update_required_body), color = colors.text, style = typography.prose, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.setOverlay(Overlay.ABOUT) },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg),
        ) { Text(stringResource(R.string.update)) }
    }
}
