package com.misgus.agustream.core.model

/** One playable stream returned by a `stream` resource. */
data class Stream(
    /** Direct URL to play. Null for torrent/external-only streams. */
    val url: String? = null,
    val ytId: String? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    /** URL to open outside the app (e.g. a web player). */
    val externalUrl: String? = null,
    /** Short label shown as the stream source, e.g. the addon name. */
    val name: String? = null,
    /** Longer label, usually the file name or quality. */
    val title: String? = null,
    /**
     * Formatted, multi-line details produced by the addon's formatter.
     *
     * AIOStreams puts the configured format here (quality, HDR/audio tags, size,
     * languages...), so it is shown as-is instead of being parsed.
     */
    val description: String? = null,
    val behaviorHints: StreamBehaviorHints? = null,
    /**
     * HTTP headers the player must send
     * (`behaviorHints.proxyHeaders.request.headers`), already flattened.
     */
    val httpHeaders: Map<String, String>? = null,
) {
    /** Whether this stream can be handed straight to the player. */
    val isDirectPlayable: Boolean get() = !url.isNullOrEmpty()
}

/** The `behaviorHints` object of a [Stream]. */
data class StreamBehaviorHints(
    /** Groups streams that belong to the same release, for binge-watching. */
    val bingeGroup: String? = null,
    val filename: String? = null,
    /** Size in bytes. */
    val videoSize: Long? = null,
)
