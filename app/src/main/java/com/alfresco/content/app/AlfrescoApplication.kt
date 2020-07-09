package com.alfresco.content.app

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class AlfrescoApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate() {
        super.onCreate()

        updateAppTheme()

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.settings_theme_key) -> updateAppTheme()
        }
    }

    private fun updateAppTheme() {
        when (PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_system))) {
            getString(R.string.settings_theme_light) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO)
            getString(R.string.settings_theme_dark) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES)
            getString(R.string.settings_theme_system) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
