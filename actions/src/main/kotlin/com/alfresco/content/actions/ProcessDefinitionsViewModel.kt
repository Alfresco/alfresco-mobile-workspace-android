package com.alfresco.content.actions

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.Entry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.coroutines.asFlow
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
            linkContentToProcess(state.entry).execute {
                when (it) {
                    is Success -> {
                        copy(linkContent = it)
                    }
                    is Fail -> {
                        copy(linkContent = it)
                    }
                    else -> {
                        copy(linkContent = it)
                    }
                }
            }
        }
    }

    private fun linkContentToProcess(entry: Entry) = TaskRepository()::linkADWContentToProcess.asFlow(LinkContentPayload.with(entry))

    companion object : MavericksViewModelFactory<ProcessDefinitionsViewModel, ProcessDefinitionsState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDefinitionsState
        ) =
            // Requires activity context in order to present other fragments
            ProcessDefinitionsViewModel(state, viewModelContext.activity())
    }
}
