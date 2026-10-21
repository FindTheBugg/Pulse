package com.example.pulse.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.pulse.UI.TrackMetadataReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepository(context: Context) {

    private val appContext = context.applicationContext
    private val trackDao = PulseDatabase.getDatabase(appContext).trackDao()
    private val metadataReader = TrackMetadataReader(appContext)

    suspend fun getAllTracks(): List<Track> = withContext(Dispatchers.IO) {
        trackDao.getAllTracks().map { it.toTrack() }
    }

    suspend fun addTrack(uri: Uri) = withContext(Dispatchers.IO) {
        val track = metadataReader.readTrack(uri)
        trackDao.insertTrack(track.toEntity())
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