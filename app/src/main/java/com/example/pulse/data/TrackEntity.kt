package com.example.pulse.UI

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val uri: String,
    val title: String,
    val artist: String,
    val duration: Long
)
