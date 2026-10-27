package com.example.pulse.ui.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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

    private val playlistId: Long =
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
        combine(trackRepository.tracks, alreadyIn) { tracks, already ->
            if (already == null) {
                null
            } else {
                tracks.map {
                    ChoiceItem(
                        id = it.id,
                        title = it.title,
                        subtitle = if (it.id in already) "Уже в плейлисте" else it.artist,
                        alreadyAdded = it.id in already
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun confirm(selectedIds: Set<String>): Int =
        playlistRepository.addTracksToPlaylist(playlistId, selectedIds.toList())

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
    }
}