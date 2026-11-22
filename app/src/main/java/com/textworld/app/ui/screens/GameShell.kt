package com.textworld.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.textworld.app.R
import com.textworld.app.data.repository.Overlay
import com.textworld.app.presentation.theme.LocalBookColors
import com.textworld.app.presentation.theme.LocalBookTypography
import com.textworld.app.ui.GameTab
import com.textworld.app.ui.MainViewModel
import com.textworld.app.ui.tabsFor
import kotlinx.coroutines.launch

/** Navigation is rebuilt from ui_manifest on every pack switch. */
@Composable
fun GameShell(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    val tabs = remember(state.manifest) { tabsFor(state.manifest) }
    var selected by rememberSaveable { mutableStateOf(GameTab.SCENE) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(tabs) {
        if (selected !in tabs) selected = GameTab.SCENE
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = colors.bgAlt) {
                Spacer(Modifier.height(28.dp))
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        state.campaign?.name?.ifBlank { stringResource(R.string.menu_title) }
                            ?: stringResource(R.string.menu_title),
                        color = colors.text,
                        style = typography.display,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.menu_subtitle),
                        color = colors.muted,
                        style = typography.caption,
                    )
                }
                Spacer(Modifier.height(12.dp))
                DrawerAction(stringResource(R.string.menu_save)) {
                    vm.saveBranch()
                    scope.launch { drawerState.close() }
                }
                DrawerAction(stringResource(R.string.menu_main)) {
                    vm.exitToMenu()
                    scope.launch { drawerState.close() }
                }
                DrawerAction(stringResource(R.string.menu_settings)) {
                    vm.setOverlay(Overlay.SETTINGS)
                    scope.launch { drawerState.close() }
                }
                Spacer(Modifier.height(8.dp))
                DrawerAction(stringResource(R.string.menu_close)) {
                    scope.launch { drawerState.close() }
                }
            }
        },
    ) {
        Row(Modifier.fillMaxSize()) {
            val useRail = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
            if (useRail) {
                NavigationRail(containerColor = colors.bgAlt.copy(alpha = 0.6f)) {
                    tabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selected == tab,
                            onClick = { selected = tab },
                            icon = { TabIcon(tab, state) },
                            label = { Text(stringResource(tab.labelRes())) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                unselectedIconColor = colors.muted,
                                selectedTextColor = colors.accent,
                                unselectedTextColor = colors.muted,
                                indicatorColor = colors.accent.copy(alpha = 0.12f),
                            ),
                        )
                    }
                }
            }

            Column(Modifier.fillMaxSize().weight(1f)) {
                GameTopBar(
                    title = state.campaign?.name.orEmpty(),
                    onMenu = { scope.launch { drawerState.open() } },
                )
                Box(Modifier.fillMaxSize().weight(1f)) {
                    when (selected) {
                        GameTab.SCENE -> SceneScreen(vm)
                        GameTab.AROUND -> AroundScreen(vm)
                        GameTab.BOOK -> BookScreen(vm)
                        GameTab.INVENTORY -> InventoryScreen(vm)
                        GameTab.SHOP -> ShopScreen(vm)
                        GameTab.QUESTS -> QuestsScreen(vm)
                        GameTab.MAP -> MapScreen(vm)
                        GameTab.CHARACTERS -> CharactersScreen(vm)
                        GameTab.JOURNAL -> JournalScreen(vm)
                        GameTab.FRIENDS -> FriendsScreen(vm)
                        GameTab.SAVES -> SavesScreen(vm)
                    }
                }
                if (!useRail) {
                    NavigationBar(containerColor = colors.bgAlt.copy(alpha = 0.92f)) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = selected == tab,
                                onClick = { selected = tab },
                                icon = { TabIcon(tab, state) },
                                label = { Text(stringResource(tab.labelRes()), style = typography.caption) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = colors.accent,
                                    unselectedIconColor = colors.muted,
                                    selectedTextColor = colors.accent,
                                    unselectedTextColor = colors.muted,
                                    indicatorColor = colors.accent.copy(alpha = 0.12f),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTopBar(title: String, onMenu: () -> Unit) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "☰",
            color = colors.accent,
            style = typography.prose,
            modifier = Modifier
                .clickable(onClick = onMenu)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
        Spacer(Modifier.weight(1f))
        if (title.isNotBlank()) {
            Text(
                title,
                color = colors.muted,
                style = typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DrawerAction(label: String, onClick: () -> Unit) {
    val colors = LocalBookColors.current
    val typography = LocalBookTypography.current
    Text(
        label,
        color = colors.text,
        style = typography.prose,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    )
}

@Composable
private fun TabIcon(tab: GameTab, state: com.textworld.app.data.repository.AppState) {
    val colors = LocalBookColors.current
    val badge = tab == GameTab.BOOK && state.levelUpAvailable &&
        state.campaign?.character?.unspentPoints?.let { it > 0 } == true
    Box {
        Text(tab.rune(), color = if (badge) colors.accent else colors.muted)
        if (badge) {
            Text(
                "●",
                color = colors.accent,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = (-2).dp),
            )
        }
    }
}
