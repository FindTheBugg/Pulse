package com.example.pulse.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulse.R
import com.example.pulse.data.Track
import com.example.pulse.databinding.FragmentLibraryBinding
import com.example.pulse.ui.common.SheetAction
import com.example.pulse.ui.common.TrackOptionsSheet
import com.example.pulse.ui.player.PlayerViewModel
import com.example.pulse.ui.playlists.AddToPlaylistFragment
import kotlinx.coroutines.launch

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackAdapter: TrackAdapter
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val libraryViewModel: LibraryViewModel by activityViewModels()

    private val audioPicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isEmpty()) {
                Toast.makeText(requireContext(), "Файлы не выбраны", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val resolver = requireContext().contentResolver
            val granted = uris.filter { uri ->
                try {
                    resolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (granted.size < uris.size) showPermissionError()
            if (granted.isNotEmpty()) libraryViewModel.onAudiosSelected(granted)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        trackAdapter = TrackAdapter(
            onMoreClick = { track -> showTrackOptions(track) },
            onTrackClick = { track -> playerViewModel.onTrackClick(track) }
        )
        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }

        binding.addMusicButton.setOnClickListener {
            audioPicker.launch(arrayOf("audio/*"))
        }

        observeState()
    }

    private fun showTrackOptions(track: Track) {
        if (track.id in playerViewModel.unavailableIds.value) {
            Toast.makeText(
                requireContext(),
                "Файл недоступен: добавить в плейлист нельзя",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        TrackOptionsSheet.show(
            requireContext(),
            track,
            listOf(SheetAction("Добавить в плейлист") { openAddToPlaylist(track) })
        )
    }

    private fun openAddToPlaylist(track: Track) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AddToPlaylistFragment.newInstance(track.id))
            .addToBackStack(null)
            .commit()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    playerViewModel.unavailableIds.collect { ids ->
                        trackAdapter.setUnavailableIds(ids)
                    }
                }

                launch {
                    libraryViewModel.tracks.collect { tracks ->
                        if (tracks == null) return@collect
                        trackAdapter.setTracks(tracks)
                        binding.emptyStateText.isVisible = tracks.isEmpty()
                        binding.tracksRecyclerView.isVisible = tracks.isNotEmpty()
                    }
                }

                launch {
                    playerViewModel.currentTrack.collect { track ->
                        trackAdapter.setCurrentTrackId(track?.id)
                    }
                }

                launch {
                    libraryViewModel.messages.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showPermissionError() {
        Toast.makeText(
            requireContext(),
            "Не удалось сохранить доступ к файлу",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        binding.tracksRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}