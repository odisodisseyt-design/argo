package com.textworld.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.textworld.app.BuildConfig
import com.textworld.app.R
import com.textworld.app.core.Outcome
import com.textworld.app.data.model.UpdateInfo
import com.textworld.app.data.repository.Overlay
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(vm: MainViewModel) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var progress by remember { mutableStateOf(0f) }
    var downloadMb by remember { mutableStateOf(0.0) }
    var totalMb by remember { mutableStateOf(0.0) }
    val noUpdatesText = stringResource(R.string.update_not_available)
    val checkFailedText = stringResource(R.string.update_check_failed)

    Column(Modifier.padding(24.dp)) {
        Row {
            ScreenTitle(stringResource(R.string.about_title), modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.setOverlay(Overlay.NONE) }) { Text(stringResource(R.string.close), color = colors.muted) }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            color = colors.text,
            style = typography.prose,
        )
        Text(stringResource(R.string.about_licence), color = colors.muted, style = typography.caption)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                scope.launch {
                    status = ""
                    when (val result = vm.checkUpdate()) {
                        is Outcome.Success -> {
                            val available = result.value.versionCode > BuildConfig.VERSION_CODE
                            info = result.value.takeIf { available }
                            status = if (available) "" else noUpdatesText
                        }
                        is Outcome.Failure -> status = checkFailedText
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg),
        ) { Text(stringResource(R.string.check_for_updates)) }
        Spacer(Modifier.height(8.dp))
        Text(status, color = colors.muted, style = typography.caption)
    }

    info?.let { update ->
        AlertDialog(
            onDismissRequest = { info = null },
            title = { Text(stringResource(R.string.update_dialog_title), color = colors.text) },
            text = {
                Column {
                    update.changelog.forEach { Text("• $it", color = colors.text, style = typography.proseSmall) }
                    Spacer(Modifier.height(8.dp))
                    if (downloadMb > 0) {
                        Text(
                            String.format(java.util.Locale.forLanguageTag("ru"), "%.1f / %.1f МБ", downloadMb, totalMb),
                            color = colors.muted,
                            style = typography.caption,
                        )
                    }
                    if (progress > 0f) LinearProgressIndicator(progress = { progress })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        when (val result = vm.updateRepository.download(update) { downloaded, total ->
                            if (total > 0) {
                                progress = downloaded.toFloat() / total
                                downloadMb = downloaded / 1_048_576.0
                                totalMb = total / 1_048_576.0
                            }
                        }) {
                            is Outcome.Success -> {
                                if (!vm.updateRepository.canInstall()) {
                                    context.startActivity(vm.updateRepository.settingsIntent())
                                    Toast.makeText(context, R.string.update_install_permission, Toast.LENGTH_LONG).show()
                                } else {
                                    context.startActivity(vm.updateRepository.installIntent(result.value))
                                }
                            }
                            is Outcome.Failure -> status = result.message
                        }
                    }
                }) { Text(stringResource(R.string.update), color = colors.accent) }
            },
            dismissButton = { TextButton(onClick = { info = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
