package com.alfresco.content.app.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.search.SearchFragment
import com.alfresco.content.session.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }
    private lateinit var menu: Menu
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController, appBarConfiguration)

        bottomNav.setupWithNavController(navController)

        updateAppTheme()

        // Start a new session
        val session = SessionManager.newSession(this)

        // On empty session show login
        if (session == null) {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }

        // Else update logged in state
        actionBarController.refreshData()

        session?.onSignedOut() {
            runOnUiThread {
                showSignedOutPrompt()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.settings_theme_key) -> updateAppTheme()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_action_bar, menu)
        actionBarController.setupOptionsMenu(menu)

        val searchItem: MenuItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                (getForegroundFragment() as? SearchFragment)?.setSearchQuery(newText ?: "")
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })

        return true
    }

    private fun showSignedOutPrompt() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.auth_signed_out_title))
            .setMessage(resources.getString(R.string.auth_signed_out_subtitle))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null)
            .setPositiveButton(resources.getString(R.string.auth_basic_sign_in_button)) { dialog, which ->
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

    private fun getForegroundFragment(): Fragment? {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    private fun updateAppTheme() {
        when (PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_system))) {
            getString(R.string.settings_theme_light) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            getString(R.string.settings_theme_dark) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            getString(R.string.settings_theme_system) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
