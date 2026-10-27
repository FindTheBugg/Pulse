package com.example.pulse.ui.playlists

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulse.R
import com.example.pulse.data.Playlist
import com.example.pulse.databinding.FragmentPlaylistsBinding
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by activityViewModels()
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistsBinding.bind(view)

        playlistAdapter = PlaylistAdapter(
            onClick = { playlist ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, PlaylistDetailFragment.newInstance(playlist.id))
                    .addToBackStack(null)
                    .commit()
            },
            onRename = { playlist -> showRenameDialog(playlist) },
            onDelete = { playlist -> showDeleteDialog(playlist) }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }

        binding.btnCreatePlaylist.setOnClickListener {
            showNameDialog("Новый плейлист", "") { name ->
                viewModel.createPlaylist(name)
            }
        }

        setupSwipeToDelete()
        observeState()
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val playlist = playlistAdapter.currentList[position]
                showDeleteDialog(playlist) {
                    playlistAdapter.notifyItemChanged(position)
                }
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.playlistsRecyclerView)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.playlists.collect { playlists ->
                        if (playlists == null) return@collect
                        playlistAdapter.submitList(playlists)
                        binding.emptyStateText.isVisible = playlists.isEmpty()
                        binding.playlistsRecyclerView.isVisible = playlists.isNotEmpty()
                    }
                }

                launch {
                    viewModel.messages.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

     fun showNameDialog(
        title: String,
        initialName: String,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(requireContext()).apply {
            hint = "Название"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine()
            setText(initialName)
            setSelection(text.length)
        }

        val density = resources.displayMetrics.density
        val sidePadding = (24 * density).toInt()
        val topPadding = (8 * density).toInt()
        val container = FrameLayout(requireContext()).apply {
            setPadding(sidePadding, topPadding, sidePadding, 0)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(container)
            .setPositiveButton("Готово") { _, _ -> onConfirm(input.text.toString()) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showRenameDialog(playlist: Playlist) {
        showNameDialog("Переименовать", playlist.name) { name ->
            viewModel.renamePlaylist(playlist.id, name)
        }
    }

    private fun showDeleteDialog(playlist: Playlist, onCancel: () -> Unit = {}) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист?")
            .setMessage("«${playlist.name}» будет удалён. Треки останутся в коллекции.")
            .setPositiveButton("Удалить") { _, _ -> viewModel.deletePlaylist(playlist.id) }
            .setNegativeButton("Отмена") { _, _ -> onCancel() }
            .setOnCancelListener { onCancel() }
            .show()
    }

    override fun onDestroyView() {
        binding.playlistsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}