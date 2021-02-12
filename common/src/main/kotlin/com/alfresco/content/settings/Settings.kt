package com.alfresco.content.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.alfresco.content.common.R

class Settings(
    val context: Context
) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val themeKey = context.getString(R.string.pref_theme_key)

    val theme: Theme
        get() = themeFromStoredValue(sharedPref.getString(themeKey, null))

    private fun themeFromStoredValue(value: String?) =
        when (value) {
            context.getString(R.string.pref_theme_light_value) -> Theme.Light
            context.getString(R.string.pref_theme_dark_value) -> Theme.Dark
            else -> Theme.System
        }

    enum class Theme {
        Light,
        Dark,
        System
    }
}