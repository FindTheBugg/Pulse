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
        })
    }

    private fun updatePlayState() {
        _playerState.value = PlayerState(
            isPlaying = player.isPlaying,
            playWhenReady = player.playWhenReady
        )
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun play(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
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
