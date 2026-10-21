package com.example.pulse.UI

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.pulse.data.Track

class TrackMetadataReader(
    private val context: Context
) {

    fun readTrack(uri: Uri): Track {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            ) ?: "Неизвестный трек"

            val artist = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            ) ?: "Неизвестный исполнитель"

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L

            return Track(
                id = uri.toString(),
                uri = uri,
                title = title,
                artist = artist,
                duration = duration
            )

        } finally {
            retriever.release()
        }
    }
}
