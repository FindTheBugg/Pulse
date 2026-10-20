package com.example.pulse.UI.player

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentPlayerBinding

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)
        binding.tvTrackTitle.text = "Трек не выбран"
        binding.tvTrackArtist.text = "Выбери музыку в коллекции"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}