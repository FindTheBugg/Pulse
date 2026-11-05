package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TrackStatsDao {

    @Query("INSERT OR IGNORE INTO track_stats (trackId, playCount, lastPlayedAt) VALUES (:trackId, 0, 0)")
    suspend fun ensureRow(trackId: String)

    @Query("UPDATE track_stats SET playCount = playCount + 1, lastPlayedAt = :playedAt WHERE trackId = :trackId")
    suspend fun increment(trackId: String, playedAt: Long)
}