package com.example.thewatcher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Aggregate tethered traffic per calendar day (yyyy-MM-dd).
 */
@Entity(tableName = "daily_totals")
data class DailyTotal(
    @PrimaryKey val dateText: String,
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L
)
