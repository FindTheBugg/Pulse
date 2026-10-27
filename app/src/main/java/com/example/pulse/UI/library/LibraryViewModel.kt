package com.example.pulse.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.Track
import com.example.pulse.data.TrackRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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
}