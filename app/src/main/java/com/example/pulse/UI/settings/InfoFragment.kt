package com.example.pulse.UI.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pulse.R
import com.example.pulse.databinding.FragmentInfoBinding


class InfoFragment: Fragment(R.layout.fragment_info) {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInfoBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}