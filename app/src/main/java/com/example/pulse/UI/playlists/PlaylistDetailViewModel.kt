package com.example.pulse.ui.playlists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.Playlist
import com.example.pulse.data.PlaylistRepository
import com.example.pulse.data.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val playlistId: Long =
        savedStateHandle[ARG_PLAYLIST_ID] ?: error("playlistId не передан")

    private val repository = PlaylistRepository(application)

    val playlist: StateFlow<Playlist?> = repository.playlists
        .map { list -> list.firstOrNull { it.id == playlistId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tracks: StateFlow<List<Track>?> = repository.tracksOf(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            repository.removeTrack(playlistId, trackId)
        }
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
    }
}