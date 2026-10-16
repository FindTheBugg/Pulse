package com.example.pulse.data

import android.net.Uri

data class Track (
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long
)