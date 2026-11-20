package com.textworld.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.repository.RootScreen
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun GamesScreen(vm: MainViewModel) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val state by vm.state.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("Новая история") }
    var pack by remember { mutableStateOf(state.availablePacks.firstOrNull().orEmpty()) }

    LaunchedEffect(Unit) { vm.loadGames() }
    LaunchedEffect(state.availablePacks) {
        if (pack.isBlank()) pack = state.availablePacks.firstOrNull().orEmpty()
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.friends_my_games), color = colors.text, style = typography.display)
            Spacer(Modifier.weight(1f))
            TextButtonSmall(stringResource(R.string.back)) { vm.setRoot(RootScreen.ENTRY) }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.myGames) { game ->
                Text(
                    "${game.name} · шаг ${game.stepNo}",
                    color = colors.text,
                    style = typography.prose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.openCampaign(game.id) }
                        .padding(12.dp),
                )
            }
            if (state.myGames.isEmpty()) {
                item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
            }
        }

        Spacer(Modifier.height(16.dp))
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.text, unfocusedTextColor = colors.text,
            focusedBorderColor = colors.accent, unfocusedBorderColor = colors.muted,
            focusedLabelColor = colors.accent, unfocusedLabelColor = colors.muted,
            cursorColor = colors.accent,
        )
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text(stringResource(R.string.friends_new_game)) },
            singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text("Мир: ${pack.ifBlank { "—" }}", color = colors.muted, style = typography.caption)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.createGame(pack, name) },
            enabled = pack.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg),
        ) { Text(stringResource(R.string.friends_new_game)) }
    }
}

@Composable
private fun TextButtonSmall(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
    }
}
