package com.misgus.agustream.core.model

/**
 * Release year embedded in a Stremio `releaseInfo` string.
 *
 * Accepts `2019`, `2019-2023`, `2019–`, and similar shapes; returns `null` when
 * no four-digit year is present. One definition so every surface parses the same
 * way.
 */
private val RELEASE_YEAR = Regex("(?:1[89]\\d\\d|20\\d\\d)")

/** Parses the release year from [releaseInfo], or `null` when there is none. */
fun releaseYear(releaseInfo: String?): Int? = RELEASE_YEAR.find(releaseInfo ?: "")?.value?.toIntOrNull()
