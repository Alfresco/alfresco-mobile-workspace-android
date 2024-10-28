package com.alfresco.content.browse.processes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.alfresco.content.actions.Action
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.ActivityTaskViewerBinding

/**
 * Marked as ProcessDetailActivity class
 */
class ProcessDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskViewerBinding

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
        graph.setStartDestination(R.id.nav_process_details)
        navController.setGraph(graph, intent.extras)
    }

    private fun setupActionToasts() =
        Action.showActionToasts(
            lifecycleScope,
            binding.parentView,
            binding.bottomView,
        )
}
