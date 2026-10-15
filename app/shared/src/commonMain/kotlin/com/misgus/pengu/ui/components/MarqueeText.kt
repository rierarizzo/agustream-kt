package com.misgus.pengu.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Single-line label that truncates with an ellipsis and — while [active], which
 * is usually hover — scrolls like a sign so the full text can be read.
 *
 * The marquee is only attached while [active] so cards that are not pointed at
 * stay still and cheap to draw.
 */
@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    active: Boolean,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        modifier =
            modifier.then(
                if (active) {
                    Modifier.basicMarquee(initialDelayMillis = 0, repeatDelayMillis = 800)
                } else {
                    Modifier
                },
            ),
    )
}
