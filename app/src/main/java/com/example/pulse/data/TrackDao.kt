package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pulse.data.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<TrackEntity>

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
}
