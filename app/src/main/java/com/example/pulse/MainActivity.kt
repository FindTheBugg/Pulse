
package com.example.pulse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pulse.databinding.ActivityMainBinding
import com.example.pulse.UI.library.LibraryFragment
import com.example.pulse.UI.overview.OverviewFragment
import com.example.pulse.UI.playlists.PlaylistsFragment
import com.example.pulse.UI.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_library -> LibraryFragment()
                R.id.nav_playlists -> PlaylistsFragment()
                R.id.nav_overview -> OverviewFragment()
                R.id.nav_settings -> SettingsFragment()
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
            binding.bottomNavigation.selectedItemId = R.id.nav_library
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
