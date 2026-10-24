package com.example.pulse.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.data.Playlist
import com.example.pulse.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onClick: (Playlist) -> Unit,
    private val onRename: (Playlist) -> Unit,
    private val onDelete: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(DiffCallback) {

    class PlaylistViewHolder(
        val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)

        holder.binding.tvPlaylistName.text = playlist.name
        holder.binding.tvPlaylistCount.text =
            "${playlist.trackCount} ${tracksWord(playlist.trackCount)}"

        holder.itemView.setOnClickListener { onClick(playlist) }
        holder.itemView.setOnLongClickListener {
            onRename(playlist)
            true
        }

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(
                holder.itemView.context,
                holder.itemView
            )

            popup.menu.add(0, MENU_RENAME, 0, "Переименовать")
            popup.menu.add(0, MENU_DELETE, 1, "Удалить")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_RENAME -> onRename(playlist)
                    MENU_DELETE -> onDelete(playlist)
                }
                true
            }

            popup.show()
            true
        }
    }

    private fun tracksWord(count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100
        return when {
            mod100 in 11..14 -> "треков"
            mod10 == 1 -> "трек"
            mod10 in 2..4 -> "трека"
            else -> "треков"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(old: Playlist, new: Playlist) = old.id == new.id
        override fun areContentsTheSame(old: Playlist, new: Playlist) = old == new
    }

    private companion object {
        const val MENU_RENAME = 1
        const val MENU_DELETE = 2
    }
}