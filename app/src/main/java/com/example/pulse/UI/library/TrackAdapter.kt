package com.example.pulse.UI.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.data.Track
import com.example.pulse.databinding.ItemTrackBinding

class TrackAdapter(
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private val tracks = mutableListOf<Track>()

    fun addTrack(track: Track) {
        tracks.add(track)
        notifyItemInserted(tracks.lastIndex)
    }

    fun setTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
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
        holder.binding.tvTrackDuration.text =
            formatDuration(track.duration)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
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
