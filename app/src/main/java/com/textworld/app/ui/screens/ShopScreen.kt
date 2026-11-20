package com.textworld.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.model.Item
import com.textworld.app.presentation.components.SystemValue
import com.textworld.app.presentation.components.SystemWindow
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.MainViewModel

/** COLD System Window. Tabs and goods come only from the pack + server. */
@Composable
fun ShopScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val tabs = state.manifest.shopTabs
    var selected by remember { mutableStateOf(tabs.firstOrNull().orEmpty()) }
    var preview by remember { mutableStateOf<Item?>(null) }
    val items = state.campaign?.shopItems.orEmpty().filter {
        selected.isEmpty() || it.type.equals(selected, true) || it.slot.equals(selected, true)
    }

    Column(screenModifier()) {
        SystemWindow(title = stringResource(R.string.shop_title), modifier = Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth()) {
                tabs.forEach { tab ->
                    androidx.compose.material3.FilterChip(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        label = { Text(tabLabel(tab), style = typography.caption) },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn {
                items(items) { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, color = colors.text, style = typography.proseSmall)
                            PriceLine(item)
                        }
                        Button(onClick = { preview = item }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.bg)) {
                            Text(stringResource(R.string.shop_preview))
                        }
                    }
                }
                if (items.isEmpty()) {
                    item { Text(stringResource(R.string.empty), color = colors.muted, style = typography.caption) }
                }
            }
        }
    }

    preview?.let { item ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { preview = null },
            confirmButton = {
                TextButtonLocal(stringResource(R.string.shop_buy)) {
                    vm.submit("купить ${item.name}")
                    preview = null
                }
            },
            dismissButton = { TextButtonLocal(stringResource(R.string.cancel)) { preview = null } },
            title = { Text(item.name, color = colors.text) },
            text = {
                Column {
                    if (item.description.isNotBlank()) Text(item.description, color = colors.muted, style = typography.proseSmall)
                    PriceLine(item)
                    item.stock?.let { Text(stringResource(R.string.shop_stock, it), color = colors.muted, style = typography.caption) }
                    if (state.manifest.showOdds && item.properties["odds"] != null) {
                        Text(stringResource(R.string.shop_odds, item.properties["odds"].toString()), color = colors.muted, style = typography.caption)
                    }
                    item.properties["pity"]?.let {
                        Text(stringResource(R.string.shop_pity, it.toString().toIntOrNull() ?: 0), color = colors.muted, style = typography.caption)
                    }
                }
            },
        )
    }
}

@Composable
private fun tabLabel(tab: String) = when (tab.lowercase()) {
    "consumables" -> stringResource(R.string.shop_tab_consumables)
    "equipment" -> stringResource(R.string.shop_tab_equipment)
    "materials" -> stringResource(R.string.shop_tab_materials)
    "skills" -> stringResource(R.string.shop_tab_skills)
    "stats" -> stringResource(R.string.shop_tab_stats)
    "services" -> stringResource(R.string.shop_tab_services)
    "unique" -> stringResource(R.string.shop_tab_unique)
    else -> tab
}

@Composable
private fun PriceLine(item: Item) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Row {
        item.costBase?.takeIf { item.costNext != null && it != item.costNext }?.let {
            Text(it.toString(), color = colors.muted, style = typography.caption.copy(textDecoration = TextDecoration.LineThrough))
        }
        SystemValue(if (item.costNext != null) item.costNext.toString() else item.price.toString())
        if (item.currency.isNotBlank()) Text(item.currency, color = colors.muted, style = typography.caption)
        item.discountReason?.let {
            Text(stringResource(R.string.levelup_discount, it), color = colors.muted, style = typography.caption)
        }
    }
}

@Composable
private fun TextButtonLocal(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) { Text(text) }
}
