package com.alfresco.content.browse.tasks

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.TaskFilters
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.Tasks
import com.alfresco.content.data.Tasks.Active
import com.alfresco.content.data.Tasks.Completed
import com.alfresco.content.listview.tasks.TaskListViewModel
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

class TasksViewModel(
    state: TasksViewState,
    val context: Context
) : TaskListViewModel<TasksViewState>(state) {

    init {
        fetchInitial()
    }

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val newPage = state.page.plus(1)
        viewModelScope.launch {
            // Fetch tasks data
            TaskRepository()::getTasks.asFlow(
                getSelectedTasks(state.displayTask, newPage)
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        update(it()).copy(request = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    override fun emptyMessageArgs(state: TaskListViewState): Triple<Int, Int, Int> {
        return Triple(R.drawable.ic_empty_recent, R.string.tasks_empty_title, R.string.tasks_empty_message)
    }

    private fun fetchInitial() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks data
            TaskRepository()::getTasks.asFlow(
                TaskFilters.active()
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        update(it()).copy(request = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun getSelectedTasks(tasks: Tasks, page: Int): TaskFilters {
        return when (tasks) {
            Active -> TaskFilters.active(page)
            Completed -> TaskFilters.complete()
            else -> TaskFilters.all()
        }
    }

    companion object : MavericksViewModelFactory<TasksViewModel, TasksViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TasksViewState
        ) = TasksViewModel(state, viewModelContext.activity)
    }
}
