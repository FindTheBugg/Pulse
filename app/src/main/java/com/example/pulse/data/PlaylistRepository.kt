package com.example.pulse.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepository(context: Context) {

    private val playlistDao = PulseDatabase.getDatabase(context.applicationContext).playlistDao()

    val playlists: Flow<List<Playlist>> = playlistDao.observePlaylists()
        .map { list ->
            list.map { Playlist(it.id, it.name, it.createdAt, it.trackCount) }
        }

    fun tracksOf(playlistId: Long): Flow<List<Track>> =
        playlistDao.observeTracksOfPlaylist(playlistId)
            .map { entities -> entities.map { it.toTrack() } }

    suspend fun createPlaylist(name: String): Boolean = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return@withContext false
        playlistDao.insertPlaylist(
            PlaylistEntity(name = cleanName, createdAt = System.currentTimeMillis())
        )
        true
    }

    suspend fun renamePlaylist(playlistId: Long, name: String): Boolean =
        withContext(Dispatchers.IO) {
            val cleanName = name.trim()
            if (cleanName.isEmpty()) return@withContext false
            playlistDao.renamePlaylist(playlistId, cleanName)
            true
        }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addTrack(playlistId: Long, trackId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                playlistDao.addTrackToPlaylist(
                    PlaylistTrackEntity(
                        playlistId = playlistId,
                        trackId = trackId,
                        addedAt = System.currentTimeMillis()
                    )
                ) != -1L
            } catch (e: SQLiteConstraintException) {
                false
            }
        }

    suspend fun removeTrack(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }
}