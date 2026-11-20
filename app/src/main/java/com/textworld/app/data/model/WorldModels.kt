package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class Anchor(
    val location: JsonElement? = null,
    val time: JsonElement? = null,
    val weather: String = "",
    @SerialName("visibility_m") val visibilityM: Int = 0,
    val nearby: List<Nearby> = emptyList(),
    val environment: JsonElement? = null,
    val player: JsonElement? = null,
    val party: List<PartyMember> = emptyList(),
)

@Serializable
data class Nearby(
    val id: String = "",
    val name: String = "",
    @SerialName("distance_m") val distanceM: Float = 0f,
    val direction: String = "",
    @SerialName("height_m") val heightM: Float? = null,
    val properties: Map<String, JsonElement> = emptyMap(),
    val affordances: List<String> = emptyList(),
    val forbidden: List<String> = emptyList(),
)

@Serializable
data class PartyMember(
    val name: String = "",
    val location: String = "",
    val alive: Boolean = true,
)

fun anchorLocationName(anchor: Anchor): String {
    val obj = anchor.location?.asModel<Map<String, JsonElement>>() ?: return ""
    return (obj["name"] as? JsonPrimitive)?.contentOrNull ?: ""
}

fun anchorCoordinates(anchor: Anchor): String {
    val obj = anchor.location?.asModel<Map<String, JsonElement>>() ?: return ""
    val coords = obj["coords"]?.asModel<List<Int>>() ?: return ""
    return coords.joinToString(", ")
}

fun anchorElevation(anchor: Anchor): String {
    val obj = anchor.location?.asModel<Map<String, JsonElement>>() ?: return ""
    val value = (obj["elevation_m"] as? JsonPrimitive)?.contentOrNull ?: return ""
    return "$value м"
}

fun anchorClock(anchor: Anchor): String {
    val obj = anchor.time?.asModel<Map<String, JsonElement>>() ?: return ""
    val day = (obj["day"] as? JsonPrimitive)?.contentOrNull ?: "?"
    val hour = (obj["hour"] as? JsonPrimitive)?.contentOrNull ?: "?"
    return "День $day · $hour:00"
}

@Serializable
data class PendingDetail(
    val id: Int = 0,
    val text: String = "",
    val source: String = "",
    val status: String = "pending",
)

@Serializable
data class JournalEntry(
    val id: Int = 0,
    val text: String = "",
    val source: String = "lore",
    val status: String = "pending",
)

@Serializable
data class Quest(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val kind: String = "main",
    val objectives: List<QuestObjective> = emptyList(),
    @SerialName("time_left") val timeLeft: String = "",
    val rewards: List<String> = emptyList(),
)

@Serializable
data class QuestObjective(
    val text: String = "",
    val done: Boolean = false,
)

@Serializable
data class MapNode(
    val id: String = "",
    val name: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val visited: Boolean = false,
    val danger: String = "",
    val floor: Int = 0,
    val connections: List<String> = emptyList(),
)

@Serializable
data class NpcDossier(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val goals: List<String> = emptyList(),
    val known: List<String> = emptyList(),
    val relations: Map<String, Int> = emptyMap(),
    val conditions: List<JsonElement> = emptyList(),
    val alive: Boolean = true,
    val emotion: String = "neutral",
    val portrait: PortraitHint? = null,
)
