package com.misgus.pengu.ui.streams

/** What the stream picker should look up. */
data class StreamsRequest(
    val type: String,
    val id: String,
    val title: String,
    val year: String? = null,
    val background: String? = null,
)
