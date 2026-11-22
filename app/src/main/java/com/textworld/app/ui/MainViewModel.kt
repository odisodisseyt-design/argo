package com.textworld.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.textworld.app.R
import com.textworld.app.core.Outcome
import com.textworld.app.data.model.Feature
import com.textworld.app.data.repository.GameRepository
import com.textworld.app.data.repository.Overlay
import com.textworld.app.data.repository.RootScreen
import com.textworld.app.data.settings.AppSettings
import com.textworld.app.data.settings.SettingsStore
import com.textworld.app.di.ServiceLocator
import com.textworld.app.presentation.renderer.RenderState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GameTab {
    SCENE, AROUND, BOOK, INVENTORY, SHOP, QUESTS, MAP, CHARACTERS, JOURNAL, FRIENDS, SAVES;

    fun labelRes() = when (this) {
        SCENE -> R.string.nav_scene
        AROUND -> R.string.nav_around
        BOOK -> R.string.nav_book
        INVENTORY -> R.string.nav_inventory
        SHOP -> R.string.nav_shop
        QUESTS -> R.string.nav_quests
        MAP -> R.string.nav_map
        CHARACTERS -> R.string.nav_characters
        JOURNAL -> R.string.nav_journal
        FRIENDS -> R.string.nav_friends
        SAVES -> R.string.nav_saves
    }

    fun rune() = when (this) {
        SCENE -> "✦"
        AROUND -> "◎"
        BOOK -> "▤"
        INVENTORY -> "▣"
        SHOP -> "⌘"
        QUESTS -> "⚑"
        MAP -> "◇"
        CHARACTERS -> "♙"
        JOURNAL -> "❧"
        FRIENDS -> "♧"
        SAVES -> "ᛉ"
    }
}

/** Rebuilds navigation from ui_manifest: false features create no destination. */
fun tabsFor(manifest: com.textworld.app.data.model.UiManifest): List<GameTab> = buildList {
    add(GameTab.SCENE)
    add(GameTab.AROUND)
    if (manifest.enabled(Feature.STATS) || manifest.enabled(Feature.SKILLS) ||
        manifest.enabled(Feature.EFFECTS) || manifest.enabled(Feature.CURRENCY) ||
        manifest.enabled(Feature.LEVEL_UP)
    ) add(GameTab.BOOK)
    if (manifest.enabled(Feature.INVENTORY)) add(GameTab.INVENTORY)
    if (manifest.enabled(Feature.SHOP)) add(GameTab.SHOP)
    if (manifest.enabled(Feature.QUESTS)) add(GameTab.QUESTS)
    if (manifest.enabled(Feature.MAP)) add(GameTab.MAP)
    if (manifest.enabled(Feature.CHARACTERS)) add(GameTab.CHARACTERS)
    if (manifest.enabled(Feature.JOURNAL)) add(GameTab.JOURNAL)
    add(GameTab.FRIENDS)
    add(GameTab.SAVES)
}

class MainViewModel(private val locator: ServiceLocator) : ViewModel() {
    val repository: GameRepository = locator.repository
    val state = repository.state
    val renderState: StateFlow<RenderState> = locator.renderer.state
    val settings: StateFlow<AppSettings> = locator.settingsStore.settings
    val updateRepository = locator.updateRepository
    val availablePacks get() = state.value.availablePacks

    private var realtimeJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch { repository.bootstrap() }
    }

    val tabs: List<GameTab> get() = tabsFor(state.value.manifest)

    fun openBook() {
        if (state.value.isLoggedIn) {
            if (state.value.campaign != null) {
                repository.setRoot(RootScreen.GAME)
            } else {
                repository.setRoot(RootScreen.GAMES)
            }
        } else {
            repository.setRoot(RootScreen.LOGIN)
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        val result = repository.login(username, password)
        if (result is Outcome.Success) repository.setRoot(RootScreen.GAMES)
    }

    fun register(username: String, password: String) = viewModelScope.launch {
        val result = repository.register(username, password)
        if (result is Outcome.Success) repository.setRoot(RootScreen.GAMES)
    }

    fun retryBootstrap() = viewModelScope.launch { repository.bootstrap() }

    fun loadGames() = viewModelScope.launch { repository.loadGames() }
    fun loadSocial() = viewModelScope.launch { repository.loadSocial() }
    fun friendRequest(username: String) = viewModelScope.launch { repository.friendRequest(username) }
    fun friendAccept(id: Int) = viewModelScope.launch { repository.friendAccept(id) }
    fun inviteToGame(campaignId: String, userId: Int) = viewModelScope.launch { repository.inviteToGame(campaignId, userId) }
    fun acceptInvite(id: Int) = viewModelScope.launch { repository.acceptInvite(id) }

    fun createGame(packId: String, name: String, invitees: List<Int> = emptyList()) = viewModelScope.launch {
        repository.createGame(packId, name, invitees)
        startRealtime()
    }

    fun openCampaign(id: String) = viewModelScope.launch {
        val result = repository.openCampaign(id)
        if (result is Outcome.Success) startRealtime()
    }

    private fun startRealtime() {
        realtimeJob?.cancel()
        val id = state.value.campaign?.id ?: return
        realtimeJob = viewModelScope.launch {
            repository.realtimeEvents(id).collect { repository.handleRealtime(it) }
        }
    }

    fun submit(text: String) = viewModelScope.launch { repository.submit(text) }
    fun chooseClass(classId: String) = viewModelScope.launch {
        val result = repository.chooseClass(classId)
        if (result is Outcome.Success) repository.refreshClasses()
    }
    fun allocate(allocations: List<Pair<String, Int>>) = viewModelScope.launch { repository.allocate(allocations) }
    fun acceptDetail(id: Int, accept: Boolean) = viewModelScope.launch { repository.acceptDetail(id, accept) }
    fun rewind() = viewModelScope.launch { repository.branchAction("rewind") }
    fun saveBranch() = viewModelScope.launch { repository.branchAction("save") }
    fun switchBranch(id: String) = viewModelScope.launch { repository.branchAction("switch", id) }
    fun dismissMessage() = repository.clearMessage()
    fun setOverlay(overlay: Overlay) = repository.setOverlay(overlay)
    fun setRoot(root: RootScreen) = repository.setRoot(root)

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        locator.settingsStore.update(transform)
        repository.applySettings()
        viewModelScope.launch { repository.syncSettings() }
    }

    suspend fun checkUpdate() = locator.updateRepository.check()

    class Factory(private val locator: ServiceLocator) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(locator) as T
    }
}
