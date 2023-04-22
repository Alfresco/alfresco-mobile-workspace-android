package com.alfresco.content.browse.processes.sheet

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.Entry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

internal class ProcessDefinitionsViewModel(
    state: ProcessDefinitionsState,
    val context: Context
) : MavericksViewModel<ProcessDefinitionsState>(state) {

    init {
        buildModel()
    }

    private fun buildModel() = withState { state ->
        viewModelScope.launch {
            processDefinitions().zip(linkContentToProcess(state.entry)) { requestProcess, requestContent ->
                Pair(requestProcess, requestContent)
            }.execute {
                when (it) {
                    is Success -> {
                        ProcessDefinitionsState(
                            entry = it().second,
                            listProcessDefinitions = it().first.listRuntimeProcessDefinitions
                        )
                    }
                    else -> {
                        ProcessDefinitionsState(
                            entry = state.entry,
                            listProcessDefinitions = null
                        )
                    }
                }
            }
        }
    }

    private fun linkContentToProcess(entry: Entry) = TaskRepository()::linkADWContentToProcess.asFlow(LinkContentPayload.with(entry))

    private fun processDefinitions() = TaskRepository()::processDefinitions.asFlow()

    companion object : MavericksViewModelFactory<ProcessDefinitionsViewModel, ProcessDefinitionsState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDefinitionsState
        ) =
            // Requires activity context in order to present other fragments
            ProcessDefinitionsViewModel(state, viewModelContext.activity())
    }
}
