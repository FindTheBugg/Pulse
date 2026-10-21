package com.example.pulse.UI.player

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pulse.R
import com.example.pulse.databinding.FragmentPlayerBinding
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels()

    private var isUserSeeking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        setupButtons()
        setupSeekBar()
        observePlayer()
    }

    private fun setupButtons() {
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
    }

    private fun setupSeekBar() {
        binding.seekBarProgress.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        binding.tvCurrentTime.text = formatTime(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    isUserSeeking = false
                    playerViewModel.onSeekTo(seekBar.progress.toLong())
                }
            }
        )
    }

    private fun observePlayer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
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

                launch {
                    playerViewModel.playerState.collect { state ->
                        binding.btnPlayPause.text =
                            if (state.isPlaying) "⏸\uFE0F Pause" else "▶\uFE0F Play"

                        binding.seekBarProgress.isEnabled = state.durationMs > 0
                        binding.seekBarProgress.max = state.durationMs.toInt()
                        binding.tvDuration.text = formatTime(state.durationMs)

                        if (!isUserSeeking) {
                            binding.seekBarProgress.progress = state.positionMs.toInt()
                            binding.tvCurrentTime.text = formatTime(state.positionMs)
                        }
                    }
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}