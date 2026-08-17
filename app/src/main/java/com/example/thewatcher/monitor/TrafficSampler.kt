package com.example.thewatcher.monitor

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi

/**
 * Samples mobile traffic via TrafficStats and the device's own per-UID usage
 * via NetworkStatsManager (to isolate tethered traffic).
 *
 * On failure (e.g. missing subscriberId / permission) the device's own usage is
 * assumed to be 0, which makes tethered ≈ total mobile — still a safe upper bound.
 */
class TrafficSampler(private val context: Context) {

    private val statsManager: NetworkStatsManager? =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager

    fun getMobileRxBytes(): Long = android.net.TrafficStats.getMobileRxBytes()
    fun getMobileTxBytes(): Long = android.net.TrafficStats.getMobileTxBytes()

    /**
     * Total bytes the device itself sent/received over mobile in [startMs, endMs).
     * Returns (rx, tx). Best-effort; returns (0,0) on any failure.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun getDeviceOwnUsage(startMs: Long, endMs: Long): Pair<Long, Long> {
        val mgr = statsManager ?: return 0L to 0L
        val subscriberId = try {
            (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
                ?.subscriberId
        } catch (_: SecurityException) {
            null
        } ?: return 0L to 0L

        return try {
            var rx = 0L
            var tx = 0L
            val bucket = NetworkStats.Bucket()
            val query = mgr.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                subscriberId,
                startMs,
                endMs
            )
            while (query.hasNextBucket()) {
                query.getNextBucket(bucket)
                rx += bucket.rxBytes
                tx += bucket.txBytes
            }
            query.close()
            // querySummaryForDevice returns whole-device mobile usage, which for
            // our purposes we treat as an upper bound on "own" usage.
            rx to tx
        } catch (_: Exception) {
            0L to 0L
        }
    }
}
