package com.example.pulse.ui.playlists

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulse.R
import com.example.pulse.data.Track
import com.example.pulse.databinding.FragmentPlaylistDetailBinding
import com.example.pulse.ui.common.SheetAction
import com.example.pulse.ui.common.TrackOptionsSheet
import com.example.pulse.ui.library.TrackAdapter
import com.example.pulse.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

class PlaylistDetailFragment : Fragment(R.layout.fragment_playlist_detail) {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistDetailViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private lateinit var trackAdapter: TrackAdapter
    private var currentTracks: List<Track> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistDetailBinding.bind(view)

        trackAdapter = TrackAdapter(
            onMoreClick = { track -> showTrackOptions(track) },
            onTrackClick = { track ->
                playerViewModel.onPlaylistTrackClick(track, currentTracks)
            }
        )
        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnPlayPlaylist.setOnClickListener {
            if (currentTracks.isEmpty()) {
                Toast.makeText(requireContext(), "В плейлисте нет треков", Toast.LENGTH_SHORT).show()
            } else {
                playerViewModel.onPlayPlaylist(currentTracks)
            }
        }

        observeState()
    }

    private fun showTrackOptions(track: Track) {
        TrackOptionsSheet.show(
            requireContext(),
            track,
            listOf(
                SheetAction("Добавить в плейлист") {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            AddToPlaylistFragment.newInstance(track.id)
                        )
                        .addToBackStack(null)
                        .commit()
                },
                SheetAction("Убрать из этого плейлиста") { showRemoveDialog(track) }
            )
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.playlist.collect { playlist ->
                        if (playlist != null) {
                            binding.tvPlaylistName.text = playlist.name
                        }
                    }
                }

                launch {
                    viewModel.tracks.collect { tracks ->
                        if (tracks == null) return@collect
                        currentTracks = tracks
                        trackAdapter.setTracks(tracks)
                        binding.tvTrackCount.text = "Треков: ${tracks.size}"
                        binding.emptyStateText.isVisible = tracks.isEmpty()
                        binding.tracksRecyclerView.isVisible = tracks.isNotEmpty()
                    }
                }

                launch {
                    playerViewModel.currentTrack.collect { track ->
                        trackAdapter.setCurrentTrackId(track?.id)
                    }
                }
            }
        }
    }

    private fun showRemoveDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle("Убрать из плейлиста?")
            .setMessage("«${track.title}» останется в коллекции.")
            .setPositiveButton("Убрать") { _, _ -> viewModel.removeTrack(track.id) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        binding.tracksRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(playlistId: Long) = PlaylistDetailFragment().apply {
            arguments = bundleOf(PlaylistDetailViewModel.ARG_PLAYLIST_ID to playlistId)
        }
    }
}