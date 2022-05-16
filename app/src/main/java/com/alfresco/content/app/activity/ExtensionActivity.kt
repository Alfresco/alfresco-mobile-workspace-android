package com.alfresco.content.app.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.alfresco.content.data.BrowseRepository.Companion.SHARE_MULTIPLE_URI
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import java.lang.ref.WeakReference
import kotlinx.coroutines.GlobalScope

/**
 * Marked as ExtensionActivity class
 */
class ExtensionActivity : BaseActivity(), MavericksView, ActionPermission {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController
    private var alertDialog = WeakReference<AlertDialog>(null)
    private val shareLimit = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        screenType = ScreenType.ExtensionActivity
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_extension)

        ActionExtension.showActionExtensionToasts(
            lifecycleScope,
            findViewById(android.R.id.content)
        )
        ActionPermission.showActionPermissionToasts(
            lifecycleScope,
            findViewById(android.R.id.content)
        )

        executePermission(this, GlobalScope)
    }

    private fun executeIntentData() {
        if (viewModel.requiresLogin) {
            showAlertPrompt(
                resources.getString(R.string.auth_login_app_title),
                resources.getString(R.string.auth_login_app_subtitle),
                resources.getString(R.string.auth_login_app_ok_button),
                null,
                AlertType.TYPE_NO_LOGIN
            )
            return
        }
        intent?.let { intentObj ->
            if (intentObj.hasExtra(LoginViewModel.EXTRA_IS_LOGIN) &&
                intentObj.getBooleanExtra(LoginViewModel.EXTRA_IS_LOGIN, false)
            ) {
                configure()
            } else {
                when (intent?.action) {
                    Intent.ACTION_SEND -> {
                        handleFiles(intent, false)
                    }
                    Intent.ACTION_SEND_MULTIPLE -> {
                        handleFiles(intent, true)
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

    private fun handleFiles(intent: Intent, isMultipleFiles: Boolean) {

        if (!isMultipleFiles) {
            (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                saveShareData(arrayOf(it.toString()))
            }
        } else {
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
    }

    private fun saveShareData(list: Array<String>) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()
        val jsonString = Gson().toJson(list)
        editor.putString(SHARE_MULTIPLE_URI, jsonString)
        editor.apply()
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (viewModel.readPermission) {
            if (state.requiresReLogin && state.isOnline) {
                showAlertPrompt(
                    resources.getString(R.string.auth_signed_out_title),
                    resources.getString(R.string.auth_signed_out_subtitle),
                    resources.getString(R.string.auth_basic_sign_in_button),
                    resources.getString(R.string.sign_out_confirmation_negative),
                    AlertType.TYPE_SIGN_OUT
                )
            } else if (!state.isOnline)
                showAlertPrompt(
                    resources.getString(R.string.auth_internet_unavailable_title),
                    resources.getString(R.string.auth_internet_unavailable_subtitle),
                    resources.getString(R.string.auth_internet_unavailable_ok_button),
                    null,
                    AlertType.TYPE_INTERNET_UNAVAILABLE
                )
        }
    }

    private fun showAlertPrompt(title: String, message: String, positive: String, negative: String? = null, type: AlertType) {
        val oldDialog = alertDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog: AlertDialog
        if (negative != null) {
            dialog = MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(negative, null)
                .setPositiveButton(positive) { _, _ ->
                    when (type) {
                        AlertType.TYPE_SIGN_OUT -> navigateToReLogin()
                        else -> {
                            Logger.d(getString(R.string.no_type))
                        }
                    }
                }
                .show()
        } else {
            dialog = MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positive) { _, _ ->
                    when (type) {
                        AlertType.TYPE_NO_LOGIN -> {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        AlertType.TYPE_INTERNET_UNAVAILABLE -> finish()
                        else -> {
                            Logger.d(getString(R.string.no_type))
                        }
                    }
                }
                .show()
        }
        alertDialog = WeakReference(dialog)
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
}

/**
 * Marked as AlertType
 */
enum class AlertType {
    TYPE_SIGN_OUT,
    TYPE_INTERNET_UNAVAILABLE,
    TYPE_NO_LOGIN
}
