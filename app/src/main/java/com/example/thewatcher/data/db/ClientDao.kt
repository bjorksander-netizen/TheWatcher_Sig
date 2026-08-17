package com.example.thewatcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thewatcher.data.model.ConnectedClient
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(client: ConnectedClient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(clients: List<ConnectedClient>)

    @Query("SELECT * FROM clients WHERE lastSeenMs > :sinceMs ORDER BY lastSeenMs DESC")
    fun getActiveClients(sinceMs: Long): Flow<List<ConnectedClient>>

    @Query("SELECT * FROM clients")
    suspend fun getAll(): List<ConnectedClient>

    @Query("DELETE FROM clients")
    suspend fun clear()
}
