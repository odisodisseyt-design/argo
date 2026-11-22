package com.textworld.app.data.repository

import com.textworld.app.data.model.CampaignState
import com.textworld.app.data.model.HealthResponse
import com.textworld.app.data.model.JournalEntry
import com.textworld.app.data.model.PendingDetail
import com.textworld.app.data.model.Presentation
import com.textworld.app.data.model.UiManifest

enum class RootScreen { ENTRY, LOGIN, GAMES, GAME }
enum class Overlay { NONE, SETTINGS, ABOUT, UPDATE, LEVEL_UP, FRIENDS, SAVES, CLASS_SELECTION }

data class AppState(
    val root: RootScreen = RootScreen.ENTRY,
    val overlay: Overlay = Overlay.NONE,
    val health: HealthResponse? = null,
    val bootError: Boolean = false,
    val updateRequired: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val myUserId: Int = 0,
    val campaign: CampaignState? = null,
    val manifest: UiManifest = UiManifest(),
    val presentation: Presentation = Presentation(),
    val prose: String = "",
    val outcome: String = "",
    val streaming: Boolean = false,
    val waitingFor: List<Int> = emptyList(),
    val waitingName: String? = null,
    val frozenStep: Int? = null,
    val pendingDetails: List<PendingDetail> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    val inWorldMessage: String? = null,
    val slipTrigger: Int = 0,
    val pageTurnTrigger: Int = 0,
    val rewindTrigger: Int = 0,
    val dead: Boolean = false,
    val levelUpAvailable: Boolean = false,
    val myGames: List<CampaignState> = emptyList(),
    val friends: List<com.textworld.app.data.model.Friend> = emptyList(),
    val onlineUsers: List<com.textworld.app.data.model.PublicUser> = emptyList(),
    val invites: List<com.textworld.app.data.model.Invite> = emptyList(),
    val availablePacks: List<String> = emptyList(),
    val living: com.textworld.app.data.model.LivingWorld = com.textworld.app.data.model.LivingWorld(),
    val classSelectionPending: Boolean = false,
    val era: String = "E0",
    val eraName: String = "",
)
