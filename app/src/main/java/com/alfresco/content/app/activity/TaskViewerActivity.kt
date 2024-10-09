package com.alfresco.content.app.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.content.actions.Action
import com.alfresco.content.activityViewModel
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.ActivityTaskViewerBinding
import com.alfresco.content.common.BaseActivity
import com.alfresco.content.session.SessionManager
import com.alfresco.content.viewer.ViewerArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as TaskViewerActivity class
 */
class TaskViewerActivity : BaseActivity(), MavericksView {
    private lateinit var binding: ActivityTaskViewerBinding

    @OptIn(InternalMavericksApi::class)
    private val viewModel: MainActivityViewModel by activityViewModel()
    private var signedOutDialog = WeakReference<AlertDialog>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureNav()
        setupActionToasts()
    }

    private fun configureNav() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_task_paths)
        navController.setGraph(graph, intent.extras)
    }

    override fun invalidate() =
        withState(viewModel) { state ->
            if (state.requiresReLogin) {
                if (state.isOnline) {
                    showSignedOutPrompt()
                }
            }
        }

    private fun showSignedOutPrompt() {
        val oldDialog = signedOutDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog =
            MaterialAlertDialogBuilder(this).setTitle(resources.getString(com.alfresco.content.app.R.string.auth_signed_out_title))
                .setMessage(resources.getString(com.alfresco.content.app.R.string.auth_signed_out_subtitle))
                .setNegativeButton(resources.getString(com.alfresco.content.app.R.string.sign_out_confirmation_negative), null)
                .setPositiveButton(resources.getString(com.alfresco.content.app.R.string.auth_basic_sign_in_button)) { _, _ ->
                    navigateToReLogin()
                }.show()
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
        i.putExtra(ViewerArgs.ID_KEY, intent.extras?.getString(ViewerArgs.ID_KEY, ""))
        i.putExtra(ViewerArgs.MODE_KEY, intent.extras?.getString(ViewerArgs.MODE_KEY, ""))
        i.putExtra(ViewerArgs.KEY_FOLDER, intent.extras?.getBoolean(ViewerArgs.KEY_FOLDER, false))
        startActivity(i)
    }

    private fun setupActionToasts() =
        Action.showActionToasts(
            lifecycleScope,
            binding.root,
            binding.bottomView,
        )
}
