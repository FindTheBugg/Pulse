package com.example.pulse.ui.player

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.MusicPlayerManager
import com.example.pulse.data.PlaybackIssues
import com.example.pulse.data.PlaybackStore
import com.example.pulse.data.PlayerState
import com.example.pulse.data.Track
import com.example.pulse.data.TrackRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val musicPlayerManager = MusicPlayerManager(application)
    private val repository = TrackRepository(application)
    private val playbackStore = PlaybackStore(application)
    val playerState: StateFlow<PlayerState> = musicPlayerManager.playerState
    val currentTrack: StateFlow<Track?> = musicPlayerManager.currentTrack
    private var libraryTracks: List<Track> = emptyList()
    private var queueIsLibrary = false
    val unavailableIds: StateFlow<Set<String>> = PlaybackIssues.unavailable

    val hasTrack: Boolean
        get() = musicPlayerManager.hasTrack

    init {
        viewModelScope.launch {
            repository.tracks.collect { tracks ->
                libraryTracks = tracks
                musicPlayerManager.onLibraryChanged(tracks)
                if (queueIsLibrary) {
                    musicPlayerManager.setQueue(tracks)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            musicPlayerManager.isConnected.first { it }
            val library = repository.tracks.first()

            if (musicPlayerManager.hasTrack) return@launch

            val (savedId, savedPosition) = playbackStore.read() ?: return@launch
            val track = library.firstOrNull { it.id == savedId }
            if (track == null) {
                playbackStore.clear()
                return@launch
            }

            val position = if (savedPosition > track.duration - 3_000) 0L else savedPosition
            queueIsLibrary = true
            musicPlayerManager.restoreTrack(track, library, position)
        }
    }

    fun onTrackClick(track: Track) {
        queueIsLibrary = true
        musicPlayerManager.playTrack(track, libraryTracks)
    }

    fun onPlaylistTrackClick(track: Track, playlistTracks: List<Track>) {
        queueIsLibrary = false
        musicPlayerManager.playTrack(track, playlistTracks)
    }

    fun onPlayPlaylist(playlistTracks: List<Track>) {
        val first = playlistTracks.firstOrNull() ?: return
        onPlaylistTrackClick(first, playlistTracks)
    }

    fun onPlayPauseClick() {
        if (!hasTrack) return
        val state = playerState.value
        when {
            state.isPlaying -> musicPlayerManager.pause()
            state.isEnded -> musicPlayerManager.replay()
            else -> musicPlayerManager.resume()
        }
    }

    fun onNextClick() {
        musicPlayerManager.playNext()
    }

    fun onRepeatClick() {
        musicPlayerManager.cycleRepeatMode()
    }

    fun onShuffleClick() {
        musicPlayerManager.toggleShuffle()
    }

    fun onPreviousClick() {
        musicPlayerManager.playPrevious()
    }

    fun onSeekTo(positionMs: Long) {
        musicPlayerManager.seekTo(positionMs)
    }

    fun stop() {
        musicPlayerManager.stop()
    }

    override fun onCleared() {
        musicPlayerManager.release()
        super.onCleared()
    }
}