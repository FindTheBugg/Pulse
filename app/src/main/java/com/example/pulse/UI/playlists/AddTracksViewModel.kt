package com.example.pulse.ui.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.PlaybackIssues
import com.example.pulse.data.PlaylistRepository
import com.example.pulse.data.TrackRepository
import com.example.pulse.ui.common.ChoiceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTracksViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    val playlistId: Long =
        savedStateHandle[ARG_PLAYLIST_ID] ?: error("playlistId не передан")

    private val trackRepository = TrackRepository(application)
    private val playlistRepository = PlaylistRepository(application)

    private val alreadyIn = MutableStateFlow<Set<String>?>(null)

    init {
        viewModelScope.launch {
            alreadyIn.value = playlistRepository.trackIdsOf(playlistId).toSet()
        }
    }

    val choices: StateFlow<List<ChoiceItem>?> =
        combine(
            trackRepository.tracks,
            alreadyIn,
            PlaybackIssues.unavailable
        ) { tracks, already, unavailable ->
            if (already == null) {
                null
            } else {
                tracks.map {
                    val inPlaylist = it.id in already
                    val broken = it.id in unavailable
                    ChoiceItem(
                        id = it.id,
                        title = it.title,
                        subtitle = when {
                            inPlaylist -> "Уже в плейлисте"
                            broken -> "Файл недоступен"
                            else -> it.artist
                        },
                        alreadyAdded = inPlaylist,
                        unavailable = broken
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun confirm(selectedIds: Set<String>): Int {
        val allowed = selectedIds - PlaybackIssues.unavailable.value
        if (allowed.isEmpty()) return 0
        return playlistRepository.addTracksToPlaylist(playlistId, allowed.toList())
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
    }
}