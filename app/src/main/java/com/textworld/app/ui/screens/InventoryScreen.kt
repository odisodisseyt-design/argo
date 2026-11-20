package com.textworld.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.Item
import com.textworld.app.data.model.inventoryAsModels
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

@Composable
fun InventoryScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val character = state.campaign?.character
    val items = character?.inventoryAsModels().orEmpty()

    Column(screenModifier()) {
        ScreenTitle(stringResource(R.string.nav_inventory))
        Spacer(Modifier.height(6.dp))
        character?.derived?.get("carry_kg")?.let {
            Text(stringResource(R.string.inventory_weight, it.toString()), color = colors.muted, style = typography.caption)
        }
        Text(
            stringResource(R.string.inventory_slots, items.size),
            color = colors.muted,
            style = typography.caption,
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            items(items) { item -> ItemRow(item) }
            if (items.isEmpty()) {
                item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
            }
        }
    }
}

@Composable
private fun ItemRow(item: Item) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val rarityColor = when (item.rarity.lowercase()) {
        "rare" -> androidx.compose.ui.graphics.Color(0xFF6FA8D6)
        "epic" -> androidx.compose.ui.graphics.Color(0xFFB07FD6)
        "legendary" -> androidx.compose.ui.graphics.Color(0xFFD8A63C)
        else -> colors.text
    }
    Column(
        Modifier.fillMaxWidth().padding(12.dp),
    ) {
        Row {
            Text(item.name.ifBlank { "—" }, color = rarityColor, style = typography.prose, modifier = Modifier.weight(1f))
            if (item.quantity > 1) Text("×${item.quantity}", color = colors.muted, style = typography.caption)
        }
        if (item.description.isNotBlank()) Text(item.description, color = colors.muted, style = typography.caption)
        Row {
            if (item.weight > 0f) Text("${item.weight} кг", color = colors.muted, style = typography.caption)
            Spacer(Modifier.weight(1f))
            if (item.durability != null) {
                Text(stringResource(R.string.inventory_durability), color = colors.muted, style = typography.caption)
            }
        }
    }
}
