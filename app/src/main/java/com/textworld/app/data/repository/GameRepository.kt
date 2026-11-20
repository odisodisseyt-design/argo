package com.textworld.app.data.repository

import com.textworld.app.BuildConfig
import com.textworld.app.core.Outcome
import com.textworld.app.core.getOrNull
import com.textworld.app.core.map
import com.textworld.app.data.model.CampaignState
import com.textworld.app.data.model.JournalEntry
import com.textworld.app.data.model.PendingDetail
import com.textworld.app.data.model.Presentation
import com.textworld.app.data.model.TurnResponse
import com.textworld.app.data.model.TwJson
import com.textworld.app.data.model.parsePayload
import com.textworld.app.data.network.RealtimeClient
import com.textworld.app.data.network.RealtimeEvent
import com.textworld.app.data.network.TextWorldApi
import com.textworld.app.data.network.TurnEvent
import com.textworld.app.data.network.UpdateRepository
import com.textworld.app.data.settings.SettingsStore
import com.textworld.app.data.token.SecureTokenStore
import com.textworld.app.presentation.gateways.HapticsGateway
import com.textworld.app.presentation.renderer.PresentationRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonObject

/** Owns UI state only. All decisions and world state stay on the server. */
class GameRepository(
    private val api: TextWorldApi,
    private val tokenStore: SecureTokenStore,
    private val settingsStore: SettingsStore,
    private val renderer: PresentationRenderer,
    private val realtime: RealtimeClient,
    private val haptics: HapticsGateway,
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var activeCampaignId: String?
        get() = settingsStore.activeCampaignId
        set(value) { settingsStore.activeCampaignId = value }

    init {
        com.textworld.app.data.network.ApiClient.updateBaseUrl(settingsStore.settings.value.serverUrl)
    }

    fun applySettings() {
        com.textworld.app.data.network.ApiClient.updateBaseUrl(settingsStore.settings.value.serverUrl)
        renderer.onSettings(settingsStore.settings.value)
    }

    fun setRoot(root: com.textworld.app.data.repository.RootScreen) = _state.update {
        it.copy(root = root, pageTurnTrigger = it.pageTurnTrigger + 1)
    }
    fun setOverlay(overlay: com.textworld.app.data.repository.Overlay) = _state.update { it.copy(overlay = overlay) }
    fun clearMessage() = _state.update { it.copy(inWorldMessage = null) }

    suspend fun bootstrap() {
        applySettings()
        when (val result = api.health()) {
            is Outcome.Failure -> _state.update { it.copy(bootError = true) }
            is Outcome.Success -> {
                val health = result.value
                _state.update {
                    it.copy(
                        health = health,
                        bootError = false,
                        availablePacks = health.packs,
                        updateRequired = health.minClientVersion > BuildConfig.VERSION_CODE,
                    )
                }
                if (health.minClientVersion > BuildConfig.VERSION_CODE) {
                    _state.update { it.copy(root = com.textworld.app.data.repository.RootScreen.ENTRY, overlay = com.textworld.app.data.repository.Overlay.UPDATE) }
                    return
                }
                if (tokenStore.isLoggedIn) {
                    _state.update {
                        it.copy(
                            isLoggedIn = true,
                            userName = tokenStore.userName,
                            myUserId = tokenStore.userId,
                        )
                    }
                    val id = activeCampaignId
                    if (id != null) {
                        val campaign = api.campaign(id).getOrNull()
                        if (campaign != null) adoptCampaign(campaign)
                        else _state.update { it.copy(root = com.textworld.app.data.repository.RootScreen.GAMES) }
                    } else {
                        _state.update { it.copy(root = com.textworld.app.data.repository.RootScreen.GAMES) }
                    }
                }
            }
        }
    }

    suspend fun login(username: String, password: String): Outcome<Unit> = when (
        val result = api.login(username, password)
    ) {
        is Outcome.Failure -> result
        is Outcome.Success -> {
            val body = result.value
            if (body.error != null || body.token.isBlank()) {
                Outcome.Failure(IllegalStateException("auth"), body.message ?: "Неверный логин или пароль.")
            } else {
                tokenStore.save(body.token, body.user.id, body.user.username)
                _state.update {
                    it.copy(
                        isLoggedIn = true,
                        userName = body.user.username,
                        myUserId = body.user.id,
                    )
                }
                Outcome.Success(Unit)
            }
        }
    }

    suspend fun register(username: String, password: String): Outcome<Unit> = when (
        val result = api.register(username, password)
    ) {
        is Outcome.Failure -> result
        is Outcome.Success -> {
            val body = result.value
            if (body.error != null || body.token.isBlank()) {
                Outcome.Failure(IllegalStateException("auth"), body.message ?: "Имя занято или слишком короткие данные.")
            } else {
                tokenStore.save(body.token, body.user.id, body.user.username)
                _state.update {
                    it.copy(
                        isLoggedIn = true,
                        userName = body.user.username,
                        myUserId = body.user.id,
                    )
                }
                Outcome.Success(Unit)
            }
        }
    }

    suspend fun createGame(packId: String, name: String, invitees: List<Int>): Outcome<String> = when (
        val result = api.newCampaign(packId, name, invitees)
    ) {
        is Outcome.Failure -> result
        is Outcome.Success -> {
            if (result.value.campaign_id.isBlank()) {
                Outcome.Failure(IllegalStateException("new"), result.value.message ?: "Мир не ответил. Попробуй ещё раз.")
            } else {
                openCampaign(result.value.campaign_id).map { it.id }
            }
        }
    }

    suspend fun openCampaign(id: String): Outcome<CampaignState> = when (val result = api.campaign(id)) {
        is Outcome.Failure -> result
        is Outcome.Success -> {
            activeCampaignId = id
            adoptCampaign(result.value)
            Outcome.Success(result.value)
        }
    }

    private fun adoptCampaign(campaign: CampaignState) {
        renderer.submit(campaign.presentation, campaign.uiManifest, settingsStore.settings.value)
        _state.update {
            it.copy(
                root = com.textworld.app.data.repository.RootScreen.GAME,
                campaign = campaign,
                manifest = campaign.uiManifest,
                presentation = campaign.presentation,
                waitingFor = campaign.waitingFor,
                prose = it.prose.ifBlank { campaign.lore },
                inWorldMessage = null,
            )
        }
    }

    suspend fun submit(text: String) {
        val id = activeCampaignId ?: return
        _state.update {
            it.copy(
                streaming = true,
                inWorldMessage = null,
                slipTrigger = it.slipTrigger + 1,
                prose = "",
            )
        }
        haptics.play(renderer.state.value.haptic)
        api.submitTurn(id, text).collect { event ->
            when (event) {
                is TurnEvent.Meta -> Unit
                is TurnEvent.Delta -> _state.update { it.copy(prose = it.prose + event.text) }
                is TurnEvent.Final -> applyFinal(event.response)
                is TurnEvent.Error -> _state.update {
                    it.copy(streaming = false, inWorldMessage = event.message)
                }
                is TurnEvent.Raw -> Unit
            }
        }
    }

    fun applyFinal(response: TurnResponse) {
        val current = _state.value
        val base = current.campaign
        val updated = base?.copy(
            stepNo = if (response.stepNo != 0) response.stepNo else base.stepNo,
            anchor = response.anchor ?: base.anchor,
            character = response.character ?: base.character,
            presentation = response.presentation ?: base.presentation,
        )
        updated?.let {
            renderer.submit(it.presentation, it.uiManifest, settingsStore.settings.value)
        }
        val prose = response.prose?.takeIf { it.isNotBlank() }
            ?: response.outcomeText?.takeIf { it.isNotBlank() }
            ?: current.prose
        val details = response.pendingDetails.map { detail ->
            PendingDetail(detail.id, detail.text, detail.source, detail.status)
        }
        _state.update { state ->
            state.copy(
                campaign = updated ?: state.campaign,
                presentation = response.presentation ?: state.presentation,
                prose = prose,
                outcome = response.outcomeText.orEmpty(),
                streaming = false,
                waitingFor = response.waitingFor,
                frozenStep = if (response.waitingFor.isNotEmpty()) response.stepNo else null,
                pendingDetails = if (details.isNotEmpty()) details else state.pendingDetails,
                journal = state.journal + details.map { JournalEntry(it.id, it.text, it.source, it.status) }
                    .filter { entry -> state.journal.none { it.id == entry.id } },
                dead = updated?.character?.alive == false,
                levelUpAvailable = (updated?.character?.unspentPoints ?: 0) > 0 ||
                    updated?.character?.pendingLevelUp == true,
            )
        }
        haptics.play(renderer.state.value.haptic)
    }

    suspend fun allocate(allocations: List<Pair<String, Int>>): Outcome<Unit> {
        val id = activeCampaignId ?: return Outcome.Failure(IllegalStateException("no campaign"), "")
        return when (val result = api.allocate(id, allocations)) {
            is Outcome.Failure -> result
            is Outcome.Success -> {
                val character = result.value.character
                _state.update { state ->
                    val campaign = state.campaign?.copy(character = character)
                    state.copy(
                        campaign = campaign,
                        levelUpAvailable = character.unspentPoints > 0 || character.pendingLevelUp,
                        overlay = com.textworld.app.data.repository.Overlay.NONE,
                    )
                }
                Outcome.Success(Unit)
            }
        }
    }

    suspend fun acceptDetail(id: Int, accept: Boolean) {
        val result = api.acceptDetail(id, accept)
        if (result is Outcome.Success) {
            _state.update { state ->
                state.copy(
                    pendingDetails = state.pendingDetails.filter { it.id != id },
                    journal = state.journal.map {
                        if (it.id == id) it.copy(status = if (accept) "canon" else "rejected") else it
                    },
                )
            }
        } else {
            showMessage("Мир не ответил. Попробуй ещё раз.")
        }
    }

    fun showMessage(message: String) = _state.update { it.copy(inWorldMessage = message) }

    fun realtimeEvents(campaignId: String) = realtime.events(campaignId) { _state.value.campaign?.stepNo ?: 0 }

    fun handleRealtime(event: RealtimeEvent) {
        when (event) {
            is RealtimeEvent.Server -> when (event.name) {
                "turn_final", "turn_started" -> {
                    val payload = event.payload ?: return
                    runCatching { parsePayload<TurnResponse>(payload) }.getOrNull()?.let { applyFinal(it) }
                }
                "awaiting_orders", "orders_open" -> {
                    val obj = runCatching { TwJson.parseToJsonElement(event.payload.orEmpty()) }.getOrNull() as? JsonObject
                    val pending = obj?.get("pending")?.toString()?.removeSurrounding("[", "]")
                        ?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                    _state.update {
                        it.copy(waitingFor = pending, frozenStep = null, streaming = false)
                    }
                }
                "presence" -> Unit
                "rollback" -> _state.update { it.copy(rewindTrigger = it.rewindTrigger + 1) }
                "character_updated" -> {
                    val obj = runCatching { TwJson.parseToJsonElement(event.payload.orEmpty()) }.getOrNull() as? JsonObject
                    val character = obj?.get("character")?.let {
                        runCatching { TwJson.decodeFromString<com.textworld.app.data.model.Character>(it.toString()) }.getOrNull()
                    }
                    if (character != null) _state.update { s ->
                        s.copy(campaign = s.campaign?.copy(character = character))
                    }
                }
                "error" -> {
                    val message = runCatching {
                        (TwJson.parseToJsonElement(event.payload.orEmpty()) as? JsonObject)?.get("message").toString()
                            .removeSurrounding("\"")
                    }.getOrNull()?.takeIf { it.isNotBlank() && it != "null" }
                        ?: "Мир не ответил. Попробуй ещё раз."
                    showMessage(message)
                }
            }
            is RealtimeEvent.Disconnected, is RealtimeEvent.Fallback -> Unit
        }
    }

    suspend fun loadGames() {
        val campaigns = api.myCampaigns().getOrNull().orEmpty()
        _state.update { it.copy(myGames = campaigns) }
    }

    suspend fun loadSocial() {
        val friends = api.friends().getOrNull().orEmpty()
        val online = api.onlineUsers().getOrNull().orEmpty()
        val invites = api.invites().getOrNull().orEmpty()
        _state.update { it.copy(friends = friends, onlineUsers = online, invites = invites) }
    }

    suspend fun friendRequest(username: String) {
        val result = api.friendRequest(username)
        if (result is Outcome.Success) loadSocial()
    }

    suspend fun friendAccept(id: Int) {
        val result = api.friendAccept(id)
        if (result is Outcome.Success) loadSocial()
    }

    suspend fun inviteToGame(campaignId: String, userId: Int) {
        when (val result = api.invite(campaignId, userId)) {
            is Outcome.Failure -> showMessage(result.message)
            else -> Unit
        }
    }

    suspend fun acceptInvite(inviteId: Int): String? {
        val result = api.acceptInvite(inviteId)
        return if (result is Outcome.Success && result.value.campaign_id.isNotBlank()) {
            val id = result.value.campaign_id
            openCampaign(id)
            loadSocial()
            id
        } else {
            showMessage("Мир не ответил. Попробуй ещё раз.")
            null
        }
    }

    suspend fun branchAction(action: String, branchId: String = "") {
        val id = activeCampaignId ?: return
        val result = api.branch(action, mapOf("campaign_id" to id, "branch_id" to branchId))
        when (result) {
            is Outcome.Success -> {
                result.value.prose?.let { prose -> _state.update { it.copy(prose = prose) } }
                if (action == "rewind") _state.update { it.copy(rewindTrigger = it.rewindTrigger + 1) }
            }
            is Outcome.Failure -> showMessage(result.message)
        }
    }

    suspend fun syncSettings() {
        api.postSettings(settingsStore.payload())
    }
}
