package com.example.pulse.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException

class PlayStatsRepository(context: Context) {

    private val dao = PulseDatabase.getDatabase(context.applicationContext).statsDao()

    suspend fun recordPlay(trackId: String) {
        try {
            dao.ensureRow(trackId)
            dao.increment(trackId, System.currentTimeMillis())
        } catch (e: SQLiteConstraintException) {
        }
    }
}