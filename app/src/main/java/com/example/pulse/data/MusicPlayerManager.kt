package com.example.pulse.data

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerManager(context: Context) {

    private val player = ExoPlayer.Builder(context.applicationContext).build()
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    private var queue: List<Track> = emptyList()
    private var currentTrackIndex: Int = -1
    val hasTrack: Boolean
        get() = player.currentMediaItem != null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    init {
        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlayState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) startProgressUpdates() else stopProgressUpdates()
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

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updatePlayState()
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlayState() {
        val duration = if (player.duration == C.TIME_UNSET) {
            _currentTrack.value?.duration ?: 0L
        } else {
            player.duration
        }

        _playerState.value = PlayerState(
            isPlaying = player.isPlaying,
            playWhenReady = player.playWhenReady,
            isEnded = player.playbackState == Player.STATE_ENDED,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(0L)
        )
    }

    // Запустить трек и сделать newQueue текущей очередью
    fun playTrack(track: Track, newQueue: List<Track>) {
        queue = newQueue
        currentTrackIndex = newQueue.indexOfFirst { it.id == track.id }
        startTrack(track)
    }

    // Заменить очередь, не прерывая музыку
    fun setQueue(newQueue: List<Track>) {
        queue = newQueue
        currentTrackIndex = newQueue.indexOfFirst { it.id == _currentTrack.value?.id }
    }

    // Библиотека изменилась: убрать из очереди удалённые треки
    fun onLibraryChanged(library: List<Track>) {
        val ids = library.map { it.id }.toSet()
        val current = _currentTrack.value

        if (current != null && current.id !in ids) {
            stop()
            return
        }
        queue = queue.filter { it.id in ids }
        currentTrackIndex = queue.indexOfFirst { it.id == current?.id }
    }

    private fun startTrack(track: Track) {
        _currentTrack.value = track
        player.setMediaItem(MediaItem.fromUri(track.uri))
        player.prepare()
        player.play()
    }

    fun playNext() {
        if (queue.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex in queue.indices) {
            (currentTrackIndex + 1) % queue.size
        } else {
            0
        }
        startTrack(queue[currentTrackIndex])
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        currentTrackIndex = if (currentTrackIndex in queue.indices) {
            (currentTrackIndex - 1 + queue.size) % queue.size
        } else {
            queue.lastIndex
        }
        startTrack(queue[currentTrackIndex])
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        updatePlayState()
    }

    fun replay() {
        player.seekTo(0)
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        queue = emptyList()
        currentTrackIndex = -1
        _currentTrack.value = null
        updatePlayState()
    }

    fun release() {
        stopProgressUpdates()
        scope.cancel()
        player.release()
    }
}