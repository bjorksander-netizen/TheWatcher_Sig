package com.example.thewatcher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One monitoring session (roughly one hotspot-on period).
 * Opened when monitoring starts, closed when it stops.
 */
@Entity(tableName = "hotspot_sessions")
data class HotspotSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val startedMs: Long = 0L,
    val endedMs: Long? = null,
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    val deviceCount: Int = 0,
    val isActive: Boolean = true
)
