package com.example.pulse.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PlayedTrack(val track: Track, val playCount: Int)

class PlayStatsRepository(context: Context) {

    private val dao = PulseDatabase.getDatabase(context.applicationContext).statsDao()

    suspend fun recordPlay(trackId: String) {
        try {
            dao.ensureRow(trackId)
            dao.increment(trackId, System.currentTimeMillis())
        } catch (e: SQLiteConstraintException) {
        }
    }
    fun recent(limit: Int = 10): Flow<List<PlayedTrack>> =
        dao.observeRecent(limit).map { rows ->
            rows.map { PlayedTrack(it.track.toTrack(), it.playCount) }
        }

    fun mostPlayed(limit: Int = 10): Flow<List<PlayedTrack>> =
        dao.observeMostPlayed(limit).map { rows ->
            rows.map { PlayedTrack(it.track.toTrack(), it.playCount) }
        }
}