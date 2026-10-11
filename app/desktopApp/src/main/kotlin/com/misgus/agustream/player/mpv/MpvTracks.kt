package com.misgus.agustream.player.mpv

import com.misgus.agustream.core.player.MediaTrack
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** Audio/subtitle tracks read from mpv's `track-list` property. */
internal data class MpvTracks(
    val audio: List<MediaTrack> = emptyList(),
    val subtitles: List<MediaTrack> = emptyList(),
    val selectedAudioId: String? = null,
    val selectedSubtitleId: String? = null,
)

private val tracksJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

/**
 * Parses the JSON string mpv returns for `track-list`.
 *
 * mpv numbers tracks per type, so an audio and a video track can share an id;
 * the two lists keep them apart.
 */
internal fun parseMpvTracks(json: String?): MpvTracks {
    if (json.isNullOrBlank()) return MpvTracks()
    val array =
        runCatching { tracksJson.parseToJsonElement(json) as? JsonArray }.getOrNull()
            ?: return MpvTracks()
    val audio = mutableListOf<MediaTrack>()
    val subtitles = mutableListOf<MediaTrack>()
    var selectedAudio: String? = null
    var selectedSubtitle: String? = null
    for (element in array) {
        val track = element as? JsonObject ?: continue
        val id = track.int("id") ?: continue
        val title = track.string("title")
        val language = track.string("lang")
        val selected = track.boolean("selected") == true
        when (track.string("type")) {
            "audio" -> {
                audio += MediaTrack(id = id.toString(), title = title ?: language, language = language)
                if (selected) selectedAudio = id.toString()
            }
            "sub" -> {
                subtitles +=
                    MediaTrack(
                        id = id.toString(),
                        title = title ?: language,
                        language = language,
                        uri = track.string("external-filename"),
                        isExternal = track.boolean("external") == true,
                    )
                if (selected) selectedSubtitle = id.toString()
            }
        }
    }
    return MpvTracks(
        audio = audio,
        subtitles = subtitles,
        selectedAudioId = selectedAudio,
        selectedSubtitleId = selectedSubtitle,
    )
}

/**
 * The `http-header-fields` value for mpv, or `null` when there are no headers.
 *
 * mpv expects a single comma-separated list. The option is only written when
 * there is something to set.
 */
internal fun httpHeaderOption(headers: Map<String, String>?): String? =
    headers
        .orEmpty()
        .entries
        .joinToString(",") { (name, value) -> "$name: $value" }
        .ifEmpty { null }

private fun JsonObject.string(name: String): String? = (this[name] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.int(name: String): Int? = (this[name] as? JsonPrimitive)?.intOrNull

private fun JsonObject.boolean(name: String): Boolean? = (this[name] as? JsonPrimitive)?.booleanOrNull
