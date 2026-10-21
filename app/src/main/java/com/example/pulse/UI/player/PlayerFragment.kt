package com.example.pulse.UI.player

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentPlayerBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)
        binding.tvTrackTitle.text = "Трек не выбран"
        binding.tvTrackArtist.text = "Выбери музыку в коллекции"
        observeCurrentTrack()
    }

    private fun observeCurrentTrack() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playerViewModel.currentTrack.collect { track ->
                    if (track == null) {
                        binding.tvTrackTitle.text = "Трек не выбран"
                        binding.tvTrackArtist.text = "Выбери музыку в коллекции"
                    } else {
                        binding.tvTrackTitle.text = track.title
                        binding.tvTrackArtist.text = track.artist
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}