package com.alfresco.content.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.DiscoveryService
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.auth.activity.LoginViewModel.Companion.DISTRIBUTION_VERSION
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.MoveResultContract
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.session.SessionManager
import com.alfresco.download.DownloadMonitor
import com.alfresco.ui.getColorForAttribute
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), MavericksView {

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
            lifecycleScope.launch { checkDistribution() }
            configure()
        }

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private suspend fun checkDistribution() {

        val acc = SessionManager.requireSession.account

        val authConfig = AuthConfig.jsonDeserialize(acc.authConfig)

        authConfig?.let { config ->

            val discoveryService = DiscoveryService(this, config)

            val contentServiceDetailsObj = withContext(Dispatchers.IO) {
                discoveryService.getContentServiceDetails(Uri.parse(acc.serverUrl).host ?: "")
            }

            contentServiceDetailsObj?.let { data ->
                // Save state to persistent storage
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = sharedPrefs.edit()
                editor.putString(DISTRIBUTION_VERSION, data.edition)
                editor.apply()
            }
        }
    }

    private fun configure() = withState(viewModel) { state ->
        val graph = navController.navInflater.inflate(R.navigation.nav_bottom)
        graph.startDestination = if (state.isOnline) R.id.nav_recents else R.id.nav_offline
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController, appBarConfiguration)

        bottomNav.setupWithNavController(navController)

        setupActionToasts()
        MoveResultContract.addMoveIntent(Intent(this, MoveActivity::class.java))
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
        i.putExtra(LoginViewModel.EXTRA_IS_EXTENSION, false)
        i.putExtra(LoginViewModel.EXTRA_ENDPOINT, acc.serverUrl)
        i.putExtra(LoginViewModel.EXTRA_AUTH_TYPE, acc.authType)
        i.putExtra(LoginViewModel.EXTRA_AUTH_CONFIG, acc.authConfig)
        i.putExtra(LoginViewModel.EXTRA_AUTH_STATE, acc.authState)
        startActivity(i)
    }

    private fun setupActionToasts() = Action.showActionToasts(
        lifecycleScope,
        findViewById(android.R.id.content),
        bottomNav
    )

    private fun setupDownloadNotifications() =
        DownloadMonitor
            .smallIcon(R.drawable.ic_notification_small)
            .tint(primaryColor(this))
            .observe(this)

    private fun primaryColor(context: Context) =
        context.getColorForAttribute(R.attr.colorPrimary)
}
