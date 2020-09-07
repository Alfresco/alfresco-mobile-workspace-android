package com.alfresco.content.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.data.AuthenticationRepository
import com.alfresco.content.session.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController, appBarConfiguration)

        bottomNav.setupWithNavController(navController)

        // Start a new session
        val session = SessionManager.newSession(this)

        // On empty session show login
        if (session == null) {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        } else {
            // TODO: Temporary
            lifecycleScope.launch {
                try {
                    session.ticket = AuthenticationRepository().fetchTicket()
                } catch (_: Exception) {
                    
                }
            }
        }

        // Else update logged in state
        actionBarController.refreshData()

        session?.onSignedOut() {
            runOnUiThread {
                showSignedOutPrompt()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    private fun showSignedOutPrompt() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.auth_signed_out_title))
            .setMessage(resources.getString(R.string.auth_signed_out_subtitle))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.auth_basic_sign_in_button)) { _, _ ->
                navigateToReLogin()
            }
            .show()
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
}
