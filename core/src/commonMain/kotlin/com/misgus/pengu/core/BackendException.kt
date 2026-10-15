package com.misgus.pengu.core

/** Thrown when a backend operation fails. */
class BackendException(
    override val message: String,
    val statusCode: Int? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    override fun toString(): String {
        val status = statusCode?.let { " (HTTP $it)" } ?: ""
        return "BackendException$status: $message"
    }
}
