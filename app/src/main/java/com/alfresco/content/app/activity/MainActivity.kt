package com.alfresco.content.app.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.auth.activity.LoginViewModel
import com.alfresco.auth.ui.observe
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ContextualActionsSheet
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.data.CommonRepository.Companion.KEY_FEATURES_MOBILE
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.MenuActions
import com.alfresco.content.data.MobileConfigDataEntry
import com.alfresco.content.data.MultiSelection
import com.alfresco.content.data.Settings.Companion.IS_PROCESS_ENABLED_KEY
import com.alfresco.content.data.getJsonFromSharedPrefs
import com.alfresco.content.session.SessionManager
import com.alfresco.content.slideBottom
import com.alfresco.content.slideTop
import com.alfresco.content.viewer.ViewerActivity
import com.alfresco.content.viewer.ViewerArgs.Companion.ID_KEY
import com.alfresco.content.viewer.ViewerArgs.Companion.KEY_FOLDER
import com.alfresco.content.viewer.ViewerArgs.Companion.MODE_KEY
import com.alfresco.content.viewer.ViewerArgs.Companion.TITLE_KEY
import com.alfresco.download.DownloadMonitor
import com.alfresco.ui.getColorForAttribute
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Marked as MainActivity class
 */
class MainActivity : AppCompatActivity(), MavericksView, ActionMode.Callback {

    @OptIn(InternalMavericksApi::class)
    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private var navHostFragment: NavHostFragment? = null
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }
    private var actionBarController: ActionBarController? = null
    private var signedOutDialog = WeakReference<AlertDialog>(null)
    private var isNewIntent = false
    private var actionMode: ActionMode? = null
    var mobileConfigDataEntry: MobileConfigDataEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mobileConfigDataEntry = getJsonFromSharedPrefs(this, KEY_FEATURES_MOBILE)
        observe(viewModel.navigationMode, ::navigateTo)

        GlobalScope.launch {
            MultiSelection.observeMultiSelection().collect {
                Handler(Looper.getMainLooper()).post {
                    viewModel.entriesMultiSelection = it.selectedEntries
                    if (it.isMultiSelectionEnabled) {
                        viewModel.path = it.path
                        enableMultiSelection(it.selectedEntries)
                    } else {
                        disableMultiSelection()
                    }
                }
            }
        }

        viewModel.handleDataIntent(
            intent.extras?.getString(MODE_KEY, ""),
            intent.extras?.getBoolean(KEY_FOLDER, false) ?: false,
        )

        // Check login during creation for faster transition on startup

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        viewModel.isProcessEnabled = {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = sharedPrefs.edit()
            editor.putBoolean(IS_PROCESS_ENABLED_KEY, it)
            editor.apply()
        }

        if (savedInstanceState != null && viewModel.entriesMultiSelection.isNotEmpty()) {
            enableMultiSelection(viewModel.entriesMultiSelection)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkIfAPSEnabled()
    }

    private fun navigateTo(mode: MainActivityViewModel.NavigationMode) {
        val data = Triple(
            intent.extras?.getString(ID_KEY, "") ?: "",
            intent.extras?.getString(MODE_KEY, "") ?: "",
            "Preview",
        )

        when (mode) {
            MainActivityViewModel.NavigationMode.FOLDER -> {
                bottomNav.selectedItemId = R.id.nav_browse
            }

            MainActivityViewModel.NavigationMode.FILE -> {
                removeShareData()
                if (!isNewIntent) checkLogin(data)
                navigateToViewer(data)
            }

            MainActivityViewModel.NavigationMode.LOGIN -> navigateToLogin(data)
            MainActivityViewModel.NavigationMode.DEFAULT -> checkLogin(data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        isNewIntent = true
        viewModel.handleDataIntent(
            intent?.extras?.getString(MODE_KEY, ""),
            intent?.extras?.getBoolean(KEY_FOLDER, false) ?: false,
        )
    }

    private fun navigateToViewer(data: Triple<String, String, String>) {
        startActivity(
            Intent(this, ViewerActivity::class.java)
                .putExtra(ID_KEY, data.first)
                .putExtra(MODE_KEY, data.second)
                .putExtra(TITLE_KEY, data.third),
        )
    }

    private fun checkLogin(data: Triple<String, String, String>) {
        if (viewModel.requiresLogin) {
            navigateToLogin(data)
        } else {
            configure()
        }
    }

    private fun removeShareData() {
        intent.replaceExtras(Bundle())
        intent.data = null
    }

    private fun navigateToLogin(data: Triple<String, String, String>) {
        val i = Intent(this, LoginActivity::class.java)
        intent.extras?.let { i.putExtras(it) }
        startActivity(i)
        finish()
    }

    private fun configure() = withState(viewModel) { state ->
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val graph = navController.navInflater.inflate(R.navigation.nav_bottom)
        graph.setStartDestination(if (state.isOnline) R.id.nav_recents else R.id.nav_offline)
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(bottomNav.menu)
        actionBarController = ActionBarController(findViewById(R.id.toolbar))
        actionBarController?.setupActionBar(this, navController, appBarConfiguration)

        bottomNav.setupWithNavController(navController)

        setupActionToasts()
//        MoveResultContract.addMoveIntent(Intent(this, MoveActivity::class.java))
        setupDownloadNotifications()

        bottomNav.setOnItemSelectedListener { item ->
            // In order to get the expected behavior, you have to call default Navigation method manually
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }
    }

    override fun invalidate() = withState(viewModel) { state ->

        if (state.requiresReLogin) {
            if (state.isOnline) {
                showSignedOutPrompt()
            }
        } else {
            viewModel.checkIfAPSEnabled()
            // Only when logged in otherwise triggers re-login prompts
            actionBarController?.setProfileIcon(viewModel.profileIcon)
        }
        if (actionBarController != null) {
            actionBarController?.setOnline(state.isOnline)
        }
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    private fun showSignedOutPrompt() {
        val oldDialog = signedOutDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.auth_signed_out_title)).setMessage(resources.getString(R.string.auth_signed_out_subtitle))
            .setNegativeButton(resources.getString(R.string.sign_out_confirmation_negative), null).setPositiveButton(resources.getString(R.string.auth_basic_sign_in_button)) { _, _ ->
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
        i.putExtra(ID_KEY, intent.extras?.getString(ID_KEY, ""))
        i.putExtra(MODE_KEY, intent.extras?.getString(MODE_KEY, ""))
        i.putExtra(KEY_FOLDER, intent.extras?.getBoolean(KEY_FOLDER, false))
        startActivity(i)
    }

    private fun setupActionToasts() = Action.showActionToasts(
        lifecycleScope,
        findViewById(android.R.id.content),
        bottomNav,
    )

    private fun setupDownloadNotifications() = DownloadMonitor.smallIcon(R.drawable.ic_notification_small).tint(primaryColor(this)).observe(this)

    private fun primaryColor(context: Context) = context.getColorForAttribute(R.attr.colorPrimary)

    private fun enableMultiSelection(selectedEntries: List<Entry>) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(this)
        }
        viewModel.entriesMultiSelection = selectedEntries
        val title = SpannableString(getString(R.string.title_action_mode, selectedEntries.size))
        title.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorActionMode)), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        actionMode?.title = title

        actionBarController?.showHideActionBarLayout(false)
        bottomNav.slideBottom()
        if (bottomNav.isVisible) {
            bottomNav.visibility = View.GONE
        }
        MultiSelection.clearSelectionChangedFlow.tryEmit(false)
    }

    private fun disableMultiSelection() {
        viewModel.entriesMultiSelection = emptyList()
        actionMode?.finish()
        actionBarController?.showHideActionBarLayout(true)
        bottomNav.slideTop()
        if (!bottomNav.isVisible) {
            bottomNav.visibility = View.VISIBLE
        }
        MultiSelection.clearSelectionChangedFlow.tryEmit(true)
        actionMode = null
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.apply {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.menu_action_multi_selection, menu)
            return true
        }
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val isMoveActionEnabled = mobileConfigDataEntry?.featuresMobile?.menus?.find { it.id.lowercase() == MenuActions.Move.value.lowercase() }?.enabled ?: false
        if ((viewModel.path.isNotEmpty() && viewModel.path == getString(com.alfresco.content.browse.R.string.nav_path_trash)) ||
            navHostFragment?.navController?.currentDestination?.id == R.id.nav_offline || !isMoveActionEnabled
        ) {
            menu?.findItem(R.id.move)?.isVisible = false
        }
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.move -> {
                withState(viewModel) { state ->
                    if (state.isOnline) {
                        viewModel.moveFilesFolder()
                        disableMultiSelection()
                    } else {
                        Snackbar.make(bottomNav, com.alfresco.content.actions.R.string.message_no_internet, Snackbar.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            R.id.more_vert -> {
                showCreateSheet()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        disableMultiSelection()
    }

    private fun showCreateSheet() = withState(viewModel) {
        ContextualActionsSheet.with(
            ContextualActionData.withEntries(
                viewModel.entriesMultiSelection,
                true,
                mobileConfigData = mobileConfigDataEntry,
            ),
        ).show(supportFragmentManager, null)
    }
}
