package com.example.thewatcher.monitor

/**
 * Parses the raw text of `/proc/net/arp` into rows of (ip, mac, interface).
 * Pure JVM function — no Android dependency — so it is unit-testable.
 *
 * Incomplete entries (MAC "00:00:00:00:00:00" or "0x0") are ignored because they
 * represent ARP table slots that have not resolved a real device.
 */
object ArpParser {

    data class ArpRow(val ip: String, val mac: String, val netInterface: String)

    private val INCOMPLETE = setOf("00:00:00:00:00:00", "0x0", "(incomplete)", "")

    fun parse(text: String): List<ArpRow> {
        val lines = text.lineSequence()
        return lines.drop(1) // skip header
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                // Columns (space separated): IP, HW type, Flags, MAC, Mask, Interface
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 6) return@mapNotNull null
                val ip = parts[0]
                val mac = parts[3]
                val netInterface = parts[5]
                if (mac in INCOMPLETE) return@mapNotNull null
                ArpRow(ip, mac, netInterface)
            }
            .toList()
    }
}
