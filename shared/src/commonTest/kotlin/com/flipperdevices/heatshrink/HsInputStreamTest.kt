package com.flipperdevices.heatshrink

import com.flipperdevices.heatshrink.streams.HsInputStream
import com.goncalossilva.resources.Resource
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HsInputStreamTest{
    private val resourceFolder = "src/commonTest/resources/testfiles/"

    @Test fun testBits() {
        val hsInputStream = HsInputStream(
            array = byteArrayOf(1, 2, 3, 1, 2, 3),
            windowSize = 2,
            lookaheadSize = 1
        )

        assertEquals(528408, hsInputStream.getBits(27))
        assertTrue(hsInputStream.ensureAvailable(16))
        assertEquals(33025, hsInputStream.getBits(20))
        assertFalse(hsInputStream.ensureAvailable(2))
    }

    @Test fun testFiles() {
        val hsFiles = listOf(
            "heatshrink_config.h.hs.9.8",
            "alphabet.txt.hs.8.4",
            "alphabet2.txt.hs.8.4",
            "alphabet2.txt.hs.11.4",
            "alphabet2.txt.hs.10.4",
            "README.md.hs.9.8",
        )

        hsFiles.forEach { testFile(it) }
    }

    private fun testFile(hsFileName: String) {
        val hsParameters = hsFileName.split(".hs.")

        val fileName = hsParameters[0]
        val params = hsParameters[1].split(".")
        val windowSize = params[0].toInt()
        val lookaheadSize = params[1].toInt()

        val hsData = Resource(resourceFolder + hsFileName).readBytes()
        val input = Resource(resourceFolder + fileName).readBytes()

        val inputStream = HsInputStream(
            array = hsData,
            windowSize = windowSize,
            lookaheadSize = lookaheadSize
        )
        val outputStream = Buffer()
        val buffer = ByteArray(10240)
        var tmp = inputStream.read(buffer)

        while (tmp != -1) {
            outputStream.write(buffer, 0, tmp)
            tmp = inputStream.read(buffer)
        }
        val output = outputStream.readByteArray()

        assertContentEquals(input, output)
    }
}