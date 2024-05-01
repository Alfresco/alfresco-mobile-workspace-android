package com.alfresco.content.browse.tasks.list

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.browse.R
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.ComponentViewModel
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.tasks.TaskListViewModel
import com.alfresco.content.listview.tasks.TaskListViewState
import com.alfresco.content.process.ui.models.UpdateTasksData
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Marked as TasksViewModel class
 */
class TasksViewModel(
    state: TasksViewState,
    val context: Context,
    private val repository: TaskRepository,
) : TaskListViewModel<TasksViewState>(state) {

    var scrollToTop = false
    val isWorkflowTask = state.processEntry != null

    init {
        if (!isWorkflowTask) {
            setState { copy(listSortDataChips = repository.getTaskFiltersJSON().filters) }
        }
        fetchUserProfile()
        fetchInitial()
        viewModelScope.on<UpdateTasksData> {
            if (it.isRefresh) {
                scrollToTop = true
                fetchInitial()
            }
        }
    }

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val newPage = state.page.plus(1)
        viewModelScope.launch {
            // Fetch tasks data
            repository::getTasks.asFlow(
                TaskProcessFiltersPayload.updateFilters(state.filterParams, newPage),
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(taskEntries = emptyList(), request = Fail(it.error))
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
                if (!isWorkflowTask) {
                    TaskProcessFiltersPayload.updateFilters(state.filterParams)
                } else TaskProcessFiltersPayload.defaultTasksOfProcess(state.processEntry?.id),
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(taskEntries = emptyList(), request = Fail(it.error))
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

    private fun fetchUserProfile() {
        if (repository.isAcsAndApsSameUser()) return
        viewModelScope.launch {
            // Fetch APS user profile data
            repository::getProcessUserProfile.execute {
                when (it) {
                    is Loading -> copy(requestProfile = Loading())
                    is Fail -> copy(requestProfile = Fail(it.error))
                    is Success -> {
                        repository.saveProcessUserDetails(it())
                        copy(requestProfile = Success(it()))
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
        var taskFiltersPayload = TaskProcessFiltersPayload()
        list.filter { it.isSelected }.forEach {
            when (context.getLocalizedName(it.name?.lowercase() ?: "")) {
                context.getString(R.string.filter_task_due_date) -> {
                    if (it.selectedQueryMap.containsKey(ComponentViewModel.DUE_BEFORE)) {
                        taskFiltersPayload.dueBefore = getZoneFormattedDate(it.selectedQueryMap[ComponentViewModel.DUE_BEFORE])
                    }

                    if (it.selectedQueryMap.containsKey(ComponentViewModel.DUE_AFTER)) {
                        taskFiltersPayload.dueAfter = getZoneFormattedDate(it.selectedQueryMap[ComponentViewModel.DUE_AFTER])
                    }
                }

                context.getString(R.string.filter_task_status) -> {
                    taskFiltersPayload = TaskProcessFiltersPayload.updateTaskFilters(it.selectedQuery)
                }

                context.getString(R.string.filter_task_name) -> {
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
                        selectedQueryMap = metaData.queryMap ?: mapOf(),
                    ),
                )
            } else list.add(obj)
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

        val currentFormat = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)
        val zonedFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        return currentFormat.parse(dateString)?.let { zonedFormat.format(it) } ?: ""
    }

    /**
     * It will execute while showing the dialog to create task.
     */
    fun execute(requireContext: Context, action: Action) = action.execute(requireContext, GlobalScope)

    companion object : MavericksViewModelFactory<TasksViewModel, TasksViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TasksViewState,
        ) = TasksViewModel(state, viewModelContext.activity, TaskRepository())
    }
}
