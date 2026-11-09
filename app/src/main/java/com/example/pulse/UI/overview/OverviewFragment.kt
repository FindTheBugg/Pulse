package com.example.pulse.ui.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.pulse.R
import com.example.pulse.data.Track
import com.example.pulse.databinding.FragmentOverviewBinding
import com.example.pulse.ui.common.timesText
import com.example.pulse.ui.library.TrackAdapter
import com.example.pulse.ui.player.PlayerViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class OverviewFragment : Fragment(R.layout.fragment_overview) {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OverviewViewModel by activityViewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private lateinit var recentAdapter: TrackAdapter
    private lateinit var popularAdapter: TrackAdapter
    private var recentTracks: List<Track> = emptyList()
    private var popularTracks: List<Track> = emptyList()
    private var loaded = false
    private var selectedTab = TAB_POPULAR

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOverviewBinding.bind(view)

        recentAdapter = TrackAdapter(
            onTrackClick = { track -> playerViewModel.onPlaylistTrackClick(track, recentTracks) }
        )
        popularAdapter = TrackAdapter(
            onTrackClick = { track -> playerViewModel.onPlaylistTrackClick(track, popularTracks) }
        )

        setupList(binding.recentRecyclerView, recentAdapter)
        setupList(binding.popularRecyclerView, popularAdapter)
        setupTabs()
        observeState()
    }

    private fun setupList(list: RecyclerView, adapter: TrackAdapter) {
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
        (list.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    private fun setupTabs() {
        val tabs = binding.overviewTabs
        tabs.addTab(tabs.newTab().setText("Часто играемые"))
        tabs.addTab(tabs.newTab().setText("Недавно проигранные"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position
                render()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun render() {
        if (!loaded) return

        val popular = selectedTab == TAB_POPULAR
        val hasItems = (if (popular) popularTracks else recentTracks).isNotEmpty()

        binding.popularRecyclerView.isVisible = popular && hasItems
        binding.recentRecyclerView.isVisible = !popular && hasItems
        binding.emptyStateText.isVisible = !hasItems
        binding.emptyStateText.text =
            if (popular) "Здесь появятся треки, которые ты слушаешь чаще всего"
            else "Ты пока ничего не слушал.\nВключи что-нибудь!"
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.state.collect { state ->
                        if (state == null) return@collect

                        recentTracks = state.recent.map { it.track }
                        popularTracks = state.popular.map { it.track }

                        recentAdapter.setTracks(recentTracks)
                        popularAdapter.setTracks(popularTracks)
                        popularAdapter.setExtraInfo(
                            state.popular.associate { it.track.id to timesText(it.playCount) }
                        )

                        loaded = true
                        render()
                    }
                }

                launch {
                    playerViewModel.currentTrack.collect { track ->
                        recentAdapter.setCurrentTrackId(track?.id)
                        popularAdapter.setCurrentTrackId(track?.id)
                    }
                }

                launch {
                    playerViewModel.unavailableIds.collect { ids ->
                        recentAdapter.setUnavailableIds(ids)
                        popularAdapter.setUnavailableIds(ids)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.recentRecyclerView.adapter = null
        binding.popularRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val TAB_POPULAR = 0
        const val TAB_RECENT = 1
    }
}