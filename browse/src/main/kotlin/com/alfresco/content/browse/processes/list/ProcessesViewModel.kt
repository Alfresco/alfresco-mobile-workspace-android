package com.alfresco.content.browse.processes.list

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.ProcessFilters
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.processes.ProcessListViewModel
import com.alfresco.content.listview.processes.ProcessListViewState
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.launch

/**
 * Marked as ProcessesViewModel class
 */
class ProcessesViewModel(
    state: ProcessesViewState,
    val context: Context,
    private val repository: TaskRepository
) : ProcessListViewModel<ProcessesViewState>(state) {

    val listProcesses = mapOf(
        ProcessFilters.All.filter to ProcessFilters.All.name,
        ProcessFilters.Active.filter to ProcessFilters.Running.name,
        ProcessFilters.Completed.filter to ProcessFilters.Completed.name
    )

    var filterName: String = ProcessFilters.Active.filter
    var filterValue: String = ""
    var scrollToTop = false

    init {
        filterValue = listProcesses.getValue(filterName)
        fetchInitial()
        viewModelScope.on<UpdateProcessData> {
            if (it.isRefresh) {
                scrollToTop = true
                fetchInitial()
            }
        }
    }

    companion object : MavericksViewModelFactory<ProcessesViewModel, ProcessesViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessesViewState
        ) = ProcessesViewModel(state, viewModelContext.activity, TaskRepository())
    }

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val newPage = state.page.plus(1)
        viewModelScope.launch {
            // Fetch processes data
            repository::getProcesses.asFlow(
                TaskProcessFiltersPayload.updateFilters(state.filterParams, filterValue, newPage)
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(processEntries = emptyList(), request = Fail(it.error))
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

    override fun emptyMessageArgs(state: ProcessListViewState): Triple<Int, Int, Int> {
        return when (state.request) {
            is Fail -> Triple(R.drawable.ic_empty_recent, R.string.workflows_empty_title, R.string.workflows_account_not_configured)
            else -> Triple(R.drawable.ic_empty_recent, R.string.workflows_empty_title, R.string.workflows_empty_message)
        }
    }

    private fun fetchInitial() = withState { state ->
        viewModelScope.launch {
            // Fetch processes data
            repository::getProcesses.asFlow(
                TaskProcessFiltersPayload.updateFilters(state.filterParams, filterValue)
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(processEntries = emptyList(), request = Fail(it.error))
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
     * Filter applied and execute api
     */
    fun applyFilters(name: String?) {
        val selectedKey = listProcesses.keys.find { name == context.getLocalizedName(it) }
        selectedKey?.let {
            filterName = it
            filterValue = listProcesses.getValue(filterName)
        }
        fetchInitial()
    }
}

/**
 * Mark as UpdateProcessData data class
 */
data class UpdateProcessData(val isRefresh: Boolean)
