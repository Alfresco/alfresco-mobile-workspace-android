package com.alfresco.content.app.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.auth.activity.LoginViewModel.Companion.EXTRA_IS_LOGIN
import com.alfresco.content.actions.ActionExtension
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.data.BrowseRepository.Companion.SHARE_MULTIPLE_URI
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.lang.ref.WeakReference

/**
 * Marked as ExtensionActivity class
 */
class ExtensionActivity : AppCompatActivity(), MavericksView {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController
    private var signedOutDialog = WeakReference<AlertDialog>(null)
    private var internetUnavailableDialog = WeakReference<AlertDialog>(null)
    private val shareLimit = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension)

        intent?.let { intentObj ->
            if (intentObj.hasExtra(EXTRA_IS_LOGIN) && intentObj.getBooleanExtra(EXTRA_IS_LOGIN, false)) {
                configure()
                return
            } else {
                when (intent?.action) {
                    Intent.ACTION_SEND -> {
                        handleSingleFile(intent)
                    }
                    Intent.ACTION_SEND_MULTIPLE -> {
                        handleMultipleFiles(intent)
                    }
                }
                configure()
            }
        }
    }

    private fun configure() = withState(viewModel) {
        val graph = navController.navInflater.inflate(R.navigation.nav_share_extension)
        graph.startDestination = R.id.nav_extension
        navController.graph = graph

        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController)

        setupActionExtensionToasts()
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController.currentDestination?.id == R.id.nav_browse_extension) {
            finish()
            false
        } else
            navController.navigateUp()
    }

    private fun handleSingleFile(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            saveShareData(arrayOf(it.toString()))
        }
    }

    private fun handleMultipleFiles(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            if (it.size > shareLimit) {
                Toast.makeText(this, getString(R.string.share_limit_message), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val list = Array(it.size) { "" }
                it.forEachIndexed { index, obj ->
                    list[index] = (obj as Uri).toString()
                }
                saveShareData(list)
            }
        }
    }

    private fun saveShareData(list: Array<String>) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()
        val jsonString = Gson().toJson(list)
        editor.putString(SHARE_MULTIPLE_URI, jsonString)
        editor.apply()
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.requiresReLogin) {
            if (state.isOnline) {
                showSignedOutPrompt()
            }
        } else {
        }
    }

    private fun showSignedOutPrompt() {
        val oldDialog = signedOutDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.auth_signed_out_title))
            .setMessage(resources.getString(R.string.auth_signed_out_subtitle))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.auth_basic_sign_in_button)) { _, _ ->
                navigateToReLogin()
            }
            .show()
        signedOutDialog = WeakReference(dialog)
    }

    private fun showInternetUnavailablePrompt() {
        val oldDialog = internetUnavailableDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.auth_internet_unavailable_title))
            .setMessage(resources.getString(R.string.auth_internet_unavailable_subtitle))
            .setPositiveButton(resources.getString(R.string.auth_internet_unavailable_ok_button)) { _, _ ->
            }
            .show()
        internetUnavailableDialog = WeakReference(dialog)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("ExtensionActivity.onNewIntent")
    }

    private fun navigateToReLogin() {
        val i = Intent(this, LoginActivity::class.java)
        val acc = SessionManager.requireSession.account
        i.putExtra(LoginViewModel.EXTRA_IS_EXTENSION, true)
        i.putExtra(LoginViewModel.EXTRA_ENDPOINT, acc.serverUrl)
        i.putExtra(LoginViewModel.EXTRA_AUTH_TYPE, acc.authType)
        i.putExtra(LoginViewModel.EXTRA_AUTH_CONFIG, acc.authConfig)
        i.putExtra(LoginViewModel.EXTRA_AUTH_STATE, acc.authState)
        startActivity(i)
        finish()
    }

    private fun setupActionExtensionToasts() =
        ActionExtension.showActionExtensionToasts(
            lifecycleScope,
            findViewById(android.R.id.content)
        )
}
