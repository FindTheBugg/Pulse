package com.example.pulse.UI.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.MusicPlayerManager
import com.example.pulse.data.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val musicPlayerManager = MusicPlayerManager(application)

    val currentTrack: StateFlow<Track?> =
        musicPlayerManager.currentTrack.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    override fun onCleared() {
        musicPlayerManager.release()
        super.onCleared()
    }
}