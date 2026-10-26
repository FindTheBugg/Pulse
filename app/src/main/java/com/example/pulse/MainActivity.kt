
package com.example.pulse

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.pulse.databinding.ActivityMainBinding
import com.example.pulse.ui.library.LibraryFragment
import com.example.pulse.ui.overview.OverviewFragment
import com.example.pulse.ui.playlists.PlaylistsFragment
import com.example.pulse.ui.settings.SettingsFragment
import com.example.pulse.ui.player.PlayerFragment
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            openFragment(LibraryFragment())
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
