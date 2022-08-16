package com.alfresco.content.browse.tasks.list

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.ComponentViewModel
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.TaskFiltersPayload
import com.alfresco.content.listview.tasks.TaskListViewModel
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.coroutines.asFlow
import java.text.SimpleDateFormat
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
                TaskFiltersPayload.updateFilters(state.filterParams, newPage)
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
                TaskFiltersPayload.updateFilters(state.filterParams)
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

    /**
     * this method makes the payload filter for task api and execute it.
     */
    fun applyFilters(list: List<TaskFilterData>) {
        val taskFiltersPayload = TaskFiltersPayload()
        list.filter { it.isSelected }.forEach {
            when (it.name?.lowercase()) {
                "due date" -> {
                    if (it.selectedQueryMap.containsKey(ComponentViewModel.DUE_BEFORE))
                        taskFiltersPayload.dueBefore = getZoneFormattedDate(it.selectedQueryMap[ComponentViewModel.DUE_BEFORE])

                    if (it.selectedQueryMap.containsKey(ComponentViewModel.DUE_AFTER))
                        taskFiltersPayload.dueAfter = getZoneFormattedDate(it.selectedQueryMap[ComponentViewModel.DUE_AFTER])
                }
                "status" -> {
                    taskFiltersPayload.state = it.selectedQuery
                }
                "task name" -> {
                    taskFiltersPayload.text = it.selectedQuery
                }
            }
        }

        setState {
            copy(filterParams = taskFiltersPayload)
        }

        refresh()
    }

    /**
     * update the isSelected state when user tap on filter chip.
     */
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

    /**
     * update the filter result
     */
    fun updateChipFilterResult(state: TasksViewState, model: TaskFilterData, metaData: ComponentMetaData): MutableList<TaskFilterData> {
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

    /**
     * reset the filter chips
     */
    fun resetChips(state: TasksViewState): List<TaskFilterData> {
        val list = mutableListOf<TaskFilterData>()
        state.listSortDataChips.forEach { obj ->
            list.add(TaskFilterData.reset(obj))
        }
        setState { copy(listSortDataChips = list) }

        return list
    }

    private fun getZoneFormattedDate(dateString: String?): String {

        if (dateString.isNullOrEmpty()) return ""

        val currentFormat = SimpleDateFormat("dd-MMM-yy")
        val zonedFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        return currentFormat.parse(dateString)?.let { zonedFormat.format(it) } ?: ""
    }

    companion object : MavericksViewModelFactory<TasksViewModel, TasksViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TasksViewState
        ) = TasksViewModel(state, viewModelContext.activity, TaskRepository())
    }
}
