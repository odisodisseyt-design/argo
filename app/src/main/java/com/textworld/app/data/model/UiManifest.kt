package com.textworld.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The entire UI is rebuilt from this object. A feature that is false is never
 * turned into a disabled tab — its destination is not created at all.
 * Unknown keys are ignored by kotlinx.serialization, which gives us forward
 * compatibility with future packs.
 */
@Serializable
data class UiManifest(
    val features: Features = Features(),
    @SerialName("stat_schema") val statSchema: List<StatSchema> = emptyList(),
    @SerialName("resource_schema") val resourceSchema: List<StatSchema> = emptyList(),
    @SerialName("skill_categories") val skillCategories: List<String> = emptyList(),
    @SerialName("status_icons") val statusIcons: Map<String, String> = emptyMap(),
    @SerialName("currency_label") val currencyLabel: String = "золото",
    @SerialName("show_odds") val showOdds: Boolean = false,
    @SerialName("shop_tabs") val shopTabs: List<String> = DEFAULT_SHOP_TABS,
    @SerialName("shop_services") val shopServices: List<String> = emptyList(),
    @SerialName("pack_id") val packId: String = "",
    val title: String = "",
) {
    fun enabled(feature: Feature): Boolean = when (feature) {
        Feature.STATS -> features.stats
        Feature.SKILLS -> features.skills
        Feature.INVENTORY -> features.inventory
        Feature.QUESTS -> features.quests
        Feature.MAP -> features.map
        Feature.JOURNAL -> features.journal
        Feature.CHARACTERS -> features.characters
        Feature.EFFECTS -> features.effects
        Feature.LEVEL_UP -> features.levelUp
        Feature.CRAFTING -> features.crafting
        Feature.MAGIC -> features.magic
        Feature.REPUTATION -> features.reputation
        Feature.CURRENCY -> features.currency
        Feature.SHOP -> features.shop
        Feature.VOICE -> features.voice
        Feature.IMAGES -> features.images
        Feature.AUDIO -> features.audio
        Feature.HAPTICS -> features.haptics
        Feature.PARALLAX -> features.parallax
    }

    companion object {
        val DEFAULT_SHOP_TABS = listOf(
            "consumables", "equipment", "materials", "skills", "stats", "services", "unique"
        )
    }
}

@Serializable
data class Features(
    val stats: Boolean = false,
    val skills: Boolean = false,
    val inventory: Boolean = false,
    val quests: Boolean = false,
    val map: Boolean = false,
    val journal: Boolean = false,
    val characters: Boolean = false,
    val effects: Boolean = false,
    @SerialName("level_up") val levelUp: Boolean = false,
    val crafting: Boolean = false,
    val magic: Boolean = false,
    val reputation: Boolean = false,
    val currency: Boolean = false,
    val shop: Boolean = false,
    val voice: Boolean = false,
    val images: Boolean = false,
    val audio: Boolean = false,
    val haptics: Boolean = false,
    val parallax: Boolean = false,
)

enum class Feature {
    STATS, SKILLS, INVENTORY, QUESTS, MAP, JOURNAL, CHARACTERS, EFFECTS,
    LEVEL_UP, CRAFTING, MAGIC, REPUTATION, CURRENCY, SHOP,
    VOICE, IMAGES, AUDIO, HAPTICS, PARALLAX
}

@Serializable
data class StatSchema(
    val id: String,
    val name: String,
    val min: Int = 0,
    val max: Int = 100,
    val icon: String = "rune",
    val primary: Boolean = false,
    @SerialName("cost_base") val costBase: Int? = null,
    @SerialName("cost_next") val costNext: Int? = null,
    @SerialName("discount_reason") val discountReason: String? = null,
)
