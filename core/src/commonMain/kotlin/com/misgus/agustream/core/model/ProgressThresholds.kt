package com.misgus.agustream.core.model

/**
 * Fractions of a title used to decide "watched", "resume" and "in progress".
 *
 * They are compared across layers (domain rules, data stores, UI), so they are
 * defined once here instead of being repeated as bare literals.
 */
object ProgressThresholds {
    /** At or above this fraction a title counts as completed/watched. */
    const val COMPLETION = 0.9

    /** Below this fraction a saved position is too early to resume. */
    const val RESUME_MIN = 0.02

    /** Above this fraction a saved position is too close to the end to resume. */
    const val RESUME_MAX = 0.9

    /** Lower bound of the "still in progress" band shown on a poster. */
    const val IN_PROGRESS_MIN = 0.02

    /** Upper bound of the "still in progress" band shown on a poster. */
    const val IN_PROGRESS_MAX = 0.95
}
