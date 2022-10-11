package com.alfresco.content.browse.tasks

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.alfresco.content.actions.Action
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.ActivityTaskViewerBinding
import com.alfresco.content.data.TaskEntry
import kotlinx.parcelize.Parcelize

/**
 * Mark as TaskDetailsArgs class
 */
@Parcelize
data class TaskDetailsArgs(
    val taskObj: TaskEntry?
) : Parcelable {
    companion object {
        const val TASK_OBJ = "taskObj"

        /**
         * return the TaskDetailsArgs obj
         */
        fun with(args: Bundle): TaskDetailsArgs {
            return TaskDetailsArgs(
                args.getParcelable(TASK_OBJ)
            )
        }
    }
}

/**
 * Marked as TaskViewerActivity class
 */
class TaskViewerActivity : AppCompatActivity() {

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
        navController.setGraph(graph, intent.extras)
    }

    private fun setupActionToasts() = Action.showActionToasts(
        lifecycleScope,
        findViewById(android.R.id.content),
        binding.navHostFragment
    )
}
