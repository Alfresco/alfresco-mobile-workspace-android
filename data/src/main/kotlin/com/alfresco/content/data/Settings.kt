package com.alfresco.content.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class Settings(
    val context: Context
) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val themeKey = context.getString(R.string.pref_theme_key)
    private val syncNetworkKey = context.getString(R.string.pref_sync_network_key)
    private val preferenceChangedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val listener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            AnalyticsManager().theme(prefs.getString(key, "") ?: "")
            preferenceChangedFlow.tryEmit(key)
        }

    fun observeChanges() {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
    }

    val theme: Theme
        get() = themeFromStoredValue(sharedPref.getString(themeKey, null))

    fun observeTheme(): Flow<Theme> =
        preferenceChangedFlow
            .onStart { emit(themeKey) }
            .filter { it == themeKey }
            .map { theme }
            .distinctUntilChanged()

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

    val getDistributionVersion: DistributionVersion
        get() = checkVersion(sharedPref.getString(DISTRIBUTION_VERSION, ""))

    private fun checkVersion(value: String?) = when (value) {
        Enterprise -> DistributionVersion.ENTERPRISE
        else -> DistributionVersion.COMMUNITY
    }

    enum class Theme {
        Light,
        Dark,
        System
    }

    enum class SyncNetwork {
        Wifi,
        Mobile
    }

    enum class DistributionVersion {
        ENTERPRISE,
        COMMUNITY
    }

    private companion object {
        const val DISTRIBUTION_VERSION = "DISTRIBUTION_VERSION"
        const val Enterprise = "Enterprise"
    }
}
