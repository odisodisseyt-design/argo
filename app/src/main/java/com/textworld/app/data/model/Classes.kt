package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LivingWorld(
    val era: String = "E0",
    @SerialName("era_name") val eraName: String = "",
    val time: WorldTime = WorldTime(),
    val weather: String = "",
    @SerialName("temperature_c") val temperatureC: Int = 0,
    val season: String = "",
    @SerialName("time_of_day") val timeOfDay: String = "",
    val events: List<String> = emptyList(),
    val rumors: List<String> = emptyList(),
    val factions: List<FactionState> = emptyList(),
    val npcs: List<LivingNpc> = emptyList(),
    val refuge: RefugeState = RefugeState(),
)

@Serializable
data class WorldTime(
    val day: Int = 0,
    val hour: Int = 0,
    val weather: String = "",
)

@Serializable
data class FactionState(
    val id: String = "",
    val name: String = "",
    val disposition: String = "neutral",
    val power: Int = 1,
    val rep: Int = 0,
    val goal: String = "",
)

@Serializable
data class LivingNpc(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val location: String = "",
    val activity: String = "",
    val disposition: String = "нейтрален",
    val needs: Map<String, Int> = emptyMap(),
)

@Serializable
data class RefugeState(
    val status: String = "none",
    val level: Int = 0,
    val name: String = "",
    val type: String = "",
    val anchor: String = "",
)

@Serializable
data class ClassSkill(
    val id: String = "",
    val name: String = "",
    val rank: Int = 1,
    val attribute: String = "",
    val bonus: Int = 0,
    val category: String = "",
    val description: String = "",
)

@Serializable
data class ClassPerk(
    val id: String = "",
    val name: String = "",
    @SerialName("desc") val description: String = "",
    val effect: JsonElement? = null,
)

@Serializable
data class ClassGear(
    @SerialName("item") val item: String = "",
    val qty: Int = 1,
)

@Serializable
data class ClassEvolution(
    val level: Int = 0,
    val name: String = "",
    @SerialName("desc") val description: String = "",
)

@Serializable
data class ClassEntry(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    @SerialName("category_name") val categoryName: String = "",
    val color: String = "#C9A227",
    val era: String = "E0",
    @SerialName("era_name") val eraName: String = "",
    @SerialName("available_at_start") val availableAtStart: Boolean = false,
    val role: String = "",
    val description: String = "",
    val attributes: Map<String, Int> = emptyMap(),
    val skills: List<ClassSkill> = emptyList(),
    val gear: List<ClassGear> = emptyList(),
    val equipment: Map<String, JsonElement> = emptyMap(),
    val perks: List<ClassPerk> = emptyList(),
    val evolutions: List<ClassEvolution> = emptyList(),
    @SerialName("starting_coins") val startingCoins: Int = 0,
)

@Serializable
data class ClassSelection(
    val pending: Boolean = false,
    val offers: List<ClassEntry> = emptyList(),
    val selected: SelectedClass? = null,
    val era: String = "E0",
)

@Serializable
data class SelectedClass(
    val id: String? = null,
    val name: String? = null,
    val category: String? = null,
)

@Serializable
data class ClassChoiceResponse(
    val ok: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val `class`: ClassEntry = ClassEntry(),
    val character: Character = Character(),
)
