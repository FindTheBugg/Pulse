package com.example.pulse.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.Player

class MusicPlayerManager(context: Context) {

    private val player = ExoPlayer.Builder(context.applicationContext).build()
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    private var tracks: List<Track> = emptyList()
    private var currentTrackIndex: Int = -1
    val hasTrack: Boolean
        get() = player.currentMediaItem != null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayState()
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                updatePlayState()
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayState()
            }
        })
    }

    private fun updatePlayState() {
        _playerState.value = PlayerState(
            isPlaying = player.isPlaying,
            playWhenReady = player.playWhenReady,
            isEnded = player.playbackState == Player.STATE_ENDED
        )
    }

    fun setPlaylist(newTracks: List<Track>) {
        tracks = newTracks
    }

    fun replay() {
        player.seekTo(0)
        player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun play(uri: Uri) {
        currentTrackIndex = tracks.indexOfFirst {
            it.uri == uri
        }
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun playNext() {
        if (tracks.isEmpty()) return

        val nextIndex = currentTrackIndex + 1

        if (nextIndex < tracks.size) {
            currentTrackIndex = nextIndex

            val nextTrack = tracks[currentTrackIndex]
            val mediaItem = MediaItem.fromUri(nextTrack.uri)

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun playPrevious() {
        if (tracks.isEmpty()) return

        val previousIndex = currentTrackIndex - 1

        if (previousIndex >= 0) {
            currentTrackIndex = previousIndex

            val previousTrack = tracks[currentTrackIndex]
            val mediaItem = MediaItem.fromUri(previousTrack.uri)

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun pause() {
        player.pause()
        android.util.Log.d(
            "PlayerPulse",
            "${playerState.value}"
        )
    }

    fun resume() {
        player.play()
    }

    fun release() {
        player.release()
    }
}
