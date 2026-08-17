package com.example.thewatcher.monitor

import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage

/**
 * Converts a stored session + its per-device usages into a display-ready summary.
 * Pure JVM so it can be unit-tested without an Android device.
 */
object SessionSummaryCalculator {

    data class DeviceShare(
        val macAddress: String,
        val estRxBytes: Long,
        val estTxBytes: Long,
        val sharePct: Int // percentage of total consumed by this device
    )

    data class SessionSummary(
        val sessionId: Long,
        val startedMs: Long,
        val endedMs: Long,
        val durationMin: Long,
        val totalMB: Double,
        val totalRxBytes: Long,
        val totalTxBytes: Long,
        val deviceCount: Int,
        val perDevice: List<DeviceShare>
    )

    fun summarize(
        session: HotspotSession,
        devices: List<SessionDeviceUsage>
    ): SessionSummary {
        val ended = session.endedMs ?: session.startedMs
        val durationMin = if (ended > session.startedMs) {
            (ended - session.startedMs) / 60_000L
        } else 0L

        val totalBytes = session.totalRxBytes + session.totalTxBytes
        val totalMB = totalBytes / 1_000_000.0

        val perDevice = devices.map { d ->
            val devTotal = d.estRxBytes + d.estTxBytes
            val sharePct = if (totalBytes > 0) {
                ((devTotal.toDouble() / totalBytes) * 100).toInt().coerceIn(0, 100)
            } else 0
            DeviceShare(d.macAddress, d.estRxBytes, d.estTxBytes, sharePct)
        }.sortedByDescending { it.estRxBytes + it.estTxBytes }

        return SessionSummary(
            sessionId = session.id,
            startedMs = session.startedMs,
            endedMs = ended,
            durationMin = durationMin,
            totalMB = totalMB,
            totalRxBytes = session.totalRxBytes,
            totalTxBytes = session.totalTxBytes,
            deviceCount = session.deviceCount,
            perDevice = perDevice
        )
    }
}
