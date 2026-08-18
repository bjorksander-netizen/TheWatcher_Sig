package com.example.thewatcher.monitor

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.example.thewatcher.data.model.ConnectedClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Detects clients connected to the device's hotspot using multiple strategies,
 * in order of preference, returning the first non-empty result.
 *
 * Strategies:
 *  1. SoftAp callback (API 30+), via reflection — may be empty on some vendors.
 *  2. `ip neigh show` — reads the kernel neighbour table (includes tethered clients).
 *  3. `cat /proc/net/arp` — older fallback.
 *  4. Direct read of /proc/net/arp as a file.
 *
 * Each attempt records a diagnostic so the UI can explain failures.
 */
class ClientDetector(context: Context) {

    private val appContext = context.applicationContext
    private val softAp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SoftApClientProvider(
            appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        ).also { it.register() }
    } else null

    private val diagnostics = mutableListOf<String>()

    fun getDiagnostic(): String = diagnostics.joinToString(" | ")

    suspend fun detect(): List<ConnectedClient> {
        diagnostics.clear()

        // 1. SoftAp
        if (softAp != null) {
            val list = runCatching { softAp!!.getClients() }.getOrDefault(emptyList())
            if (list.isNotEmpty()) return list
            diagnostics.add("softap:empty")
        } else {
            diagnostics.add("softap:n/a(<API30)")
        }

        // 2. ip neigh
        runCommand("ip", "neigh", "show").let { out ->
            if (out.isBlank()) diagnostics.add("ipneigh:blank")
            else {
                val parsed = NeighParser.parse(out)
                if (parsed.isNotEmpty()) return parsed.map { toClient(it) }
                diagnostics.add("ipneigh:noclient")
            }
        }

        // 3. cat /proc/net/arp
        runCommand("cat", "/proc/net/arp").let { out ->
            if (out.isBlank()) diagnostics.add("arp:blank")
            else {
                val parsed = ArpParser.parse(out)
                if (parsed.isNotEmpty()) return parsed.map { row ->
                    ConnectedClient(
                        macAddress = row.mac,
                        ipAddress = row.ip,
                        hostname = "",
                        firstSeenMs = System.currentTimeMillis(),
                        lastSeenMs = System.currentTimeMillis()
                    )
                }
                diagnostics.add("arp:noclient")
            }
        }

        // 4. direct file read
        readFileDirect("/proc/net/arp")?.let { out ->
            if (out.isNotBlank()) {
                val parsed = ArpParser.parse(out)
                if (parsed.isNotEmpty()) return parsed.map { row ->
                    ConnectedClient(
                        macAddress = row.mac,
                        ipAddress = row.ip,
                        hostname = "",
                        firstSeenMs = System.currentTimeMillis(),
                        lastSeenMs = System.currentTimeMillis()
                    )
                }
            }
            diagnostics.add("arpfile:noclient")
        } ?: diagnostics.add("arpfile:unreadable")

        return emptyList()
    }

    private fun toClient(row: NeighParser.NeighRow): ConnectedClient {
        return ConnectedClient(
            macAddress = row.mac,
            ipAddress = row.ip,
            hostname = "",
            firstSeenMs = System.currentTimeMillis(),
            lastSeenMs = System.currentTimeMillis()
        )
    }

    private fun runCommand(vararg cmd: String): String {
        return try {
            val proc = ProcessBuilder(*cmd).start()
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(proc.inputStream)).useLines { lines ->
                lines.forEach { sb.appendLine(it) }
            }
            proc.waitFor(2, TimeUnit.SECONDS)
            sb.toString()
        } catch (e: Exception) {
            diagnostics.add("cmd:${cmd.joinToString(" ")}:${e.message?.take(40)}")
            ""
        }
    }

    private fun readFileDirect(path: String): String? {
        return try {
            java.io.File(path).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    fun release() {
        softAp?.release()
    }
}
