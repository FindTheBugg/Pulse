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

    fun onAudioSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val added = repository.addTrack(uri)
                if (!added) {
                    _messages.send("Трек уже в библиотеке")
                }
            } catch (e: Exception) {
                _messages.send("Не удалось прочитать или сохранить аудиофайл")
            }
        }
    }
}