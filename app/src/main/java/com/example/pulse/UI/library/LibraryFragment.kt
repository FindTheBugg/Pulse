package com.example.pulse.UI.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulse.R
import com.example.pulse.databinding.FragmentLibraryBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.activityViewModels
import com.example.pulse.UI.player.PlayerViewModel
import com.example.pulse.data.TrackRepository

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackAdapter: TrackAdapter
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val repository by lazy { TrackRepository(requireContext()) }
    private val audioPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                Toast.makeText(
                    requireContext(),
                    "Файл не выбран",
                    Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }

            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val hasPermission = requireContext()
                    .contentResolver
                    .persistedUriPermissions
                    .any { it.uri == uri && it.isReadPermission }
                if (hasPermission) {
                    onAudioSelected(uri)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось сохранить доступ к файлу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Не удалось сохранить доступ к файлу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLibraryBinding.bind(view)
        observePlayerState()

        trackAdapter = TrackAdapter { track ->
            playerViewModel.onTrackClick(track)
        }
        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
        binding.addMusicButton.setOnClickListener {
            audioPicker.launch(arrayOf("audio/*"))
        }
        binding.btnPlayPause.setOnClickListener {
            if (!playerViewModel.hasTrack) {
                Toast.makeText(
                    requireContext(),
                    "Сначала выбери трек",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                playerViewModel.onPlayPauseClick()
            }
        }
        binding.btnPrevious.setOnClickListener {
            playerViewModel.onPreviousClick()
        }
        binding.btnNext.setOnClickListener {
            playerViewModel.onNextClick()
        }
        loadTracks()
    }

    private fun observePlayerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                playerViewModel.playerState.collect { state ->
                    binding.btnPlayPause.text =
                        if (state.isPlaying) "⏸\uFE0F Pause" else "▶\uFE0F Play"
                }
            }
        }
    }

    private fun loadTracks() {
        viewLifecycleOwner.lifecycleScope.launch {
            val tracks = repository.getAllTracks()
            trackAdapter.setTracks(tracks)
            playerViewModel.setPlaylist(tracks)
            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        val isEmpty = trackAdapter.itemCount == 0

        binding.emptyStateText.isVisible = isEmpty
        binding.tracksRecyclerView.isVisible = !isEmpty
    }

    private fun onAudioSelected(uri: Uri) {
        val context = requireContext()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.addTrack(uri)
                loadTracks()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Не удалось прочитать или сохранить аудиофайл",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    override fun onDestroyView() {
        binding.tracksRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

}
