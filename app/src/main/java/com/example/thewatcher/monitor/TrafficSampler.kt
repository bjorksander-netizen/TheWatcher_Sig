package com.example.thewatcher.monitor

import android.content.Context
import android.net.TrafficStats

/**
 * Samples mobile traffic via TrafficStats.
 *
 * Note: isolating the device's *own* mobile usage would require NetworkStatsManager
 * (per-UID query), but the public API surface available on the build SDK here is
 * limited, so we treat total mobile traffic as the tethered upper bound. The
 * MonitorService already accounts for this by passing own-usage = 0.
 */
class TrafficSampler(context: Context) {

    @Suppress("UNUSED_PARAMETER")
    fun getMobileRxBytes(): Long = TrafficStats.getMobileRxBytes()
    fun getMobileTxBytes(): Long = TrafficStats.getMobileTxBytes()

    /**
     * Best-effort estimate of the device's own mobile usage in [startMs, endMs).
     * Returns (0,0) on this SDK — the service treats total mobile as tethered.
     */
    fun getDeviceOwnUsage(startMs: Long, endMs: Long): Pair<Long, Long> = 0L to 0L
}
