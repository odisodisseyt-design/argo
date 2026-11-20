package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Whitelisted character view. Unknown fields are ignored. */
@Serializable
data class Character(
    val id: Int = 0,
    val name: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    @SerialName("xp_to_next") val xpToNext: Int = 0,
    @SerialName("unspent_points") val unspentPoints: Int = 0,
    val attributes: Map<String, Int> = emptyMap(),
    val derived: Map<String, JsonElement> = emptyMap(),
    val body: Map<String, JsonElement> = emptyMap(),
    val conditions: List<JsonElement> = emptyList(),
    val skills: List<JsonElement> = emptyList(),
    val inventory: List<JsonElement> = emptyList(),
    val equipment: Map<String, JsonElement> = emptyMap(),
    val currency: Map<String, Int> = emptyMap(),
    val statuses: List<JsonElement> = emptyList(),
    @SerialName("pending_level_up") val pendingLevelUp: Boolean = false,
    val alive: Boolean = true,
    val location: String = "",
)

@Serializable
data class Item(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val rarity: String = "common",
    val type: String = "",
    val slot: String = "",
    val quantity: Int = 1,
    val weight: Float = 0f,
    val durability: JsonElement? = null,
    val price: Int = 0,
    @SerialName("cost_base") val costBase: Int? = null,
    @SerialName("cost_next") val costNext: Int? = null,
    @SerialName("discount_reason") val discountReason: String? = null,
    val currency: String = "",
    val stock: Int? = null,
    val properties: Map<String, JsonElement> = emptyMap(),
    val comparison: JsonElement? = null,
)

@Serializable
data class Skill(
    val id: String = "",
    val name: String = "",
    val level: Int = 0,
    val category: String = "",
    @SerialName("cost_base") val costBase: Int? = null,
    @SerialName("cost_next") val costNext: Int? = null,
    @SerialName("discount_reason") val discountReason: String? = null,
    val description: String = "",
)

@Serializable
data class Condition(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("icon_hint") val iconHint: String = "",
    @SerialName("duration_turns") val durationTurns: Int? = null,
)

/** Extracts a human-readable label from a value that may be a string or object. */
fun readableStatus(element: JsonElement?, fallback: String = ""): String {
    if (element == null) return fallback
    if (element is JsonPrimitive) return element.contentOrNull ?: fallback
    val obj = element.asModel<Map<String, JsonElement>>() ?: return fallback
    return (obj["name"] ?: obj["status"] ?: obj["label"])?.asString().orEmpty().ifBlank { fallback }
}

private fun JsonElement.asString(): String = (this as? JsonPrimitive)?.contentOrNull ?: ""

fun Character.conditionsAsModels(): List<Condition> = conditions.mapNotNull { it.asModel<Condition>() }
fun Character.skillsAsModels(): List<Skill> = skills.mapNotNull { it.asModel<Skill>() }
fun Character.inventoryAsModels(): List<Item> = inventory.mapNotNull { it.asModel<Item>() }
