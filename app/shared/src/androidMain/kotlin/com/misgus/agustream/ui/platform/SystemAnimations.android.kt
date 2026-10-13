package com.misgus.agustream.ui.platform

import android.animation.ValueAnimator
import android.os.Build

/**
 * Reads Android's animator duration scale, which the "Remove animations"
 * accessibility setting sets to zero.
 */
actual fun systemAnimationsEnabled(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        true
    }
