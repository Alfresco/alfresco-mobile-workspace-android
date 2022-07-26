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
import com.alfresco.content.data.TaskState
import com.alfresco.content.data.TaskState.Active
import com.alfresco.content.data.TaskState.All
import com.alfresco.content.data.TaskState.Completed
import com.alfresco.content.listview.tasks.TaskListViewModel
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

/**
 * Marked as TasksViewModel class
 */
class TasksViewModel(
    state: TasksViewState,
    val context: Context
) : TaskListViewModel<TasksViewState>(state) {

    init {
        setupSortAndFiltersData()
        fetchInitial()
    }

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val newPage = state.page.plus(1)
        viewModelScope.launch {
            // Fetch tasks data
            TaskRepository()::getTasks.asFlow(
                getSelectedTasks(state.displayTask, newPage, state.listSortDataChips)
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

        return when (state.request) {
            is Fail -> Triple(R.drawable.ic_empty_recent, R.string.tasks_empty_title, R.string.account_not_configured)
            else -> Triple(R.drawable.ic_empty_recent, R.string.tasks_empty_title, R.string.tasks_empty_message)
        }
    }

    private fun fetchInitial() = withState { state ->
        println("executing api")
        viewModelScope.launch {
            // Fetch tasks data
            TaskRepository()::getTasks.asFlow(
                getSelectedTasks(state.displayTask, 0, state.listSortDataChips)
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

    private fun setupSortAndFiltersData() {
        val listState = mutableListOf<TaskState>()
        listState.add(All)
        listState.add(Active)
        listState.add(Completed)

        val list = mutableListOf<TaskSortData>()
        list.add(TaskSortData.with(context.getString(R.string.sort_due_date), context.resources.getStringArray(R.array.due_date_values).toList()))
        list.add(TaskSortData.with(context.getString(R.string.sort_priority)))
        list.add(TaskSortData.with(context.getString(R.string.sort_assignment), context.resources.getStringArray(R.array.assignment_values).toList()))
        list.add(TaskSortData.with(context.getString(R.string.sort_created), context.resources.getStringArray(R.array.created_values).toList()))
        setState {
            copy(
                listSortDataChips = list,
                listTaskState = listState
            )
        }
    }

    private fun getSelectedTasks(taskState: TaskState, page: Int, listSort: List<TaskSortData>): TaskFilters {
        val sortObj = listSort.find { it.isSelected }
        return if (sortObj?.isSelected == true && sortObj.title == context.getString(R.string.sort_assignment)) {
            TaskFilters.filter(
                page, taskState.name.lowercase(),
                sort = "",
                assignment = sortObj.selectedValue.lowercase()
            )
        } else {
            TaskFilters.filter(
                page, taskState.name.lowercase(),
                sort = sortObj?.selectedValue?.lowercase() ?: ""
            )
        }
    }

    fun updateState(position: Int) {
        setState {
            copy(displayTask = listTaskState[position])
        }
        refresh()
    }

    fun updateSortSelection(state: TasksViewState, data: TaskSortData) {
        val list = mutableListOf<TaskSortData>()
        state.listSortDataChips.forEach { obj ->
            if (obj == data) {
                list.add(TaskSortData.updateData(obj))
            } else {
                list.add(TaskSortData.reset(obj))
            }
        }
        setState { copy(listSortDataChips = list) }

        refresh()
    }

    companion object : MavericksViewModelFactory<TasksViewModel, TasksViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TasksViewState
        ) = TasksViewModel(state, viewModelContext.activity)
    }
}
