package com.example.pulse.ui.playlists

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulse.R
import com.example.pulse.data.Playlist
import com.example.pulse.databinding.FragmentPlaylistsBinding
import com.example.pulse.ui.playlists.PlaylistAdapter

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlaylistsBinding.bind(view)

        playlistAdapter = PlaylistAdapter(
            onClick = { },
            onRename = { },
            onDelete = { }
        )

        binding.playlistsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }
// тестовые данные потом надо буде тудалить
        val testPlaylists = listOf(
            Playlist(
                id = 1L,
                name = "Спорт",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 2L,
                name = "Сон",
                createdAt = System.currentTimeMillis(),
                trackCount = 3
            ),
            Playlist(
                id = 1L,
                name = "Панк",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Глэм",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Вайб",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Фонк",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Джаз",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Хип=Хоп",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Рэп",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
            Playlist(
                id = 1L,
                name = "Рок",
                createdAt = System.currentTimeMillis(),
                trackCount = 12
            ),
        )

        playlistAdapter.submitList(testPlaylists)

        binding.emptyStateText.isVisible = testPlaylists.isEmpty()
        binding.playlistsRecyclerView.isVisible = testPlaylists.isNotEmpty()
    }

    override fun onDestroyView() {
        binding.playlistsRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}