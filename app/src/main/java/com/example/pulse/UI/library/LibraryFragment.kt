package com.example.pulse.UI.library

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentLibraryBinding

class LibraryFragment: Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val audioPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        uri: Uri? ->
        if (uri != null) {
            onAudioSelected(uri)
        } else {
            Toast.makeText(
                requireContext(),
                "Файл не выбран",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    private fun onAudioSelected(uri: Uri) {
        Toast.makeText(
            requireContext(),
            "Аудиофайл выбран:\\n\$uri",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLibraryBinding.bind(view)

        binding.addMusicButton.setOnClickListener {
            audioPicker.launch(arrayOf("audio/*"))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}