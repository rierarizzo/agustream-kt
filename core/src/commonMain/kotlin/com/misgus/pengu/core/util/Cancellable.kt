package com.misgus.pengu.core.util

import kotlinx.coroutines.CancellationException

/**
 * Like [runCatching], but never swallows coroutine cancellation.
 *
 * A [CancellationException] means the caller went away (a screen left the
 * composition, a scope was cancelled). Turning it into a `Result.failure` would
 * keep cancelled work alive and let it write stale state, so it is always
 * rethrown.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        Result.failure(error)
    }
