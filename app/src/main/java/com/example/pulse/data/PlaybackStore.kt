package com.example.pulse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.playbackDataStore by preferencesDataStore(name = "playback")

class PlaybackStore(context: Context) {

    private val store = context.applicationContext.playbackDataStore

    suspend fun save(trackId: String, positionMs: Long) {
        store.edit {
            it[KEY_TRACK_ID] = trackId
            it[KEY_POSITION] = positionMs
        }
    }

    suspend fun read(): Pair<String, Long>? {
        val prefs = store.data.first()
        val id = prefs[KEY_TRACK_ID] ?: return null
        return id to (prefs[KEY_POSITION] ?: 0L)
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }

    private companion object {
        val KEY_TRACK_ID = stringPreferencesKey("track_id")
        val KEY_POSITION = longPreferencesKey("position_ms")
    }
}