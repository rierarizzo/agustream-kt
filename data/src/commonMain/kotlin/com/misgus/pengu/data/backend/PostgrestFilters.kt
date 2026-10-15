package com.misgus.pengu.data.backend

import com.misgus.pengu.data.addons.encodeValue

/**
 * Characters PostgREST treats as structural inside a filter value.
 *
 * They must be backslash-escaped so a content id such as `tt1:2:3` or a title
 * containing a comma cannot change the shape of the query.
 */
private const val POSTGREST_RESERVED = ",.:()"

/**
 * Encodes [value] so it can be embedded in a PostgREST filter.
 *
 * Two layers are needed: PostgREST parses the *decoded* value and uses
 * backslashes to escape its reserved characters, and the URL layer needs the
 * result percent-encoded.
 */
internal fun postgrestLiteral(value: String): String {
    val escaped =
        buildString(value.length) {
            for (char in value) {
                if (char == '\\' || char in POSTGREST_RESERVED) append('\\')
                append(char)
            }
        }
    return encodeValue(escaped)
}
