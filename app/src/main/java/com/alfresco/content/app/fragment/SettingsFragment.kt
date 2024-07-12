package com.alfresco.content.app.fragment

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
import com.alfresco.content.app.activity.MainActivity
import com.alfresco.content.app.loadAny
import com.alfresco.content.app.widget.AccountPreference
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

@Suppress("unused")
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onStart() {
        super.onStart()
        requireActivity().title = resources.getString(R.string.nav_title_settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val acc = SessionManager.requireSession.account
        preferenceScreen.findPreference<AccountPreference>(resources.getString(R.string.pref_account_key))?.apply {
            accessibilityTextUserInfo = getString(R.string.accessibility_text_user_info, acc.displayName, acc.email)
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

        preferenceScreen.findPreference<Preference>(resources.getString(R.string.pref_version_key))?.apply {
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
            .setPositiveButton(resources.getString(R.string.sign_out_confirmation_positive)) { _, _ ->
                logout()
            }
            .show()

        return true
    }

    private fun logout() {
        val acc = SessionManager.requireSession.account
        val i = Intent(context, LogoutActivity::class.java)
        i.putExtra(LogoutViewModel.EXTRA_HOST_NAME, acc.hostName)
        i.putExtra(LogoutViewModel.EXTRA_CLIENT_ID, acc.clientId)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_TYPE, acc.authType)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_CONFIG, acc.authConfig)
        i.putExtra(LogoutViewModel.EXTRA_AUTH_STATE, acc.authState)
        startActivityForResult(i, REQUEST_CODE_LOGOUT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_LOGOUT) {
            // no-op
            deleteAccount()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun deleteAccount() {
        // Extra cleanup before removing the account
        SearchRepository().clearRecentSearch()
        OfflineRepository().cleanup()
        TaskRepository().clearAPSData()

        // Actual account removal
        val weakRef = WeakReference(requireActivity())
        Account.delete(requireContext().applicationContext) {
            val activity = weakRef.get() ?: return@delete
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
        }
    }

    companion object {
        const val REQUEST_CODE_LOGOUT = 0
    }
}
