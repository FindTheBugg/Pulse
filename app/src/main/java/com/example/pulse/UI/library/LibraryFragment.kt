package com.example.pulse.UI.library

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentLibraryBinding
import com.example.pulse.data.TrackMetadataReader

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
       try {
           val reader = TrackMetadataReader(requireContext())
           val track = reader.readTrack(uri)

           android.util.Log.d("Pulsed", "Название: ${track.title}")
           android.util.Log.d("Pulsed", "Исполнитель: ${track.artist}")
           android.util.Log.d("Pulsed", "Длительность: ${track.duration}")

       } catch (e: Exception) {
           Toast.makeText(
               requireContext(),
               "Не удалось прочитать аудиофайл",
               Toast.LENGTH_SHORT
           ).show()
           android.util.Log.e("Pulsed", "Ошибка чтения аудиофайла", e)
       }
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