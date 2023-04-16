package com.alfresco.content.browse.processes.details

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.TaskRepository

/**
 * Marked as ProcessDetailViewModel
 */
class ProcessDetailViewModel(
    state: ProcessDetailViewState,
    val context: Context,
    val repository: TaskRepository
) : MavericksViewModel<ProcessDetailViewState>(state) {

    companion object : MavericksViewModelFactory<ProcessDetailViewModel, ProcessDetailViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDetailViewState
        ) = ProcessDetailViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
