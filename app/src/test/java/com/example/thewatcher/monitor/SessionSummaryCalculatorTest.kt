package com.example.thewatcher.monitor

import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionSummaryCalculatorTest {

    @Test
    fun `summarizes total mb duration and share`() {
        val session = HotspotSession(
            id = 1L,
            startedMs = 0L,
            endedMs = 600_000L, // 10 minutes
            totalRxBytes = 1_048_576, // 1 MB
            totalTxBytes = 0,
            deviceCount = 1
        )
        val devices = listOf(
            SessionDeviceUsage(sessionId = 1L, macAddress = "AA", estRxBytes = 1_048_576, estTxBytes = 0)
        )
        val summary = SessionSummaryCalculator.summarize(session, devices)
        assertEquals(10L, summary.durationMin)
        assertEquals(1, summary.deviceCount)
        assertEquals(1.048576, summary.totalMB, 0.0001)
        assertEquals(100, summary.perDevice[0].sharePct)
    }

    @Test
    fun `share percentages sum sensibly for two devices`() {
        val session = HotspotSession(
            id = 2L,
            startedMs = 0L,
            endedMs = 60_000L,
            totalRxBytes = 1_000_000,
            totalTxBytes = 0,
            deviceCount = 2
        )
        val devices = listOf(
            SessionDeviceUsage(2L, "AA", estRxBytes = 750_000, estTxBytes = 0),
            SessionDeviceUsage(2L, "BB", estRxBytes = 250_000, estTxBytes = 0)
        )
        val summary = SessionSummaryCalculator.summarize(session, devices)
        assertEquals(75, summary.perDevice[0].sharePct)
        assertEquals(25, summary.perDevice[1].sharePct)
        assertEquals(1, summary.durationMin)
    }
}
