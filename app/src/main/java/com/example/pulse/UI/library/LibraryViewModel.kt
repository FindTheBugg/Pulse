package com.example.pulse.ui.library

import android.app.Application
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.Track
import com.example.pulse.data.TrackRepository
import com.example.pulse.ui.common.tracksText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


data class LibraryUiState(
    val tracks: List<Track>,
    val totalCount: Int,
    val filtering: Boolean
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrackRepository(application)

    val tracks: StateFlow<List<Track>?> = repository.tracks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _sort = MutableStateFlow(SortOrder.TITLE)
    val sort: StateFlow<SortOrder> = _sort.asStateFlow()

    val state: StateFlow<LibraryUiState?> =
        combine(tracks, _query, _sort) { all, query, sort ->
            if (all == null) {
                null
            } else {
                val q = query.trim()
                val filtered = if (q.isEmpty()) all else all.filter {
                    it.title.contains(q, ignoreCase = true) ||
                            it.artist.contains(q, ignoreCase = true)
                }
                LibraryUiState(sortTracks(filtered, sort), all.size, q.isNotEmpty())
            }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setQuery(text: String) {
        _query.value = text
    }

    fun setSort(order: SortOrder) {
        _sort.value = order
    }

    private fun sortTracks(list: List<Track>, order: SortOrder): List<Track> = when (order) {
        SortOrder.TITLE -> list.sortedWith(
            compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
        )
        SortOrder.ARTIST -> list.sortedWith(
            compareBy<Track, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        )
        SortOrder.DURATION -> list.sortedBy { it.duration }
    }



    fun onAudiosSelected(uris: List<Uri>) {
        viewModelScope.launch {
            var added = 0
            var duplicates = 0
            var failed = 0

            for (uri in uris) {
                try {
                    if (repository.addTrack(uri)) added++ else duplicates++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failed++
                }
            }

            val parts = mutableListOf<String>()
            if (added > 0) parts += "добавлено: $added"
            if (duplicates > 0) parts += "уже были: $duplicates"
            if (failed > 0) parts += "ошибок: $failed"
            _messages.send(parts.joinToString(", ").replaceFirstChar { it.uppercase() })
        }
    }
    fun checkLibrary(manual: Boolean) {
        if (_isRefreshing.value) {
            if (manual) _messages.trySend("Проверка уже идёт")
            return
        }
        _isRefreshing.value = true

        viewModelScope.launch {
            Log.d("Pulse", "checkLibrary() called")
            try {
                val removed = repository.removeUnavailable()
                when {
                    removed > 0 ->
                        _messages.send("С устройства удалено ${tracksText(removed)}, база обновлена")
                    manual ->
                        _messages.send("База треков обновлена и находится в актуальном состоянии")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (manual) _messages.send("Не удалось проверить библиотеку")
            } finally {
                _isRefreshing.value = false
            }
            Log.d("Pulse", "checkLibrary() called")
        }
    }

}