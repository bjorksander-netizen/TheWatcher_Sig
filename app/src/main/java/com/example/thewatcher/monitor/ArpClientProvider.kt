package com.example.thewatcher.monitor

import com.example.thewatcher.data.model.ConnectedClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Fallback for API < 31: reads /proc/net/arp and resolves each IP to a hostname
 * (best-effort, with a short timeout) to label clients.
 */
class ArpClientProvider : ClientProvider {

    override suspend fun getClients(): List<ConnectedClient> {
        val text = readArp() ?: return emptyList()
        val now = System.currentTimeMillis()
        return ArpParser.parse(text).map { row ->
            val hostname = try {
                java.net.InetAddress.getByName(row.ip).canonicalHostName
                    .takeIf { it != row.ip } ?: ""
            } catch (_: Exception) {
                ""
            }
            ConnectedClient(
                macAddress = row.mac,
                ipAddress = row.ip,
                hostname = hostname,
                firstSeenMs = now,
                lastSeenMs = now
            )
        }
    }

    private fun readArp(): String? {
        return try {
            val process = ProcessBuilder("cat", "/proc/net/arp").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val sb = StringBuilder()
            reader.useLines { lines -> lines.forEach { sb.appendLine(it) } }
            process.waitFor(2, TimeUnit.SECONDS)
            sb.toString()
        } catch (_: Exception) {
            null
        }
    }

    override fun release() {
        // Nothing to release.
    }
}
