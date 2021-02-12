package com.alfresco.content.data

import android.content.Context
import androidx.preference.PreferenceManager

class Settings(
    val context: Context
) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val themeKey = context.getString(R.string.pref_theme_key)
    private val syncNetworkKey = context.getString(R.string.pref_sync_network_key)

    val theme: Theme
        get() = themeFromStoredValue(sharedPref.getString(themeKey, null))

    private fun themeFromStoredValue(value: String?) =
        when (value) {
            context.getString(R.string.pref_theme_light_value) -> Theme.Light
            context.getString(R.string.pref_theme_dark_value) -> Theme.Dark
            context.getString(R.string.pref_theme_system_value) -> Theme.System
            else -> Theme.System
        }

    val syncNetwork: SyncNetwork
        get() = syncNetworkFromStoredValue(sharedPref.getString(syncNetworkKey, null))

    private fun syncNetworkFromStoredValue(value: String?) =
        when (value) {
            context.getString(R.string.pref_sync_network_wifi_value) -> SyncNetwork.Wifi
            context.getString(R.string.pref_sync_network_mobile_value) -> SyncNetwork.Mobile
            else -> SyncNetwork.Wifi
        }

    val canSyncOverMeteredNetwork: Boolean
        get() = syncNetwork == SyncNetwork.Mobile

    enum class Theme {
        Light,
        Dark,
        System
    }

    enum class SyncNetwork {
        Wifi,
        Mobile
    }
}
