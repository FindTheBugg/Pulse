package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

//// Трек вместе со счётчиком. @Embedded значит «возьми столбцы tracks целиком»
//data class TrackPlayRow(
//    @Embedded val track: TrackEntity,
//    val playCount: Int
//)

@Dao
interface TrackStatsDao {

    @Query("INSERT OR IGNORE INTO track_stats (trackId, playCount, lastPlayedAt) VALUES (:trackId, 0, 0)")
    suspend fun ensureRow(trackId: String)

    @Query("UPDATE track_stats SET playCount = playCount + 1, lastPlayedAt = :playedAt WHERE trackId = :trackId")
    suspend fun increment(trackId: String, playedAt: Long)

    @Query(
        "SELECT t.*, s.playCount AS playCount FROM tracks t " +
                "INNER JOIN track_stats s ON s.trackId = t.id " +
                "WHERE s.playCount > 0 " +
                "ORDER BY s.lastPlayedAt DESC LIMIT :limit"
    )
    fun observeRecent(limit: Int): Flow<List<TrackPlayRow>>

    @Query(
        "SELECT t.*, s.playCount AS playCount FROM tracks t " +
                "INNER JOIN track_stats s ON s.trackId = t.id " +
                "WHERE s.playCount > 0 " +
                "ORDER BY s.playCount DESC, s.lastPlayedAt DESC LIMIT :limit"
    )
    fun observeMostPlayed(limit: Int): Flow<List<TrackPlayRow>>
}