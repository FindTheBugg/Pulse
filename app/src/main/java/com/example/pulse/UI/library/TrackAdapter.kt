package com.example.pulse.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.R
import com.example.pulse.data.Track
import com.example.pulse.databinding.ItemTrackBinding

class TrackAdapter(
    private val onTrackLongClick: ((Track) -> Unit)? = null,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private val tracks = mutableListOf<Track>()
    private var currentTrackId: String? = null

    fun setTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    fun setCurrentTrackId(newId: String?) {
        if (newId == currentTrackId) return

        val oldIndex = tracks.indexOfFirst { it.id == currentTrackId }
        val newIndex = tracks.indexOfFirst { it.id == newId }
        currentTrackId = newId

        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    class TrackViewHolder(
        val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TrackViewHolder,
        position: Int
    ) {
        val track = tracks[position]

        holder.binding.tvTrackTitle.text = track.title
        holder.binding.tvTrackArtist.text = track.artist
        holder.binding.tvTrackDuration.text = formatDuration(track.duration)

        holder.binding.trackFrame.setEffectEnabled(
            track.id == currentTrackId
        )

        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }

        holder.itemView.setOnLongClickListener {
            onTrackLongClick?.invoke(track)
            onTrackLongClick != null
        }
    }

    override fun getItemCount(): Int = tracks.size

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}