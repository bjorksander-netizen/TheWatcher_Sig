package com.example.thewatcher.monitor

/**
 * Estimates the tethered (hotspot) traffic by subtracting the device's own
 * mobile usage from the total mobile traffic.
 * Pure JVM.
 */
object TetheredEstimator {

    data class Tethered(val rxBytes: Long, val txBytes: Long)

    fun estimateTethered(
        mobileRx: Long,
        mobileTx: Long,
        deviceOwnRx: Long,
        deviceOwnTx: Long
    ): Tethered {
        return Tethered(
            rxBytes = maxOf(0L, mobileRx - deviceOwnRx),
            txBytes = maxOf(0L, mobileTx - deviceOwnTx)
        )
    }
}
