package com.alfresco.content.app.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as ExtensionActivity class
 */
class ExtensionActivity : AppCompatActivity(), MavericksView {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController
    private var signedOutDialog = WeakReference<AlertDialog>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                println("EXTENSION SINGLE IMAGE")
                if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                println("EXTENSION MULTIPLE IMAGE")
                if (intent.type?.startsWith("image/") == true) {
                    handleSendMultipleImages(intent)
                }
            }
            else -> println("EXTENSION OTHER INTENT")
        }

        configure()
    }

    private fun configure() = withState(viewModel) { state ->
        val graph = navController.navInflater.inflate(R.navigation.nav_share_extension)
        graph.startDestination = R.id.nav_extension
        navController.graph = graph

        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.showActionBarLayout()
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }

    override fun invalidate() = withState(viewModel) { state ->

        if (state.requiresReLogin) {
            if (state.isOnline) {
                showSignedOutPrompt()
            }
        } else {
            // Only when logged in otherwise triggers re-login prompts
            actionBarController.setProfileIcon(viewModel.profileIcon)
        }

        actionBarController.setOnline(state.isOnline)
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
}
