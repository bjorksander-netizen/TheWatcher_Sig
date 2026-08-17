package com.example.thewatcher.monitor

import org.junit.Assert.assertEquals
import org.junit.Test

class PerDeviceSplitterTest {

    @Test
    fun `splits proportionally to duration`() {
        val clients = listOf(
            PerDeviceSplitter.ClientWeight("AA", 10_000),
            PerDeviceSplitter.ClientWeight("BB", 30_000)
        )
        val result = PerDeviceSplitter.split(400, 0, clients)
        assertEquals(100L to 0L, result["AA"])
        assertEquals(300L to 0L, result["BB"])
    }

    @Test
    fun `residual goes to last client so sum is exact`() {
        val clients = listOf(
            PerDeviceSplitter.ClientWeight("AA", 1),
            PerDeviceSplitter.ClientWeight("BB", 1),
            PerDeviceSplitter.ClientWeight("CC", 1)
        )
        val result = PerDeviceSplitter.split(10, 0, clients)
        val sum = result.values.sumOf { it.first }
        assertEquals(10L, sum)
    }

    @Test
    fun `empty clients returns empty map`() {
        assertEquals(0, PerDeviceSplitter.split(100, 0, emptyList()).size)
    }

    @Test
    fun `zero total returns zeros`() {
        val clients = listOf(PerDeviceSplitter.ClientWeight("AA", 1000))
        val result = PerDeviceSplitter.split(0, 0, clients)
        assertEquals(0L to 0L, result["AA"])
    }
}
