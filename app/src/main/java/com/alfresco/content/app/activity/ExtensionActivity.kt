package com.alfresco.content.app.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.Logger
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.actions.ActionExtension
import com.alfresco.content.actions.ActionPermission
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.data.BrowseRepository.Companion.LOGIN_SESSION_STATUS
import com.alfresco.content.data.BrowseRepository.Companion.SHARE_MULTIPLE_URI
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import java.lang.ref.WeakReference

/**
 * Marked as ExtensionActivity class
 */
class ExtensionActivity : AppCompatActivity(), MavericksView, ActionPermission {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController
    private var signedOutDialog = WeakReference<AlertDialog>(null)
    private var internetUnavailableDialog = WeakReference<AlertDialog>(null)
    private var loginAppDialog = WeakReference<AlertDialog>(null)
    private val shareLimit = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension)
        setupActionExtensionToasts()
        executePermission(this, GlobalScope)
    }

    private fun executeIntentData() {
        if (viewModel.requiresLogin) {
            showLoginAppPrompt()
            return
        }
        intent?.let { intentObj ->
            if (intentObj.hasExtra(LoginViewModel.EXTRA_IS_LOGIN) && intentObj.getBooleanExtra(LoginViewModel.EXTRA_IS_LOGIN, false)) {
                saveLoginSessionStatus()
                configure()
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
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController.currentDestination?.id == R.id.nav_browse_extension) {
            finish()
            false
        } else navController.navigateUp()
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

    private fun saveLoginSessionStatus() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()
        editor.putBoolean(LOGIN_SESSION_STATUS, true)
        editor.apply()
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (viewModel.readPermission && state.requiresReLogin && state.isOnline) {
            showSignedOutPrompt()
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

    private fun showLoginAppPrompt() {
        val oldDialog = loginAppDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.auth_login_app_title))
            .setMessage(resources.getString(R.string.auth_login_app_subtitle))
            .setPositiveButton(resources.getString(R.string.auth_login_app_ok_button)) { _, _ ->
                finish()
            }
            .show()
        loginAppDialog = WeakReference(dialog)
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

    private fun setupActionExtensionToasts() {
        ActionExtension.showActionExtensionToasts(
            lifecycleScope,
            findViewById(android.R.id.content)
        )
        ActionPermission.showActionPermissionToasts(
            lifecycleScope,
            findViewById(android.R.id.content)
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override suspend fun executeIntentData(context: Context) {
        viewModel.readPermission = true
        runOnUiThread {
            executeIntentData()
        }
    }

    override fun showToast(view: View, anchorView: View?) {
        Logger.d("Read Permission Granted")
    }
}
