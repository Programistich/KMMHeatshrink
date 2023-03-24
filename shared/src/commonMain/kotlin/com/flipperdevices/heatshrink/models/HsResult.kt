package com.flipperdevices.heatshrink.models

data class HsResult(
    var off: Int = 0,
    val len: Int = 0,
    val end: Int = 0,
    val b: ByteArray = ByteArray(0),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HsResult

        if (off != other.off) return false
        if (len != other.len) return false
        if (end != other.end) return false
        if (!b.contentEquals(other.b)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = off
        result = 31 * result + len
        result = 31 * result + end
        result = 31 * result + b.contentHashCode()
        return result
    }
}
