package com.example.pulse.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.data.Track
import com.example.pulse.databinding.ItemTrackBinding

class TrackAdapter(
    private val onMoreClick: ((Track) -> Unit)? = null,
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, TrackAdapter.TrackViewHolder>(DiffCallback) {

    private var currentTrackId: String? = null
    private var unavailableIds: Set<String> = emptySet()
    private var extraInfo: Map<String, String> = emptyMap()

    fun setExtraInfo(info: Map<String, String>) {
        if (info == extraInfo) return
        extraInfo = info
        notifyItemRangeChanged(0, itemCount)
    }
    fun setTracks(newTracks: List<Track>) {
        submitList(newTracks)
    }

    fun setCurrentTrackId(newId: String?) {
        if (newId == currentTrackId) return
        val oldId = currentTrackId
        currentTrackId = newId
        notifyRowOf(oldId)
        notifyRowOf(newId)
    }

    fun setUnavailableIds(ids: Set<String>) {
        if (ids == unavailableIds) return
        val changed = (unavailableIds - ids) + (ids - unavailableIds)
        unavailableIds = ids
        changed.forEach { notifyRowOf(it) }
    }

    private fun notifyRowOf(trackId: String?) {
        if (trackId == null) return
        val index = currentList.indexOfFirst { it.id == trackId }
        if (index != -1) notifyItemChanged(index)
    }

    class TrackViewHolder(
        val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        val unavailable = track.id in unavailableIds
        val extra = extraInfo[track.id]

        holder.binding.tvTrackTitle.text = track.title
        holder.binding.tvTrackArtist.text = track.artist
        holder.binding.tvTrackDuration.text = when {
            unavailable -> "Файл недоступен"
            extra != null -> "${formatDuration(track.duration)} · $extra"
            else -> formatDuration(track.duration)
        }

        holder.binding.trackFrame.setEffectEnabled(track.id == currentTrackId)

        holder.binding.btnMore.isVisible = onMoreClick != null
        holder.binding.btnMore.setOnClickListener { onMoreClick?.invoke(track) }
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(old: Track, new: Track) = old.id == new.id
        override fun areContentsTheSame(old: Track, new: Track) = old == new
    }
}