package com.alfresco.content.app.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import coil.transform.CircleCropTransformation
import com.alfresco.auth.activity.LogoutActivity
import com.alfresco.auth.activity.LogoutViewModel
import com.alfresco.content.account.Account
import com.alfresco.content.app.R
import com.alfresco.content.app.activity.LoginActivity
import com.alfresco.content.app.loadAny
import com.alfresco.content.app.widget.AccountPreference
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onStart() {
        super.onStart()
        requireActivity().title = resources.getString(R.string.nav_title_settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val acc = SessionManager.requireSession.account
        preferenceScreen.findPreference<AccountPreference>(resources.getString(R.string.settings_account_key))?.apply {
            title = acc.displayName
            summary = acc.email
            loadAny(PeopleRepository.myPicture()) {
                placeholder(R.drawable.ic_transparent)
                error(R.drawable.ic_transparent)
                transformations(CircleCropTransformation())
            }
            onSignOutClickListener = View.OnClickListener {
                showSignOutConfirmation()
            }
        }

        preferenceScreen.findPreference<Preference>(resources.getString(R.string.settings_version_key))?.apply {
            val pkg = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val version = "${pkg.versionName}-${PackageInfoCompat.getLongVersionCode(pkg)}"
            summary = resources.getString(R.string.settings_version, version)
        }
    }

    private fun showSignOutConfirmation(): Boolean {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sign_out_confirmation_title))
            .setMessage(resources.getString(R.string.sign_out_confirmation_message))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.sign_out_confirmation_positive)) { dialog, which ->
                logout()
            }
            .show()

        return true
    }

    private fun logout() {
        val acc = SessionManager.requireSession.account
        val i = Intent(context, LogoutActivity::class.java)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_TYPE, acc.authType)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_CONFIG, acc.authConfig)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_STATE, acc.authState)
        startActivityForResult(i, REQUEST_CODE_LOGOUT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_LOGOUT) {
            if (resultCode == Activity.RESULT_OK) {
                deleteAccount()
            } else {
                // no-op
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun deleteAccount() {
        // Extra cleanup before removing the account
        SearchRepository().clearRecentSearch()

        // Actual account removal
        Account.delete(requireActivity()) {
            requireActivity().startActivity(Intent(activity, LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    companion object {
        const val REQUEST_CODE_LOGOUT = 0
    }
}
