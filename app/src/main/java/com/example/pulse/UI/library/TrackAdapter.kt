package com.example.pulse.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.data.Track
import com.example.pulse.databinding.ItemTrackBinding

class TrackAdapter(
    private val onMoreClick: ((Track) -> Unit)? = null,
    private val onTrackClick: (Track) -> Unit,
    private var unavailableIds: Set<String> = emptySet()
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {
    private val tracks = mutableListOf<Track>()
    private var currentTrackId: String? = null

    fun setTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    fun setUnavailableIds(ids: Set<String>) {
        if (ids == unavailableIds) return
        unavailableIds = ids
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
        holder.binding.btnMore.isVisible = onMoreClick != null
        holder.binding.btnMore.setOnClickListener { onMoreClick?.invoke(track) }
        val unavailable = track.id in unavailableIds
        holder.binding.tvTrackDuration.text =
            if (unavailable) "Файл недоступен" else formatDuration(track.duration)
        holder.itemView.alpha = if (unavailable) 0.25f else 1f

        holder.binding.trackFrame.setEffectEnabled(track.id == currentTrackId)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }

        holder.binding.btnMore.isVisible = onMoreClick != null
        holder.binding.btnMore.setOnClickListener { onMoreClick?.invoke(track) }
    }

    override fun getItemCount(): Int = tracks.size

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}