package com.alfresco.content.browse.processes.sheet

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.TaskRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

internal class ProcessDefinitionsViewModel(
    state: ProcessDefinitionsState,
    val context: Context,
) : MavericksViewModel<ProcessDefinitionsState>(state) {

    init {
        buildModel()
    }

    private fun buildModel() = withState { state ->
        viewModelScope.launch {
            processDefinitions().execute {
                when (it) {
                    is Success -> {
                        ProcessDefinitionsState(
                            entries = state.entries,
                            listProcessDefinitions = it().listRuntimeProcessDefinitions,
                        )
                    }

                    else -> {
                        ProcessDefinitionsState(
                            entries = state.entries,
                            listProcessDefinitions = null,
                        )
                    }
                }
            }
        }
    }

    private fun processDefinitions() = TaskRepository()::processDefinitions.asFlow()

    /**
     * returns the empty data if no workflows avaialble
     */
    fun emptyMessageArgs() = Triple(R.drawable.ic_empty_workflow, R.string.workflows_unavailable_title, R.string.workflow_unavailable_message)

    companion object : MavericksViewModelFactory<ProcessDefinitionsViewModel, ProcessDefinitionsState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDefinitionsState,
        ) =
            // Requires activity context in order to present other fragments
            ProcessDefinitionsViewModel(state, viewModelContext.activity())
    }
}
