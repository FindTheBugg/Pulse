
package com.example.pulse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pulse.data.CoverStore
import com.example.pulse.databinding.ActivityMainBinding
import com.example.pulse.ui.library.LibraryFragment
import com.example.pulse.ui.overview.OverviewFragment
import com.example.pulse.ui.player.PlayerFragment
import com.example.pulse.ui.player.PlayerViewModel
import com.example.pulse.ui.playlists.PlaylistsFragment
import com.example.pulse.ui.settings.SettingsFragment
import com.example.pulse.update.UpdateEvent
import com.example.pulse.update.UpdateViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private val currentTab = MutableStateFlow(R.id.nav_library)
    private val updateViewModel: UpdateViewModel by viewModels()
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private lateinit var binding: ActivityMainBinding
    private val coverStore by lazy { CoverStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets()

        binding.bottomNavigation.setOnItemReselectedListener {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_library -> LibraryFragment()
                R.id.nav_playlists -> PlaylistsFragment()
                R.id.nav_overview -> OverviewFragment()
                R.id.nav_settings -> SettingsFragment()
                R.id.nav_player -> PlayerFragment()
                else -> null
            }

            if (fragment != null) {
                openFragment(fragment)
                supportFragmentManager.executePendingTransactions()
                currentTab.value = item.itemId
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            openFragment(LibraryFragment())
        }
        observeUpdates()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        currentTab.value = binding.bottomNavigation.selectedItemId
        setupMiniPlayer()
    }


    private fun observeUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                updateViewModel.events.collect { event ->
                    when (event) {
                        is UpdateEvent.Message ->
                            Toast.makeText(this@MainActivity, event.text, Toast.LENGTH_LONG).show()
                        is UpdateEvent.ReadyToInstall -> installApk(event.file)
                    }
                }
            }
        }
    }

    private fun setupMiniPlayer() {
        val mini = binding.miniPlayer

        mini.root.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_player
        }
        mini.btnMiniPlayPause.setOnClickListener { playerViewModel.onPlayPauseClick() }
        mini.btnMiniNext.setOnClickListener { playerViewModel.onNextClick() }
        mini.btnMiniPrevious.setOnClickListener { playerViewModel.onPreviousClick() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    combine(playerViewModel.currentTrack, currentTab) { track, tab -> track to tab }
                        .collectLatest { (track, tab) ->
                            mini.root.isVisible = track != null && tab != R.id.nav_player
                            if (track == null) return@collectLatest

                            mini.tvMiniTitle.text = track.title
                            mini.tvMiniArtist.text = track.artist

                            val cached = coverStore.cached(track.id)
                            if (cached != null) {
                                mini.miniCover.setImageBitmap(cached)
                            } else {
                                mini.miniCover.setImageResource(R.drawable.ic_cover_placeholder)
                                coverStore.loadBitmap(track)?.let { mini.miniCover.setImageBitmap(it) }
                            }
                        }
                }

                launch {
                    playerViewModel.playerState.collect { state ->
                        mini.btnMiniPlayPause.setImageResource(
                            if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        )
                        mini.miniProgress.max = state.durationMs.toInt().coerceAtLeast(1)
                        mini.miniProgress.setProgressCompat(state.positionMs.toInt(), false)
                    }
                }
            }
        }
    }

    private fun installApk(file: File) {
        if (!packageManager.canRequestPackageInstalls()) {
            updateViewModel.awaitingInstallPermission = file
            Toast.makeText(
                this,
                "Разреши установку обновлений для Pulse и вернись в приложение",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val waiting = updateViewModel.awaitingInstallPermission ?: return
        if (packageManager.canRequestPackageInstalls()) {
            updateViewModel.awaitingInstallPermission = null
            installApk(waiting)
        }
    }

    private fun setupWindowInsets() {
        val root = binding.root
        val initialTop = root.paddingTop

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(
                top = initialTop + systemBars.top
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
