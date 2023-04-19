package com.alfresco.content.browse.processes.details

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope

/**
 * Marked as ProcessDetailViewModel
 */
class ProcessDetailViewModel(
    state: ProcessDetailViewState,
    val context: Context,
    val repository: TaskRepository
) : MavericksViewModel<ProcessDetailViewState>(state) {

    init {
        viewModelScope.on<ActionUpdateNameDescription> {
            setState { copy(entry = it.entry as ProcessEntry) }
        }
    }
    /**
     * update the priority in the existing ProcessEntry obj and update the UI.
     */
    fun updatePriority(result: ComponentMetaData) {
        setState {
            requireNotNull(this.entry)
            copy(entry = ProcessEntry.updatePriority(this.entry, result.query?.toInt() ?: 0))
        }
    }

    /**
     * update the formatted date in the existing ProcessEntry obj and update the UI.
     */
    fun updateDate(formattedDate: String?) {
        setState {
            requireNotNull(this.entry)
            copy(entry = ProcessEntry.updateDueDate(this.entry, formattedDate))
        }
    }

    /**
     * It will execute while showing the dialog to update task name and description.
     */
    fun execute(action: Action) = action.execute(context, GlobalScope)

    companion object : MavericksViewModelFactory<ProcessDetailViewModel, ProcessDetailViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDetailViewState
        ) = ProcessDetailViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
