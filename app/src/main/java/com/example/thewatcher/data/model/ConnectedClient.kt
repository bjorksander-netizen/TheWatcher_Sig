package com.example.thewatcher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks a currently (or recently) connected client by MAC address.
 * firstSeenMs/lastSeenMs drive the connection-duration used for per-device estimation.
 */
@Entity(tableName = "clients")
data class ConnectedClient(
    @PrimaryKey val macAddress: String,
    val ipAddress: String = "",
    val hostname: String = "",
    val firstSeenMs: Long = 0L,
    val lastSeenMs: Long = 0L,
    val estRxBytes: Long = 0L,
    val estTxBytes: Long = 0L
)
