package com.example.pulse.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object PlaybackIssues {

    private val _unavailable = MutableStateFlow<Set<String>>(emptySet())
    val unavailable: StateFlow<Set<String>> = _unavailable.asStateFlow()

    fun markUnavailable(trackId: String) = _unavailable.update { it + trackId }

    fun markAvailable(trackId: String) = _unavailable.update { it - trackId }
}