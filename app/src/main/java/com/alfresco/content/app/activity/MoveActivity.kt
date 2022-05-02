package com.alfresco.content.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController

/**
 * Marked as ExtensionActivity class
 */
class MoveActivity : AppCompatActivity(), MavericksView {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension)

        configure()

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

    override fun invalidate() = withState(viewModel) { state ->

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
