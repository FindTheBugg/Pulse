package com.example.pulse.ui.playlists

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pulse.R
import com.example.pulse.databinding.FragmentChoiceBinding
import com.example.pulse.ui.common.ChoiceAdapter
import kotlinx.coroutines.launch

class AddTracksFragment : Fragment(R.layout.fragment_choice) {

    private var _binding: FragmentChoiceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTracksViewModel by viewModels()
    private lateinit var choiceAdapter: ChoiceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChoiceBinding.bind(view)

        binding.tvChoiceHeader.text = "Добавить треки"
        binding.emptyStateText.text = "В коллекции нет треков.\nСначала добавь музыку."

        choiceAdapter = ChoiceAdapter { count -> updateConfirmButton(count) }
        binding.choiceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = choiceAdapter
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnConfirm.setOnClickListener { confirm() }
        updateConfirmButton(0)

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.choices.collect { choices ->
                    if (choices == null) return@collect
                    choiceAdapter.submitList(choices)
                    binding.emptyStateText.isVisible = choices.isEmpty()
                    binding.choiceRecyclerView.isVisible = choices.isNotEmpty()
                }
            }
        }
    }

    private fun updateConfirmButton(count: Int) {
        binding.btnConfirm.isEnabled = count > 0
        binding.btnConfirm.text = if (count > 0) "Добавить ($count)" else "Добавить"
    }

    private fun confirm() {
        val ids = choiceAdapter.getSelectedIds()
        viewLifecycleOwner.lifecycleScope.launch {
            val added = viewModel.confirm(ids)
            Toast.makeText(
                requireContext(),
                if (added > 0) "Добавлено треков: $added" else "Ничего не добавлено",
                Toast.LENGTH_SHORT
            ).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        binding.choiceRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(playlistId: Long) = AddTracksFragment().apply {
            arguments = bundleOf(AddTracksViewModel.ARG_PLAYLIST_ID to playlistId)
        }
    }
}