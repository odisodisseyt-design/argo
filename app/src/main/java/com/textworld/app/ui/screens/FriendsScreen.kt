package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun FriendsScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    var username by remember { mutableStateOf("") }
    val campaignId = state.campaign?.id.orEmpty()

    LaunchedEffect(Unit) { vm.loadSocial() }

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.friends_title))
        Spacer(Modifier.height(10.dp))

        if (state.invites.isNotEmpty()) {
            Text(stringResource(R.string.friends_invites), color = colors.accent, style = typography.caption)
            state.invites.forEach { invite ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        "${invite.inviter} · ${invite.campaignName}",
                        color = colors.text,
                        style = typography.proseSmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.acceptInvite(invite.id) }) {
                        Text(stringResource(R.string.friends_join), color = colors.accent)
                    }
                    TextButton(onClick = { vm.dismissMessage() }) {
                        Text(stringResource(R.string.friends_decline), color = colors.muted)
                    }
                }
            }
        }

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.text, unfocusedTextColor = colors.text,
            focusedBorderColor = colors.accent, unfocusedBorderColor = colors.muted,
            focusedLabelColor = colors.accent, unfocusedLabelColor = colors.muted,
            cursorColor = colors.accent,
        )
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text(stringResource(R.string.login_username)) },
            singleLine = true, colors = fieldColors, modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { vm.friendRequest(username); username = "" },
            enabled = username.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg),
        ) { Text(stringResource(R.string.friends_invite)) }
        Spacer(Modifier.height(12.dp))

        Text(stringResource(R.string.friends_my), color = colors.accent, style = typography.caption)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(state.friends) { friend ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("●", color = if (friend.online) colors.accent else colors.muted, style = typography.caption)
                    Spacer(Modifier.padding(4.dp))
                    Text(friend.username, color = colors.text, style = typography.proseSmall, modifier = Modifier.weight(1f))
                }
            }
            item {
                Text(stringResource(R.string.friends_online), color = colors.accent, style = typography.caption)
            }
            items(state.onlineUsers) { user ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(user.username, color = colors.text, style = typography.proseSmall, modifier = Modifier.weight(1f))
                    if (campaignId.isNotBlank()) {
                        TextButton(onClick = { vm.inviteToGame(campaignId, user.id) }) {
                            Text(stringResource(R.string.friends_invite), color = colors.accent)
                        }
                    }
                }
            }
        }
    }
}
