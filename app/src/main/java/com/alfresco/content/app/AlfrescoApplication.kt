package com.alfresco.content.app

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.alfresco.content.data.Settings
import com.alfresco.content.network.ConnectivityTracker

class AlfrescoApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate() {
        super.onCreate()

        updateAppTheme()

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        ConnectivityTracker.startTracking(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.pref_theme_key) -> updateAppTheme()
        }
    }

    private fun updateAppTheme() {
        AppCompatDelegate.setDefaultNightMode(
            when (Settings(this).theme) {
                Settings.Theme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Settings.Theme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
                Settings.Theme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
