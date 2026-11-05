package com.example.pulse.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_stats",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackStatsEntity(
    @PrimaryKey
    val trackId: String,
    val playCount: Int,
    val lastPlayedAt: Long
)