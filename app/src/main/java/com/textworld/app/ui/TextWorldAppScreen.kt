package com.textworld.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.textworld.app.core.Outcome
import com.textworld.app.data.repository.RootScreen
import com.textworld.app.di.ServiceLocator
import com.textworld.app.presentation.components.AmbientBackdrop
import com.textworld.app.presentation.components.DeathVeil
import com.textworld.app.presentation.components.PageTurnOverlay
import com.textworld.app.presentation.components.PaperSlip
import com.textworld.app.presentation.components.RewindEffect
import com.textworld.app.presentation.theme.BookTheme
import com.textworld.app.ui.screens.EntryScreen
import com.textworld.app.ui.screens.GameShell
import com.textworld.app.ui.screens.GamesScreen
import com.textworld.app.ui.screens.LoginScreen
import com.textworld.app.ui.screens.OverlayHost

@Composable
fun TextWorldAppScreen(locator: ServiceLocator) {
    val vm: MainViewModel = viewModel(factory = MainViewModel.Factory(locator))
    val state by vm.state.collectAsStateWithLifecycle()
    val render by vm.renderState.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.inWorldMessage) {
        state.inWorldMessage?.let {
            snackbar.showSnackbar(it)
            vm.dismissMessage()
        }
    }

    BookTheme(render = render, fontScale = settings.fontScale) {
        Box(Modifier.fillMaxSize()) {
            AmbientBackdrop(render)

            when (state.root) {
                RootScreen.ENTRY -> EntryScreen(
                    onOpen = { vm.openBook() },
                    onLongPressCover = { vm.setOverlay(com.textworld.app.data.repository.Overlay.SETTINGS) },
                )
                RootScreen.LOGIN -> LoginScreen(
                    onLogin = { u, p -> vm.login(u, p) },
                    onRegister = { u, p -> vm.register(u, p) },
                    onBack = { vm.setRoot(RootScreen.ENTRY) },
                )
                RootScreen.GAMES -> GamesScreen(vm = vm)
                RootScreen.GAME -> GameShell(vm = vm)
            }

            OverlayHost(vm = vm)
            PaperSlip(state.slipTrigger)
            PageTurnOverlay(state.pageTurnTrigger)
            RewindEffect(state.rewindTrigger)
            DeathVeil(
                visible = state.dead,
                onRewind = { vm.dismissMessage() },
                onBranch = { vm.setOverlay(com.textworld.app.data.repository.Overlay.SAVES) },
            )

            SnackbarHost(snackbar, modifier = Modifier.fillMaxSize())
        }
    }
}
