package com.example.pulse.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.UI.settings.readChangelog
import com.example.pulse.databinding.FragmentInfoBinding
import com.example.pulse.databinding.ItemChangelogLineBinding
import com.example.pulse.databinding.ItemChangelogVersionBinding


class InfoFragment: Fragment(R.layout.fragment_info) {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInfoBinding.bind(view)
        showChangelog()
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    private fun showChangelog() {
        val inflater = LayoutInflater.from(requireContext())
        val container = binding.changelogContainer
        container.removeAllViews()

        readChangelog(requireContext()).forEach { version ->
            val header = ItemChangelogVersionBinding.inflate(inflater, container, false)
            header.tvVersion.text = "v${version.version}"
            container.addView(header.root)

            version.items.forEach { text ->
                val line = ItemChangelogLineBinding.inflate(inflater, container, false)
                line.tvLine.text = text
                container.addView(line.root)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}