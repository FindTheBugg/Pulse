package com.example.pulse.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TrackRepository(context: Context) {

    private val appContext = context.applicationContext
    private val trackDao = PulseDatabase.getDatabase(appContext).trackDao()
    private val metadataReader = TrackMetadataReader(appContext)

    val tracks: Flow<List<Track>> = trackDao.observeAllTracks()
        .map { entities -> entities.map { it.toTrack() } }

    suspend fun addTrack(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val track = metadataReader.readTrack(uri)
        trackDao.insertTrack(track.toEntity()) != -1L
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val savedTracks = trackDao.getAllTracks()

        savedTracks
            .map { Uri.parse(it.uri) }
            .distinct()
            .forEach { uri ->
                try {
                    appContext.contentResolver.releasePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { }
            }

        trackDao.deleteAllTracks()
    }
}