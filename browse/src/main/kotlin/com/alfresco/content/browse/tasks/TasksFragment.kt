package com.alfresco.content.browse.tasks

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.tasks.TaskListFragment
import kotlinx.parcelize.Parcelize

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

    override fun onItemClicked(entry: TaskEntry) {
    }
}
