package com.alfresco.content.app.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.MoveResultContract.Companion.ENTRY_OBJ_KEY
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.data.Entry
import com.alfresco.content.session.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as MoveActivity class
 */
class MoveActivity : AppCompatActivity(), MavericksView {

    @OptIn(InternalMavericksApi::class)
    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomView by lazy { findViewById<View>(R.id.bottom_view) }
    private lateinit var actionBarController: ActionBarController
    private var signedOutDialog = WeakReference<AlertDialog>(null)
    private var entryObj: Entry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move)

        if (intent.extras != null) {
            entryObj = intent.getParcelableExtra(ENTRY_OBJ_KEY) as Entry?
        }

        configure()

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun configure() {
        val graph = navController.navInflater.inflate(R.navigation.nav_move_paths)
        graph.setStartDestination(R.id.nav_move)
        val bundle = Bundle().apply {
            putParcelable(ENTRY_OBJ_KEY, entryObj)
        }
        navController.setGraph(graph, bundle)
        setupActionToasts()
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController.setupActionBar(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController.currentDestination?.id == R.id.nav_browse_move) {
            finish()
            false
        } else navController.navigateUp()
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
        bottomView,
    )

    override fun onBackPressed() {
        onSupportNavigateUp()
    }
}
