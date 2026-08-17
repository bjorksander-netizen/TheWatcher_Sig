package com.example.thewatcher.monitor

import org.junit.Assert.assertEquals
import org.junit.Test

class TetheredEstimatorTest {

    @Test
    fun `subtracts device own usage`() {
        val t = TetheredEstimator.estimateTethered(
            mobileRx = 1000, mobileTx = 500,
            deviceOwnRx = 200, deviceOwnTx = 100
        )
        assertEquals(800, t.rxBytes)
        assertEquals(400, t.txBytes)
    }

    @Test
    fun `never returns negative`() {
        val t = TetheredEstimator.estimateTethered(
            mobileRx = 100, mobileTx = 50,
            deviceOwnRx = 200, deviceOwnTx = 100
        )
        assertEquals(0, t.rxBytes)
        assertEquals(0, t.txBytes)
    }
}
