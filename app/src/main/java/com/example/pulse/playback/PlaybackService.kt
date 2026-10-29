package com.example.pulse.playback

import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.pulse.MainActivity
import com.example.pulse.data.PlaybackIssues
import com.example.pulse.data.PlaybackStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var saveJob: Job? = null
    private var consecutiveErrors = 0
    private lateinit var playbackStore: PlaybackStore
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .apply { repeatMode = Player.REPEAT_MODE_ALL }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        playbackStore = PlaybackStore(this)

        player.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                saveJob?.cancel()
                if (isPlaying) {
                    consecutiveErrors = 0
                    player.currentMediaItem?.mediaId?.let { PlaybackIssues.markAvailable(it) }

                    saveJob = serviceScope.launch {
                        while (isActive) {
                            delay(5_000)
                            saveProgress(player)
                        }
                    }
                } else {
                    saveProgress(player)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                saveProgress(player)
            }

            override fun onPlayerError(error: PlaybackException) {
                player.currentMediaItem?.mediaId?.let { PlaybackIssues.markUnavailable(it) }
                consecutiveErrors++

                if (consecutiveErrors >= player.mediaItemCount) {
                    consecutiveErrors = 0
                    Toast.makeText(
                        this@PlaybackService,
                        "Не удалось воспроизвести треки. Проверь, что файлы на месте",
                        Toast.LENGTH_LONG
                    ).show()
                    player.stop()
                    return
                }

                val fileProblem = error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ||
                        error.errorCode == PlaybackException.ERROR_CODE_IO_NO_PERMISSION
                Toast.makeText(
                    this@PlaybackService,
                    if (fileProblem) "Файл недоступен, пропускаю" else "Не удалось воспроизвести трек, пропускаю",
                    Toast.LENGTH_SHORT
                ).show()

                player.seekToNextMediaItem()
                player.prepare()
                player.play()
            }
        })

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent)
            .build()
    }

    private fun saveProgress(player: Player) {
        val id = player.currentMediaItem?.mediaId ?: return
        val position = player.currentPosition.coerceAtLeast(0L)
        serviceScope.launch { playbackStore.save(id, position) }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        saveJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}