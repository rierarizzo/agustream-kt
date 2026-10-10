package com.misgus.agustream.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant

/** Shared, lenient JSON codec. */
internal val appJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

/**
 * Small, defensive JSON coercion helpers.
 *
 * Addon and backend payloads are third-party data, so a malformed field should
 * degrade to `null`/empty instead of throwing and taking the whole response
 * down.
 */
internal fun JsonElement?.objOrNull(): JsonObject? = this as? JsonObject

internal fun JsonElement?.arrOrNull(): JsonArray? = this as? JsonArray

internal fun JsonElement?.stringOrNull(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive is JsonNull) return null
    return primitive.contentOrNull
}

/** Returns [this] as a `List<String>`, ignoring non-string entries. */
internal fun JsonElement?.stringList(): List<String> {
    val array = this as? JsonArray ?: return emptyList()
    return array.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p !is JsonNull }?.contentOrNull }
}

/** Returns [this] as an `Int`, accepting numeric strings. */
internal fun JsonElement?.intOrNull(): Int? = longOrNull()?.toInt()

/** Returns [this] as a `Long`, accepting numeric strings. */
internal fun JsonElement?.longOrNull(): Long? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive is JsonNull) return null
    return primitive.longOrNull ?: primitive.contentOrNull?.toDoubleOrNull()?.toLong()
}

/** Returns [this] as a `Double`, accepting numeric strings. */
internal fun JsonElement?.doubleOrNull(): Double? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive is JsonNull) return null
    return primitive.contentOrNull?.toDoubleOrNull()
}

/** Returns [this] as a `Boolean`, accepting `0`/`1` and `"true"`/`"false"`. */
internal fun JsonElement?.boolOrNull(): Boolean? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive is JsonNull) return null
    val content = primitive.contentOrNull ?: return null
    return when (content.lowercase()) {
        "true", "1" -> true
        "false", "0" -> false
        else -> null
    }
}

/** Converts an epoch-milliseconds value to a [Instant]. */
internal fun JsonElement?.instantMillisOrNull(): Instant? =
    longOrNull()?.let { Instant.fromEpochMilliseconds(it) }
