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
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    private var currentTrackIndex: Int = -1
    val hasTrack: Boolean
        get() = player.currentMediaItem != null

    init {
        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(
                mediaItem: MediaItem?,
                reason: Int
            ) {
                updatePlayState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayState()
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                updatePlayState()
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayState()

                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
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
        val currentUri = player.currentMediaItem
            ?.localConfiguration
            ?.uri
        val currentTrackStillExists = newTracks.any {
            it.uri == currentUri
        }
        tracks = newTracks
        if (currentUri != null && !currentTrackStillExists) {
            stop()
            currentTrackIndex = -1
            return
        }
        currentTrackIndex = tracks.indexOfFirst {
            it.uri == currentUri
        }
    }

    fun replay() {
        player.seekTo(0)
        player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        currentTrackIndex = -1
        _currentTrack.value = null
    }

    fun play(uri: Uri) {
        currentTrackIndex = tracks.indexOfFirst {
            it.uri == uri
        }
        _currentTrack.value = tracks.getOrNull(currentTrackIndex)
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun playNext() {
        if (tracks.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex in tracks.indices) {
            (currentTrackIndex + 1) % tracks.size
        } else {
            0
        }
        val nextTrack = tracks[currentTrackIndex]
        _currentTrack.value = nextTrack
        player.setMediaItem(MediaItem.fromUri(nextTrack.uri))
        player.prepare()
        player.play()
    }

    fun playPrevious() {
        if (tracks.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex in tracks.indices) {
            (currentTrackIndex - 1 + tracks.size) % tracks.size
        } else {
            tracks.lastIndex
        }
        val previousTrack = tracks[currentTrackIndex]
        _currentTrack.value = previousTrack
        player.setMediaItem(MediaItem.fromUri(previousTrack.uri))
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun release() {
        player.release()
    }
}
