package com.example.thewatcher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Estimated per-device usage within a single hotspot session.
 * One row per (sessionId, macAddress).
 */
@Entity(
    tableName = "session_device_usage",
    primaryKeys = ["sessionId", "macAddress"]
)
data class SessionDeviceUsage(
    val sessionId: Long,
    val macAddress: String,
    val estRxBytes: Long = 0L,
    val estTxBytes: Long = 0L
)
