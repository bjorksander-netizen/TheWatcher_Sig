package com.example.thewatcher.monitor

/**
 * Parses `ip neigh show` output. Each line looks like:
 *   192.168.43.10 dev wlan0 lladdr 3a:5b:...:9f router STALE
 *   192.168.43.1 dev wlan0 lladdr 02:...:00 PERMANENT
 *
 * We keep entries that have a MAC (lladdr) and a non-loopback IP. The device
 * itself (gateway) is typically PERMANENT / has no lladdr in some setups and is
 * filtered by callers if needed.
 */
object NeighParser {

    data class NeighRow(val ip: String, val mac: String, val iface: String)

    private val INCOMPLETE = setOf("00:00:00:00:00:00", "0x0", "", "(incomplete)")

    fun parse(text: String): List<NeighRow> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val tokens = line.split(Regex("\\s+"))
                if (tokens.size < 2) return@mapNotNull null
                val ip = tokens[0]
                // Find lladdr + mac
                val llIdx = tokens.indexOf("lladdr")
                val mac = if (llIdx >= 0 && llIdx + 1 < tokens.size) tokens[llIdx + 1] else ""
                if (mac in INCOMPLETE) return@mapNotNull null
                // Find dev + iface
                val devIdx = tokens.indexOf("dev")
                val iface = if (devIdx >= 0 && devIdx + 1 < tokens.size) tokens[devIdx + 1] else ""
                NeighRow(ip, mac, iface)
            }
            .filter { it.mac.isNotEmpty() }
            .toList()
    }
}
