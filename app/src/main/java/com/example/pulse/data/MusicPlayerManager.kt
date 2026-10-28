package com.example.pulse.data

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.pulse.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
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
import android.net.Uri

class MusicPlayerManager(context: Context) {

    private val appContext = context.applicationContext

    // Пульт к плееру в сервисе. null, пока соединение не установлено
    private var controller: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>

    // Библиотека, пришедшая до установки соединения
    private var pendingLibrary: List<Track>? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val hasTrack: Boolean
        get() = controller?.currentMediaItem != null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = refresh()

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
            refresh()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = refresh()

        override fun onPlaybackStateChanged(playbackState: Int) = refresh()
    }

    init {
        val token = SessionToken(
            appContext,
            ComponentName(appContext, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture.addListener({
            val connected = try {
                controllerFuture.get()
            } catch (e: Exception) {
                return@addListener
            }
            controller = connected
            connected.addListener(listener)

            pendingLibrary?.let { onLibraryChanged(it) }
            pendingLibrary = null

            if (connected.isPlaying) startProgressUpdates()
            refresh()   // сервис уже мог играть: сразу показываем его трек
        }, ContextCompat.getMainExecutor(appContext))
    }

    // ---------- Состояние ----------

    private fun refresh() {
        val c = controller ?: return
        val track = c.currentMediaItem?.toTrack()
        _currentTrack.value = track

        val duration = if (c.duration == C.TIME_UNSET) track?.duration ?: 0L else c.duration

        _playerState.value = PlayerState(
            isPlaying = c.isPlaying,
            playWhenReady = c.playWhenReady,
            isEnded = c.playbackState == Player.STATE_ENDED,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(0L)
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                refresh()
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    // ---------- Очередь ----------

    // Запустить трек и сделать newQueue текущей очередью
    fun playTrack(track: Track, newQueue: List<Track>) {
        val c = controller ?: return
        val queue = if (newQueue.any { it.id == track.id }) newQueue else listOf(track)
        val index = queue.indexOfFirst { it.id == track.id }

        c.setMediaItems(queue.map { it.toMediaItem() }, index, 0L)
        c.prepare()
        c.play()
    }

    // Заменить очередь, не прерывая музыку
    fun setQueue(newQueue: List<Track>) {
        syncQueue(newQueue)
    }

    // Библиотека изменилась: убрать из очереди удалённые треки
    fun onLibraryChanged(library: List<Track>) {
        val c = controller
        if (c == null) {
            pendingLibrary = library
            return
        }
        val ids = library.map { it.id }.toSet()
        val currentId = c.currentMediaItem?.mediaId

        if (currentId != null && currentId !in ids) {
            stop()
            return
        }
        syncQueue(currentQueue().filter { it.id in ids })
    }

    private fun currentQueue(): List<Track> {
        val c = controller ?: return emptyList()
        return (0 until c.mediaItemCount).mapNotNull { c.getMediaItemAt(it).toTrack() }
    }

    // Подменяем элементы до и после текущего, сам играющий трек не трогаем
    private fun syncQueue(newQueue: List<Track>) {
        val c = controller ?: return
        val currentId = c.currentMediaItem?.mediaId ?: return
        if (currentQueue().map { it.id } == newQueue.map { it.id }) return

        val index = newQueue.indexOfFirst { it.id == currentId }
        if (index == -1) return

        val currentIndex = c.currentMediaItemIndex
        if (currentIndex + 1 < c.mediaItemCount) {
            c.removeMediaItems(currentIndex + 1, c.mediaItemCount)
        }
        c.addMediaItems(newQueue.drop(index + 1).map { it.toMediaItem() })

        if (currentIndex > 0) c.removeMediaItems(0, currentIndex)
        c.addMediaItems(0, newQueue.take(index).map { it.toMediaItem() })
    }

    // ---------- Управление ----------

    fun playNext() {
        val c = controller ?: return
        c.seekToNextMediaItem()
        c.play()
    }

    fun playPrevious() {
        val c = controller ?: return
        c.seekToPreviousMediaItem()
        c.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        refresh()
    }

    fun replay() {
        controller?.seekTo(0)
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun resume() {
        controller?.play()
    }

    fun stop() {
        controller?.stop()
        controller?.clearMediaItems()
        refresh()
    }

    // Отпускаем только пульт. Плеер в сервисе продолжает играть
    fun release() {
        stopProgressUpdates()
        scope.cancel()
        controller?.removeListener(listener)
        MediaController.releaseFuture(controllerFuture)
        controller = null
    }

    // ---------- Track <-> MediaItem ----------

    private fun Track.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setExtras(bundleOf(KEY_URI to uri.toString(), KEY_DURATION to duration))
                    .build()
            )
            .build()

    private fun MediaItem.toTrack(): Track? {
        val extras = mediaMetadata.extras ?: return null
        val uriString = extras.getString(KEY_URI) ?: return null
        return Track(
            id = mediaId,
            uri = Uri.parse(uriString),
            title = mediaMetadata.title?.toString().orEmpty(),
            artist = mediaMetadata.artist?.toString().orEmpty(),
            duration = extras.getLong(KEY_DURATION)
        )
    }

    private companion object {
        const val KEY_URI = "uri"
        const val KEY_DURATION = "duration"
    }
}