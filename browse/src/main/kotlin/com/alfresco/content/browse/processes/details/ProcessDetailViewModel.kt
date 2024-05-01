package com.alfresco.content.browse.processes.details

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.common.EntryListener
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.content.data.payloads.TaskProcessFiltersPayload
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * Marked as ProcessDetailViewModel
 */
class ProcessDetailViewModel(
    state: ProcessDetailViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<ProcessDetailViewState>(state) {

    private var observeUploadsJob: Job? = null
    var entryListener: EntryListener? = null
    var observerID: String = ""
    private var isExecuted = false

    init {
        observerID = UUID.randomUUID().toString()
        viewModelScope.on<ActionOpenWith> {
            if (!it.entry.path.isNullOrEmpty()) {
                entryListener?.onEntryCreated(it.entry)
            }
        }
        viewModelScope.on<ActionUpdateNameDescription> {
            setState { copy(parent = it.entry as ProcessEntry) }
        }

        fetchUserProfile()
        fetchAccountInfo()
        if (state.parent?.processDefinitionId.isNullOrEmpty()) {
            state.parent?.let { processEntry ->
                singleProcessDefinition(processEntry.id)
            }
        } else {
            fetchTasks()
        }
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
    fun updateDate(formattedDate: String?) {
        setState {
            requireNotNull(this.parent)
            copy(parent = ProcessEntry.updateDueDate(this.parent, formattedDate))
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
        requireNotNull(state.parent)

        val repo = OfflineRepository()

        // On refresh clean completed uploads
        repo.removeCompletedUploads()

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeUploads(observerID, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    updateUploads(it())
                } else {
                    this
                }
            }
    }

    private fun getStartForm(processEntry: ProcessEntry) {
        requireNotNull(processEntry.id)
        viewModelScope.launch {
            repository::startForm.asFlow(processEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestStartForm = Loading())
                    is Fail -> copy(requestStartForm = Fail(it.error))
                    is Success -> {
                        updateFormFields(it(), processEntry).copy(requestStartForm = Success(it()))
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

    private fun linkContentToProcess(entry: Entry, sourceName: String) =
        viewModelScope.launch {
            repository::linkADWContentToProcess.asFlow(LinkContentPayload.with(entry, sourceName)).execute {
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
                        val updatedState = updateSingleProcessDefinition(it())
                        observeUploads(updatedState)
                        updatedState.parent?.let { processEntry ->
                            getStartForm(processEntry)
                        }
                        copy(requestProcessDefinition = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * This method will execute the start flow api with required data
     */
    fun startWorkflow() = withState { state ->
        val items = state.listContents.joinToString(separator = ",") { it.id }
        viewModelScope.launch {
            repository::startWorkflow.asFlow(state.parent, items, mapOf()).execute {
                when (it) {
                    is Loading -> copy(requestStartWorkflow = Loading())
                    is Fail -> copy(requestStartWorkflow = Fail(it.error))
                    is Success -> copy(requestStartWorkflow = Success(it()))
                    else -> this
                }
            }
        }
    }

    /**
     * adding listener to update the View after downloading the content
     */
    fun setListener(listener: EntryListener) {
        this.entryListener = listener
    }

    private fun fetchUserProfile() {
        if (repository.isAcsAndApsSameUser()) {
            return
        }
        viewModelScope.launch {
            // Fetch APS user profile data
            repository::getProcessUserProfile.execute {
                when (it) {
                    is Loading -> copy(requestProfile = Loading())
                    is Fail -> copy(requestProfile = Fail(it.error))
                    is Success -> {
                        val response = it()
                        repository.saveProcessUserDetails(response)
                        copy(requestProfile = Success(response))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun fetchAccountInfo() = withState { state ->
        viewModelScope.launch {
            repository::getAccountInfo.execute {
                when (it) {
                    is Loading -> copy(requestAccountInfo = Loading())
                    is Fail -> copy(requestAccountInfo = Fail(it.error))
                    is Success -> {
                        val response = it()

                        repository.saveSourceName(response.listAccounts.first())
                        val sourceName = response.listAccounts.first().sourceName
                        if (!isExecuted) {
                            isExecuted = true
                            state.parent?.defaultEntries?.map { entry ->
                                linkContentToProcess(entry, sourceName)
                            }
                        }
                        copy(requestAccountInfo = Success(response))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun fetchTasks() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks data
            repository::getTasks.asFlow(
                TaskProcessFiltersPayload.defaultTasksOfProcess(state.parent?.id),
            ).execute {
                when (it) {
                    is Loading -> copy(requestTasks = Loading())
                    is Fail -> copy(requestTasks = Fail(it.error))
                    is Success -> {
                        val response = it()
                        updateTasks(response).copy(requestTasks = Success(response))
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
            state: ProcessDetailViewState,
        ) = ProcessDetailViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
