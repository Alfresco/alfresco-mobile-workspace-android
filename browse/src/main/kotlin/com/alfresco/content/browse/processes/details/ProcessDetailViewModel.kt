package com.alfresco.content.browse.processes.details

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
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
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Marked as ProcessDetailViewModel
 */
class ProcessDetailViewModel(
    state: ProcessDetailViewState,
    val context: Context,
    private val repository: TaskRepository
) : MavericksViewModel<ProcessDetailViewState>(state) {

    private var observeUploadsJob: Job? = null

    init {
        viewModelScope.on<ActionUpdateNameDescription> {
            setState { copy(parent = it.entry as ProcessEntry) }
        }
        state.parent?.defaultEntry?.let { entry ->
            linkContentToProcess(entry)
        }
        state.parent?.let { processEntry ->
            singleProcessDefinition(processEntry.id)
        }
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
        if (state.parent?.id == null) return

        val repo = OfflineRepository()

        // On refresh clean completed uploads
        repo.removeCompletedUploads()

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeUploads(state.parent.id, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    println("Check 2 == ${it()}")
                    updateUploads(it())
                } else {
                    this
                }
            }
    }

    private fun getStartForm(processDefinitionId: String) = withState { state ->
        viewModelScope.launch {
            repository::startForm.asFlow(processDefinitionId).execute {
                when (it) {
                    is Loading -> copy(requestStartForm = Loading())
                    is Fail -> copy(requestStartForm = Fail(it.error))
                    is Success -> {
                        updateFormFields(it()).copy(requestStartForm = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * delete content locally
     */
    fun deleteAttachment(contentId: String) = stateFlow.execute {
        deleteUploads(contentId)
    }

    private fun linkContentToProcess(entry: Entry) =
        viewModelScope.launch {
            repository::linkADWContentToProcess.asFlow(LinkContentPayload.with(entry)).execute {
                when (it) {
                    is Loading -> copy(requestContent = Loading())
                    is Fail -> copy(requestContent = Fail(it.error))
                    is Success -> {
                        updateContent(it()).copy(requestContent = Success(it()))
                    }

                    else -> this
                }
            }
        }

    private fun singleProcessDefinition(appDefinitionId: String) = withState { state ->
        viewModelScope.launch {
            repository::singleProcessDefinition.asFlow(appDefinitionId).execute {
                when (it) {
                    is Loading -> copy(requestProcessDefinition = Loading())
                    is Fail -> copy(requestProcessDefinition = Fail(it.error))
                    is Success -> {
                        updateSingleProcessDefinition(it())
                        getStartForm(it().listProcessDefinitions.first().id ?: "")
                        copy(requestProcessDefinition = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
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
