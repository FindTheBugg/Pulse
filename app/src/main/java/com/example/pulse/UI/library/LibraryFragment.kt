package com.example.pulse.UI.library

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentLibraryBinding

class LibraryFragment: Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLibraryBinding.bind(view)

        binding.addMusicButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Импорт музыки появится во втором спринте",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}