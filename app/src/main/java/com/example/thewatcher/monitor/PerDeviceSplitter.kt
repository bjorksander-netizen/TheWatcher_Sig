package com.example.thewatcher.monitor

/**
 * Splits an aggregate tethered byte count across connected clients proportionally
 * to how long each client has been connected (connection duration weight).
 *
 * This is an ESTIMATE (Android does not expose per-MAC byte counters to
 * non-root apps). The split weights sum to 1, and rounding residuals are given
 * to the last client so that sum(estimated) == total exactly.
 *
 * Pure JVM.
 */
object PerDeviceSplitter {

    data class ClientWeight(val macAddress: String, val durationMs: Long)

    fun split(
        totalRx: Long,
        totalTx: Long,
        clients: List<ClientWeight>
    ): Map<String, Pair<Long, Long>> {
        if (clients.isEmpty() || (totalRx == 0L && totalTx == 0L)) {
            return clients.associate { it.macAddress to (0L to 0L) }
        }

        val totalDuration = clients.sumOf { it.durationMs.coerceAtLeast(0L) }
        if (totalDuration == 0L) {
            // No duration info: split equally.
            val even = clients.size
            return clients.associate {
                it.macAddress to ((totalRx / even) to (totalTx / even))
            }
        }

        val result = LinkedHashMap<String, Pair<Long, Long>>()
        var rxRemainder = totalRx
        var txRemainder = totalTx
        clients.forEachIndexed { index, client ->
            val weight = client.durationMs.toDouble() / totalDuration
            val rx = if (index == clients.lastIndex) {
                rxRemainder
            } else {
                (totalRx * weight).toLong().also { rxRemainder -= it }
            }
            val tx = if (index == clients.lastIndex) {
                txRemainder
            } else {
                (totalTx * weight).toLong().also { txRemainder -= it }
            }
            result[client.macAddress] = rx to tx
        }
        return result
    }
}
