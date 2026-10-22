package com.example.pulse.data

import android.net.Uri
import com.example.pulse.data.TrackEntity

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = id,
        uri = uri.toString(),
        title = title,
        artist = artist,
        duration = duration
    )
}

fun TrackEntity.toTrack(): Track {
    return Track(
        id = id,
        uri = Uri.parse(uri),
        title = title,
        artist = artist,
        duration = duration
    )
}