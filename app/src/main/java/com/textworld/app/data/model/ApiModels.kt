package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class HealthResponse(
    val ok: Boolean = false,
    val version: String = "",
    @SerialName("min_client_version") val minClientVersion: Int = 0,
    @SerialName("public_base") val publicBase: String = "",
    val packs: List<String> = emptyList(),
    val provider: String = "",
)

@Serializable
data class CampaignState(
    val id: String = "",
    val name: String = "",
    @SerialName("pack_id") val packId: String = "",
    @SerialName("step_no") val stepNo: Int = 1,
    val phase: String = "orders",
    @SerialName("waiting_for") val waitingFor: List<Int> = emptyList(),
    @SerialName("ui_manifest") val uiManifest: UiManifest = UiManifest(),
    val anchor: Anchor = Anchor(),
    val character: Character = Character(),
    val presentation: Presentation = Presentation(),
    val lore: String = "",
    val quests: List<Quest> = emptyList(),
    @SerialName("map_nodes") val mapNodes: List<MapNode> = emptyList(),
    val npcs: List<NpcDossier> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    @SerialName("shop_items") val shopItems: List<Item> = emptyList(),
)

/**
 * One envelope that can represent a plain POST /api/turn response, an SSE
 * `final` payload, or a WebSocket `turn_final` payload. Fields the pack does
 * not send simply stay at their defaults — the client never fabricates values.
 */
@Serializable
data class TurnResponse(
    @SerialName("order_saved") val orderSaved: Boolean = false,
    val preview: JsonElement? = null,
    @SerialName("waiting_for") val waitingFor: List<Int> = emptyList(),
    @SerialName("clarifying_question") val clarifyingQuestion: String? = null,
    val error: String? = null,
    val message: String? = null,
    @SerialName("step_no") val stepNo: Int = 0,
    val anchor: Anchor? = null,
    val character: Character? = null,
    val presentation: Presentation? = null,
    val prose: String? = null,
    @SerialName("outcome_text") val outcomeText: String? = null,
    @SerialName("delta") val delta: String? = null,
    @SerialName("pending_details") val pendingDetails: List<PendingDetail> = emptyList(),
    val warnings: List<String> = emptyList(),
    val event: JsonElement? = null,
    @SerialName("next_step") val nextStep: Int = 0,
)

@Serializable
data class WireEvent(
    val event: String = "",
    val payload: JsonObject? = null,
)

@Serializable
data class PublicUser(
    val id: Int = 0,
    val username: String = "",
    val role: String = "player",
    val online: Boolean = false,
)

@Serializable
data class Friend(
    val id: Int = 0,
    val username: String = "",
    val status: String = "",
    val online: Boolean = false,
)

@Serializable
data class Invite(
    val id: Int = 0,
    @SerialName("campaign_id") val campaignId: String = "",
    @SerialName("campaign_name") val campaignName: String = "",
    val inviter: String = "",
)

@Serializable
data class CampaignSummary(
    val id: String = "",
    val name: String = "",
    @SerialName("pack_id") val packId: String = "",
    @SerialName("step_no") val stepNo: Int = 1,
    val phase: String = "orders",
    val role: String = "member",
)

@Serializable
data class UpdateInfo(
    @SerialName("versionCode") val versionCode: Int = 0,
    @SerialName("versionName") val versionName: String = "",
    val changelog: List<String> = emptyList(),
    @SerialName("apk_url") val apkUrl: String = "",
    val sha256: String = "",
    @SerialName("min_client_version") val minClientVersion: Int = 0,
)

@Serializable
data class AllocateRequest(
    @SerialName("campaign_id") val campaignId: String,
    val allocations: List<Allocation>,
)

@Serializable
data class Allocation(
    val id: String,
    val delta: Int,
)
