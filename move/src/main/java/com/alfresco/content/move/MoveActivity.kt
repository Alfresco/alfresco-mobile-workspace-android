package com.alfresco.content.move

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.airbnb.mvrx.MavericksView
import com.alfresco.content.move.widget.ActionBarController
import com.alfresco.content.move.widget.MoveActionBarLayout

/**
 * Marked as MoveActivity class
 */
class MoveActivity : AppCompatActivity(), MavericksView {

//    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move)

        configure()
    }

    private fun configure() {
        val graph = navController.navInflater.inflate(R.navigation.nav_move_paths)
        graph.startDestination = R.id.nav_move
        val bundle = Bundle().apply {
        }
        navController.setGraph(graph, bundle)

        val moveActionBarLayout = findViewById<MoveActionBarLayout>(R.id.toolbar_move)

        actionBarController = ActionBarController(moveActionBarLayout)
        actionBarController.setupActionBar(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (navController.currentDestination?.id == R.id.nav_browse_move) {
            finish()
            false
        } else navController.navigateUp()
    }

    override fun invalidate() {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
