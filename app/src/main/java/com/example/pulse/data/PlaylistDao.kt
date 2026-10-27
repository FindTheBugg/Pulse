package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val trackCount: Int
)

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLinks(links: List<PlaylistTrackEntity>): List<Long>

    @Query("SELECT playlistId FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getPlaylistIdsOfTrack(trackId: String): List<Long>

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackIdsOfPlaylist(playlistId: Long): List<String>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query(
        "SELECT p.id AS id, p.name AS name, p.createdAt AS createdAt, " +
                "COUNT(pt.trackId) AS trackCount " +
                "FROM playlists p " +
                "LEFT JOIN playlist_tracks pt ON pt.playlistId = p.id " +
                "GROUP BY p.id " +
                "ORDER BY p.createdAt DESC"
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(link: PlaylistTrackEntity): Long

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query(
        "SELECT t.* FROM tracks t " +
                "INNER JOIN playlist_tracks pt ON pt.trackId = t.id " +
                "WHERE pt.playlistId = :playlistId " +
                "ORDER BY pt.addedAt ASC"
    )
    fun observeTracksOfPlaylist(playlistId: Long): Flow<List<TrackEntity>>
}
