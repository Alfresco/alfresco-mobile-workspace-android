package com.alfresco.content.app.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.mvrx.MavericksView
import com.alfresco.content.actions.Action
import com.alfresco.content.app.R
import com.alfresco.content.app.databinding.ActivityProcessBinding
import com.alfresco.content.app.widget.ActionBarController
import com.alfresco.content.app.widget.ActionBarLayout
import com.alfresco.content.common.BaseActivity

class ProcessActivity : BaseActivity(), MavericksView {

    private lateinit var binding: ActivityProcessBinding
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        configureNav()
        setupActionToasts()
    }

    private fun configureNav() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_process_paths)
        navController.setGraph(graph, intent.extras)
        val actionBarLayout = findViewById<ActionBarLayout>(R.id.toolbar)
        actionBarController = ActionBarController(actionBarLayout)
        actionBarController.setupActionBar(this, navController)

        actionBarLayout.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupActionToasts() = Action.showActionToasts(
        lifecycleScope,
        binding.parentView,
        binding.bottomView,
    )

    override fun invalidate() {
    }
}
