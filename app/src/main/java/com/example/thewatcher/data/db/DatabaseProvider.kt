package com.example.thewatcher.data.db

import android.content.Context
import androidx.room.Room

/**
 * Lazily-built singleton Room database holder.
 */
object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
