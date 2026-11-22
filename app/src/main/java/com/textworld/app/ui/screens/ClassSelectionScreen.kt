package com.textworld.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.ClassEntry
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/**
 * First-kill class selection. Offers are data from the pack; selected class
 * instantly applies attributes, skills, gear, perks and equipment server-side.
 * A single overlay, no nested dialogs.
 */
@Composable
fun ClassSelectionScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val offers = state.campaign?.classSelection?.offers ?: emptyList()
    var selectedId by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf<String?>(null) }
    val golden = Color(0xFFD8B24C)

    val categories = offers.map { it.categoryName.ifBlank { it.category } }.distinct()

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colors.bgAlt, RoundedCornerShape(10.dp))
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(stringResource(R.string.class_title), color = golden, style = typography.display)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.class_intro, offers.size),
                color = colors.muted,
                style = typography.caption,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { filter = null }) {
                    Text(
                        stringResource(R.string.class_all),
                        color = if (filter == null) golden else colors.muted,
                        style = typography.caption,
                    )
                }
                categories.forEach { cat ->
                    TextButton(onClick = { filter = cat }) {
                        Text(
                            cat,
                            color = if (filter == cat) golden else colors.muted,
                            style = typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            val shown = offers.filter { filter == null || (it.categoryName.ifBlank { it.category }) == filter }
            shown.forEach { entry ->
                val isChosen = selectedId == entry.id
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (isChosen) golden.copy(alpha = 0.10f) else colors.bg.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.name, color = if (isChosen) golden else colors.text, style = typography.prose, modifier = Modifier.weight(1f))
                        Text(entry.categoryName.ifBlank { entry.category }, color = colors.muted, style = typography.caption)
                    }
                    Text(entry.role, color = colors.muted, style = typography.caption, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(entry.description, color = colors.text, style = typography.prose, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatAttributes(entry.attributes, colors.muted),
                        color = colors.muted,
                        style = typography.caption,
                    )
                    if (entry.skills.isNotEmpty()) {
                        Text(
                            entry.skills.take(4).joinToString(" · ") { s -> "${s.name} ${s.rank}" + if (s.bonus != 0) " (${s.bonus})" else "" },
                            color = colors.muted,
                            style = typography.caption,
                        )
                    }
                    if (entry.perks.isNotEmpty()) {
                        Text(
                            entry.perks.joinToString(" · ") { p -> p.name },
                            color = colors.accent,
                            style = typography.caption,
                        )
                    }
                    if (entry.gear.isNotEmpty()) {
                        Text(
                            "Снаряжение: " + entry.gear.joinToString(", ") { g -> "«${g.item}» ×${g.qty}" },
                            color = colors.muted,
                            style = typography.caption,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { selectedId = entry.id },
                        enabled = !isChosen,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isChosen) colors.muted else golden,
                            contentColor = colors.bg,
                        ),
                    ) { Text(stringResource(R.string.class_pick)) }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { selectedId?.let { vm.chooseClass(it) } },
                enabled = selectedId != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = golden, contentColor = colors.bg),
            ) { Text(stringResource(R.string.class_confirm)) }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.class_hint),
                color = colors.muted,
                style = typography.caption,
            )
        }
    }
}

private fun formatAttributes(attributes: Map<String, Int>, muted: Color): String = attributes
    .filter { it.value != 0 }
    .entries
    .joinToString(" · ") { (k, v) -> "${k.uppercase()} ${if (v > 0) "+" else ""}${v}" }
    .ifBlank { "Без особых бонусов" }
