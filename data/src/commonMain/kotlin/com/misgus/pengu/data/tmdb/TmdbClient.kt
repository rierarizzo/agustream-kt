package com.misgus.pengu.data.tmdb

import com.misgus.pengu.data.appJson
import com.misgus.pengu.data.objOrNull
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject

/**
 * A TMDB response the client cannot use.
 *
 * [status] is the HTTP status, so a caller can tell "this id does not exist"
 * (404) from "the key is wrong" (401); `0` means the body itself was unusable.
 * The message never contains the API key.
 */
internal class TmdbException(
    val status: Int,
    message: String,
) : Exception(message)

/**
 * Thin client for the TMDB v3 REST API.
 *
 * Both authentication styles are supported: a v3 API key travels in the query
 * string, a v4 read access token (a JWT) in the `Authorization` header, and the
 * `eyJ` prefix decides which. The key is passed per call from the settings, so
 * changing it takes effect on the next request without rebuilding the client.
 */
internal class TmdbClient(
    private val http: HttpClient,
) {
    suspend fun getJson(
        path: String,
        key: String,
        language: String? = null,
        params: Map<String, String> = emptyMap(),
    ): JsonObject {
        val response =
            http.get("$API_BASE$path") {
                if (key.startsWith(JWT_PREFIX)) {
                    header(HttpHeaders.Authorization, "Bearer $key")
                } else {
                    parameter("api_key", key)
                }
                language?.let { parameter("language", it) }
                params.forEach { (name, value) -> parameter(name, value) }
            }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw TmdbException(response.status.value, "TMDB answered ${response.status.value} for $path")
        }
        return appJson.parseToJsonElement(body).objOrNull()
            ?: throw TmdbException(0, "TMDB returned an unusable body for $path")
    }

    private companion object {
        const val API_BASE = "https://api.themoviedb.org/3"

        /** A v4 read access token is a JWT; anything else is treated as a v3 key. */
        const val JWT_PREFIX = "eyJ"
    }
}
