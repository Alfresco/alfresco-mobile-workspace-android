package com.alfresco.content.browse.processes.details

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job

/**
 * Marked as ProcessDetailViewModel
 */
class ProcessDetailViewModel(
    state: ProcessDetailViewState,
    val context: Context,
    val repository: TaskRepository
) : MavericksViewModel<ProcessDetailViewState>(state) {

    private var observeUploadsJob: Job? = null

    init {
        viewModelScope.on<ActionUpdateNameDescription> {
            setState { copy(parent = it.entry as ProcessEntry) }
        }
        updateDefaultEntry(state.parent?.defaultEntry)
        observeUploads(state)
    }

    /**
     * update the priority in the existing ProcessEntry obj and update the UI.
     */
    fun updatePriority(result: ComponentMetaData) {
        setState {
            requireNotNull(this.parent)
            copy(parent = ProcessEntry.updatePriority(this.parent, result.query?.toInt() ?: 0))
        }
    }

    private fun updateDefaultEntry(entry: Entry?) = setState { copy(baseEntries = if (entry != null) listOf(entry) else emptyList()) }

    /**
     * update the formatted date in the existing ProcessEntry obj and update the UI.
     */
    fun updateDate(formattedDate: String?, isClearDueDate: Boolean = false) {
        setState {
            requireNotNull(this.parent)
            copy(parent = ProcessEntry.updateDueDate(this.parent, formattedDate, isClearDueDate))
        }
    }

    /**
     * update the assignee in the existing ProcessEntry obj and update the UI.
     */
    fun updateAssignee(result: UserGroupDetails) {
        setState {
            requireNotNull(this.parent)
            copy(parent = ProcessEntry.updateAssignee(this.parent, result))
        }
    }

    /**
     * It will execute while showing the dialog to update task name and description.
     */
    fun execute(action: Action) = action.execute(context, GlobalScope)

    /**
     * returns the current logged in APS user profile data
     */
    fun getAPSUser() = repository.getAPSUser()

    private fun observeUploads(state: ProcessDetailViewState) {
        println("Check Server ID 4 = ${state.parent?.id}")
        if (state.parent?.id == null) return

        val repo = OfflineRepository()

        // On refresh clean completed uploads
        repo.removeCompletedUploads()

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeUploads(state.parent.id, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    updateUploads(it())
                } else {
                    this
                }
            }
    }

    companion object : MavericksViewModelFactory<ProcessDetailViewModel, ProcessDetailViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessDetailViewState
        ) = ProcessDetailViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
