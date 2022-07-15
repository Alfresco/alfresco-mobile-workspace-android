package com.alfresco.content.browse.tasks

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.listview.tasks.TaskListFragment

/**
 * Marked as TasksFragment
 */
class TasksFragment : TaskListFragment<TasksViewModel, TasksViewState>() {

    override val viewModel: TasksViewModel by fragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
