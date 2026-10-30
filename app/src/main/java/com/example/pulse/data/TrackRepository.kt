package com.example.pulse.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

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

    suspend fun removeUnavailable(): Int = withContext(Dispatchers.IO) {
        val missing: List<TrackEntity> = trackDao.getAllTracks().filter {
            ensureActive()
            !isAccessible(Uri.parse(it.uri))
        }
        if (missing.isEmpty()) return@withContext 0

        missing.forEach { chunk ->
            trackDao.deleteByIds(listOf(chunk.id))
        }

        missing.forEach { entity ->
            PlaybackIssues.markAvailable(entity.id)
            try {
                appContext.contentResolver.releasePersistableUriPermission(
                    Uri.parse(entity.uri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
        }
        missing.size
    }

    private fun isAccessible(uri: Uri): Boolean = try {
        appContext.contentResolver.openAssetFileDescriptor(uri, "r")?.close()
        true
    } catch (e: FileNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    } catch (e: IOException) {
        true
    }

//    private fun isAccessible(uri: Uri): Boolean {
//        return try {
//            appContext.contentResolver.query(
//                uri,
//                arrayOf(MediaStore.Audio.Media._ID),
//                null,
//                null,
//                null
//            )?.use { cursor ->
//                cursor.moveToFirst()
//            } ?: false
//        } catch (e: SecurityException) {
//            false
//        } catch (e: IllegalArgumentException) {
//            false
//        }
//    }
}