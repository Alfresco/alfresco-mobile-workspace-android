package com.alfresco.content.app.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.alfresco.content.app.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}
