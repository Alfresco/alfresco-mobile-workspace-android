package com.alfresco.content.app.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.alfresco.content.app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        preferenceScreen.findPreference<Preference>(resources.getString(R.string.settings_account_key))?.apply {
            setSummary("other summary")
        }

        preferenceScreen.findPreference<Preference>(resources.getString(R.string.settings_sign_out_key))?.apply {
            setOnPreferenceClickListener {
                showSignOutConfirmation()
            }
        }
    }

    private fun showSignOutConfirmation(): Boolean {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sign_out_confirmation_title))
            .setMessage(resources.getString(R.string.sign_out_confirmation_message))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.sign_out_confirmation_positive)) { dialog, which ->
                // Respond to positive button press
            }
            .show()

        return true
    }
}
