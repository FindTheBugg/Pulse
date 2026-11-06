package com.example.pulse.ui.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulse.data.PlayStatsRepository
import com.example.pulse.data.PlayedTrack
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OverviewState(
    val recent: List<PlayedTrack>,
    val popular: List<PlayedTrack>
)

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlayStatsRepository(application)

    val state: StateFlow<OverviewState?> =
        combine(repository.recent(), repository.mostPlayed()) { recent, popular ->
            OverviewState(recent, popular)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}