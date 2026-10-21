package com.example.pulse.UI.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.pulse.data.MusicPlayerManager
import com.example.pulse.data.PlayerState
import com.example.pulse.data.Track
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val musicPlayerManager = MusicPlayerManager(application)

    val playerState: StateFlow<PlayerState> = musicPlayerManager.playerState
    val currentTrack: StateFlow<Track?> = musicPlayerManager.currentTrack

    val hasTrack: Boolean
        get() = musicPlayerManager.hasTrack

    fun onSeekTo(positionMs: Long) {
        musicPlayerManager.seekTo(positionMs)
    }
    fun setPlaylist(tracks: List<Track>) {
        musicPlayerManager.setPlaylist(tracks)
    }

    fun onTrackClick(track: Track) {
        musicPlayerManager.play(track.uri)
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

    fun onPreviousClick() {
        musicPlayerManager.playPrevious()
    }

    fun stop() {
        musicPlayerManager.stop()
    }

    override fun onCleared() {
        musicPlayerManager.release()
        super.onCleared()
    }
}