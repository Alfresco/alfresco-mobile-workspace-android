package com.alfresco.content.app.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.alfresco.content.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        bottomNav.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        setupActionBarWithNavController(navController, appBarConfiguration)

        updateAppTheme()
    }

    override fun onResume() {
        super.onResume()

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    override fun onPause() {
        super.onPause()

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.settings_theme_key) -> updateAppTheme()
        }
    }

    private fun updateAppTheme() {
        when (PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_light))) {
            getString(R.string.settings_theme_light) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            getString(R.string.settings_theme_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            getString(R.string.settings_theme_system) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
