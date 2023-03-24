package com.flipperdevices.heatshrink.streams

import com.flipperdevices.heatshrink.models.HsInputState
import com.flipperdevices.heatshrink.models.HsResult
import okio.Buffer
import okio.IOException
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "HsInputStream")
class HsInputStream(
    private val array: ByteArray,
    private val windowSize: Int = 11,
    private val lookaheadSize: Int = 4,
    private val bufferSize: Int = bestInputBufferSize(0, windowSize),
    private var inputBuffer: ByteArray = ByteArray(bufferSize) { 0 },
    private val window: ByteArray = ByteArray(1 shl windowSize) { 0 },
) {
    private val state: HsInputState = HsInputState.TAG_BIT
    private var outputCount: Int = 0
    private var outputIndex: Int = 0
    private var inputBufferPos: Int = 0
    private var inputBufferLen: Int = 0
    private var inputExhausted: Boolean = false
    private var currentBytePos: Int = 0
    private var currentByte: Int = 0
    private var windowPos: Int = 0
    private var rr = HsResult()
    private val buffer = Buffer().apply {
        write(array)
    }

    @ObjCName("read")
    @Throws(IOException::class)
    fun read(@ObjCName("bytes") bytes: ByteArray): Int {
        return read(bytes, 0, bytes.size)
    }

    @Throws(IOException::class)
    private fun read(b: ByteArray, off: Int, len: Int): Int {
        rr = rr.copy(off = off, len = len, end = off + len, b = b)
        var lastState = state

        while(rr.off < rr.end) {
            lastState = when (lastState) {
                HsInputState.TAG_BIT -> readTagBit()
                HsInputState.YIELD_LITERAL -> readLiteral(rr)
                HsInputState.BACKREF_BOUNDS -> readBackrefBounds()
                HsInputState.YIELD_BACKREF -> readBackref(rr)
                HsInputState.BUFFER_EMPTY -> { break }
            }
        }

        val numRead = rr.off - off
        return if (numRead > 0) {
            numRead
        } else {
            if (inputExhausted) {
                -1
            } else {
                0
            }
        }
    }

    @Throws(IOException::class)
    private fun readTagBit(): HsInputState {
        val bits: Int = getBits(1) // get tag bit
        if (bits == -1) {
            return HsInputState.BUFFER_EMPTY
        } else if (bits != 0) {
            return HsInputState.YIELD_LITERAL
        }
        outputIndex = 0
        outputCount = outputIndex
        return HsInputState.BACKREF_BOUNDS
    }

    @Throws(IOException::class)
    private fun readLiteral(rr: HsResult): HsInputState {
        if (rr.off < rr.end) {
            val bits = getBits(8)
            if (bits == -1) return HsInputState.BUFFER_EMPTY
            val mask = (1 shl windowSize) - 1
            val c = (bits and 0xff).toByte()
            window[windowPos++ and mask] = c
            rr.b[rr.off++] = c
            return HsInputState.TAG_BIT
        }
        return HsInputState.YIELD_LITERAL
    }

    @Throws(IOException::class)
    private fun readBackrefBounds(): HsInputState {
        var bits = getBits(windowSize)
        if (bits == -1) return HsInputState.BUFFER_EMPTY
        outputIndex = bits + 1
        bits = getBits(lookaheadSize)
        if (bits == -1) return HsInputState.BUFFER_EMPTY
        outputCount = bits + 1
        return HsInputState.YIELD_BACKREF
    }

    private fun readBackref(rr: HsResult): HsInputState {
        val count: Int = min(rr.end - rr.off, outputCount)
        if (count > 0) {
            val mask = (1 shl windowSize) - 1
            for (i in 0 until count) {
                val c = window[windowPos - outputIndex and mask]
                rr.b[rr.off++] = c
                window[windowPos++ and mask] = c
            }
            outputCount -= count
            if (outputCount == 0) {
                return HsInputState.TAG_BIT
            }
        }
        return HsInputState.YIELD_BACKREF
    }

    @Throws(IOException::class)
    fun getBits(numBitsInput: Int): Int {
        var numBits = numBitsInput
        var ret = 0
        if (!ensureAvailable(numBits)) {
            return -1
        }
        while (numBits > 0) {
            if (currentBytePos == 0) {
                currentByte = inputBuffer[inputBufferPos++].toInt()
                currentBytePos = 8
            }
            ret = ret shl 1
            if (currentBytePos == 8 && numBits >= 8) {
                ret = ret shl 7 // look up!  we've already shifted one
                ret = ret or (currentByte and 0xff)
                numBits -= 7 // the final bit it subtracted after the loop
                currentBytePos = 1
            } else if (currentByte and (1 shl currentBytePos - 1) != 0) {
                ret = ret or 0x01
            }
            numBits--
            currentBytePos--
        }
        return ret
    }

    @Throws(IOException::class)
    fun ensureAvailable(bits: Int): Boolean {
        var bitsRequired = bits
        val bytesRemaining = inputBufferLen - inputBufferPos
        var bitsAvailable = bytesRemaining * 8
        bitsRequired -= currentBytePos
        if (bitsRequired > bitsAvailable) {
            if (bytesRemaining > 0) {
                inputBuffer = inputBuffer.copyInto(inputBuffer, 0, bytesRemaining, inputBuffer.size)
            }
            inputBufferPos = 0
            inputBufferLen = bytesRemaining
            val numRead = buffer.read(inputBuffer, bytesRemaining, inputBuffer.size - bytesRemaining)
            if (numRead > -1) {
                inputBufferLen += numRead
            } else {
                inputExhausted = true
            }
            bitsAvailable = inputBufferLen * 8
        }
        return bitsAvailable >= bitsRequired
    }


    companion object {
        fun bestInputBufferSize(bufferSize: Int, windowSize: Int): Int {
            return max(1 shl windowSize, bufferSize)
        }
    }
}
