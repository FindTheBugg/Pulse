package com.example.pulse.ui.library

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
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
import com.example.pulse.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackAdapter: TrackAdapter
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val libraryViewModel: LibraryViewModel by activityViewModels()

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
                    libraryViewModel.onAudioSelected(uri)
                } else {
                    showPermissionError()
                }
            } catch (e: Exception) {
                showPermissionError()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLibraryBinding.bind(view)

        trackAdapter = TrackAdapter(
            onTrackLongClick = { track -> showAddToPlaylistDialog(track) },
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

    private fun showAddToPlaylistDialog(track: Track) {
        val playlists = libraryViewModel.playlists.value
        val items = (playlists.map { it.name } + "➕ Создать новый плейлист").toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить в плейлист")
            .setItems(items) { _, which ->
                if (which < playlists.size) {
                    libraryViewModel.addTrackToPlaylist(track, playlists[which])
                } else {
                    showCreatePlaylistDialog(track)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showCreatePlaylistDialog(track: Track) {
        val input = EditText(requireContext()).apply {
            hint = "Название"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine()
        }

        val density = resources.displayMetrics.density
        val container = FrameLayout(requireContext()).apply {
            setPadding((24 * density).toInt(), (8 * density).toInt(), (24 * density).toInt(), 0)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Новый плейлист")
            .setView(container)
            .setPositiveButton("Создать") { _, _ ->
                libraryViewModel.createPlaylistAndAddTrack(track, input.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

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