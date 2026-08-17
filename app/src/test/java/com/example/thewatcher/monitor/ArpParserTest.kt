package com.example.thewatcher.monitor

import org.junit.Assert.assertEquals
import org.junit.Test

class ArpParserTest {

    private val sample = """
IP address       HW type     Flags       HW address            Mask     Device
192.168.43.1     0x1         0x2         00:11:22:33:44:55     *        wlan0
192.168.43.10    0x1         0x2         aa:bb:cc:dd:ee:ff     *        wlan0
192.168.43.99    0x1         0x0         00:00:00:00:00:00     *        wlan0
""".trimIndent()

    @Test
    fun `parses two valid rows and skips incomplete mac`() {
        val rows = ArpParser.parse(sample)
        assertEquals(2, rows.size)
        assertEquals("192.168.43.1", rows[0].ip)
        assertEquals("00:11:22:33:44:55", rows[0].mac)
        assertEquals("wlan0", rows[0].netInterface)
        assertEquals("aa:bb:cc:dd:ee:ff", rows[1].mac)
    }

    @Test
    fun `empty input yields empty list`() {
        assertEquals(0, ArpParser.parse("").size)
        assertEquals(0, ArpParser.parse("IP address HW type Flags HW address Mask Device").size)
    }
}
