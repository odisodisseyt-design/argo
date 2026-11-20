package com.textworld.app.data.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.decodeFromString

/** Lenient JSON used for every server payload: unknown keys are ignored. */
val TwJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

inline fun <reified T> parsePayload(raw: String): T = TwJson.decodeFromString<T>(raw)

inline fun <reified T> JsonElement.asModel(): T? = runCatching {
    TwJson.decodeFromString<T>(this.toString())
}.getOrNull()
