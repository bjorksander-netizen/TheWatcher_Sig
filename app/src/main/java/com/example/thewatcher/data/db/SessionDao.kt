package com.example.thewatcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage
import kotlinx.coroutines.flow.Flow

data class SessionWithDevices(
    val session: HotspotSession,
    val devices: List<SessionDeviceUsage>
)

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HotspotSession): Long

    @Query("UPDATE hotspot_sessions SET endedMs = :endedMs, totalRxBytes = :rx, totalTxBytes = :tx, deviceCount = :count, isActive = 0 WHERE id = :id")
    suspend fun closeSession(id: Long, endedMs: Long, rx: Long, tx: Long, count: Int)

    @Query("UPDATE hotspot_sessions SET totalRxBytes = :rx, totalTxBytes = :tx, deviceCount = :count WHERE id = :id")
    suspend fun updateTotals(id: Long, rx: Long, tx: Long, count: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeviceUsage(usage: SessionDeviceUsage)

    @Query("SELECT * FROM hotspot_sessions WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getOpenSession(): HotspotSession?

    @Query("SELECT * FROM hotspot_sessions ORDER BY startedMs DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<HotspotSession>>

    @Query("SELECT * FROM session_device_usage WHERE sessionId = :sessionId")
    suspend fun getDevicesForSession(sessionId: Long): List<SessionDeviceUsage>

    @Transaction
    suspend fun getSessionWithDevices(sessionId: Long): SessionWithDevices? {
        val session = getSessionById(sessionId) ?: return null
        val devices = getDevicesForSession(sessionId)
        return SessionWithDevices(session, devices)
    }

    @Query("SELECT * FROM hotspot_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): HotspotSession?
}
