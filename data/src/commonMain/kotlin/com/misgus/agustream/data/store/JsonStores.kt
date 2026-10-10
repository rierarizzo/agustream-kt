package com.misgus.agustream.data.store

import com.misgus.agustream.data.appJson
import com.misgus.agustream.data.arrOrNull
import com.misgus.agustream.data.objOrNull
import com.misgus.agustream.data.platform.ioDispatcher
import com.misgus.agustream.data.platform.readTextFile
import com.misgus.agustream.data.platform.writeTextFile
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * A tiny JSON-array document store backed by a file.
 *
 * A missing or corrupt file reads as empty instead of throwing, so the app
 * degrades to "no data" rather than failing to start.
 */
class JsonFileStore(private val path: String) {
    suspend fun read(): List<JsonObject> = withContext(ioDispatcher) {
        val text = readTextFile(path) ?: return@withContext emptyList()
        runCatching {
            appJson.parseToJsonElement(text).arrOrNull()?.mapNotNull { it.objOrNull() }
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun write(rows: List<JsonObject>) = withContext(ioDispatcher) {
        runCatching { writeTextFile(path, JsonArray(rows).toString()) }
        Unit
    }
}

/**
 * A JSON-object document store backed by a file.
 *
 * A missing or corrupt file reads as `{}` instead of throwing.
 */
class JsonMapStore(private val path: String) {
    suspend fun read(): JsonObject = withContext(ioDispatcher) {
        val text = readTextFile(path) ?: return@withContext JsonObject(emptyMap())
        runCatching { appJson.parseToJsonElement(text).objOrNull() }
            .getOrNull() ?: JsonObject(emptyMap())
    }

    suspend fun write(data: JsonObject) = withContext(ioDispatcher) {
        runCatching { writeTextFile(path, data.toString()) }
        Unit
    }
}
