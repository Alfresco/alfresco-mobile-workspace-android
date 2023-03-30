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
import com.alfresco.content.listview.processes.ProcessListViewModel
import com.alfresco.content.listview.processes.ProcessListViewState
import com.alfresco.coroutines.asFlow
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
        ProcessFilters.All.name to ProcessFilters.All.name,
        ProcessFilters.Active.name to ProcessFilters.Running.name,
        ProcessFilters.Completed.name to ProcessFilters.Completed.name
    )

    var filterName: String = ProcessFilters.All.name
    var filterValue: String = ""

    init {
        filterValue = listProcesses.getValue(filterName)
        fetchInitial()
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
            // Fetch tasks data
            repository::getTasks.asFlow(
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
            is Fail -> Triple(R.drawable.ic_empty_recent, R.string.workflows_empty_title, R.string.account_not_configured)
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

    fun applyFilters(key: String?) {
        key?.let {
            filterName = it
            filterValue = listProcesses.getValue(filterName)
        }
        fetchInitial()
    }
}
