package com.example.thewatcher.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.thewatcher.data.model.ConnectedClient
import com.example.thewatcher.data.model.DailyTotal
import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage

@Database(
    entities = [
        ConnectedClient::class,
        DailyTotal::class,
        HotspotSession::class,
        SessionDeviceUsage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun dailyTotalDao(): DailyTotalDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val DATABASE_NAME = "thewatcher.db"
    }
}
