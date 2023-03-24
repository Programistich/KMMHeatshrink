package com.flipperdevices.heatshrink.models

enum class HsInputState {
    TAG_BIT,                /* tag bit */
    YIELD_LITERAL,          /* ready to yield literal byte */
    BACKREF_BOUNDS,         /* ready to yield backref bounds */
    YIELD_BACKREF,          /* ready to yield back-reference */
    BUFFER_EMPTY,           /* Not enough data to continue */
}
