package com.example.pulse.ui.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.PlaylistRepository
import com.example.pulse.ui.common.ChoiceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddToPlaylistViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val trackId: String =
        savedStateHandle[ARG_TRACK_ID] ?: error("trackId не передан")

    private val repository = PlaylistRepository(application)

    private val alreadyIn = MutableStateFlow<Set<Long>?>(null)

    init {
        viewModelScope.launch {
            alreadyIn.value = repository.playlistIdsOfTrack(trackId).toSet()
        }
    }

    val choices: StateFlow<List<ChoiceItem>?> =
        combine(repository.playlists, alreadyIn) { playlists, already ->
            if (already == null) {
                null
            } else {
                playlists.map {
                    ChoiceItem(
                        id = it.id.toString(),
                        title = it.name,
                        subtitle = if (it.id in already) "Трек уже здесь" else "Треков: ${it.trackCount}",
                        alreadyAdded = it.id in already
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun confirm(selectedIds: Set<String>): Int =
        repository.addTrackToPlaylists(trackId, selectedIds.map { it.toLong() })

    companion object {
        const val ARG_TRACK_ID = "trackId"
    }
}