package com.example.pulse.data

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.util.Log

class MusicPlayerManager(context: Context) {

    private val player = ExoPlayer.Builder(context.applicationContext).build()

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
    }

    fun resume() {
        player.play()
    }

    fun release() {
        player.release()
    }
}
