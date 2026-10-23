package com.example.pulse.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.pulse.R
import com.example.pulse.ui.player.PlayerViewModel
import com.example.pulse.data.TrackRepository
import com.example.pulse.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val repository by lazy { TrackRepository(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        binding.btnClearLibrary.setOnClickListener {
            showClearConfirmation()
        }
    }

    private fun showClearConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистить библиотеку треков?")
            .setMessage("Вы уверены что хотите удалить ВСЕ треки?")
            .setPositiveButton("Удалить") { _, _ -> clearLibrary() }
            .setNegativeButton("Нинада", null)
            .show()
    }

    private fun clearLibrary() {
        val context = requireContext()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.clearAll()
                Toast.makeText(context, "Библиотека очищена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось очистить библиотеку", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}