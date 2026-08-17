package com.example.thewatcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thewatcher.data.model.DailyTotal

@Dao
interface DailyTotalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(daily: DailyTotal)

    @Query("SELECT * FROM daily_totals WHERE dateText = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyTotal?

    @Query("SELECT * FROM daily_totals ORDER BY dateText DESC LIMIT 7")
    suspend fun getLast7(): List<DailyTotal>
}
