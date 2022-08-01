package com.alfresco.content.browse.tasks

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.data.TaskFilters
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.TaskStateData
import com.alfresco.content.listview.tasks.TaskListViewModel
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

/**
 * Marked as TasksViewModel class
 */
class TasksViewModel(
    state: TasksViewState,
    val context: Context,
    private val repository: TaskRepository
) : TaskListViewModel<TasksViewState>(state) {

    init {
        setState { copy(listSortDataChips = repository.getTaskFiltersJSON().filters) }
        fetchInitial()
    }

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val newPage = state.page.plus(1)
        viewModelScope.launch {
            // Fetch tasks data
            repository::getTasks.asFlow(
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
        viewModelScope.launch {
            // Fetch tasks data
            repository::getTasks.asFlow(
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

    private fun getSelectedTasks(taskState: TaskStateData, page: Int, listSort: List<TaskFilterData>): TaskFilters {
        val sortObj = listSort.find { it.isSelected }
        return TaskFilters.filter(
            page = page,
            state = taskState.state,
            sort = sortObj?.selectedValue?.lowercase() ?: ""
        )
    }

    fun updateSelected(state: TasksViewState, data: TaskFilterData, isSelected: Boolean) {
        val list = mutableListOf<TaskFilterData>()
        state.listSortDataChips.forEach { obj ->
            if (obj == data) {
                list.add(TaskFilterData.updateData(obj, isSelected))
            } else {
                list.add(obj)
            }
        }
        setState { copy(listSortDataChips = list) }
    }

    fun updateChipFilterResult(state: TasksViewState, model: TaskFilterData, metaData: FilterMetaData): MutableList<TaskFilterData> {
        val list = mutableListOf<TaskFilterData>()

        state.listSortDataChips.forEach { obj ->
            if (obj == model) {
                list.add(
                    TaskFilterData.withFilterResult(
                        obj,
                        isSelected = metaData.name?.isNotEmpty() == true,
                        selectedName = metaData.name ?: "",
                        selectedQuery = metaData.query ?: "",
                        selectedQueryMap = metaData.queryMap ?: mapOf()
                    )
                )
            } else
                list.add(obj)
        }

        setState { copy(listSortDataChips = list) }

        return list
    }

    fun resetChips(state: TasksViewState): List<TaskFilterData> {
        val list = mutableListOf<TaskFilterData>()
        state.listSortDataChips.forEach { obj ->
            list.add(TaskFilterData.reset(obj))
        }
        setState { copy(listSortDataChips = list) }

        return list
    }

    companion object : MavericksViewModelFactory<TasksViewModel, TasksViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TasksViewState
        ) = TasksViewModel(state, viewModelContext.activity, TaskRepository())
    }
}
