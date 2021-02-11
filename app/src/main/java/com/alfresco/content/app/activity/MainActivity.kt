package com.alfresco.content.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.BaseMvRxActivity
import com.alfresco.content.actions.Action
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.session.SessionManager
import com.alfresco.download.DownloadMonitor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

class MainActivity : BaseMvRxActivity() {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }
    private lateinit var actionBarController: ActionBarController
    private var signedOutDialog = WeakReference<AlertDialog>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check login during creation for faster transition on startup
        if (viewModel.requiresLogin) {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        } else {
            configure()
        }

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun configure() {
        navController.setGraph(R.navigation.nav_bottom)

        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController, appBarConfiguration)

        bottomNav.setupWithNavController(navController)

        setupActionToasts()
        setupDownloadNotifications()
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

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    private fun checkInvalidLogin(state: MainActivityState) {
        if (state.requiresReLogin) {
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

    private fun navigateToReLogin() {
        val i = Intent(this, LoginActivity::class.java)
        val acc = SessionManager.requireSession.account
        i.putExtra(LoginViewModel.EXTRA_ENDPOINT, acc.serverUrl)
        i.putExtra(LoginViewModel.EXTRA_AUTH_TYPE, acc.authType)
        i.putExtra(LoginViewModel.EXTRA_AUTH_CONFIG, acc.authConfig)
        i.putExtra(LoginViewModel.EXTRA_AUTH_STATE, acc.authState)
        startActivity(i)
    }

    private fun setupActionToasts() =
        Action.showActionToasts(
            lifecycleScope,
            findViewById(android.R.id.content),
            bottomNav
        )

    private fun setupDownloadNotifications() =
        DownloadMonitor
            .smallIcon(R.drawable.ic_notification_small)
            .tint(primaryColor(this))
            .observe(this)

    private fun primaryColor(context: Context): Int {
        val typedValue = TypedValue()
        val arr = context.obtainStyledAttributes(
            typedValue.data,
            intArrayOf(R.attr.colorPrimary)
        )
        val color = arr.getColor(0, 0)
        arr.recycle()
        return color
    }
}
