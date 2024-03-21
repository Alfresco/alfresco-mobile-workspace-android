package com.alfresco.content.process.ui.fragments

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class FormViewModel(
    val state: FormViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<FormViewState>(state) {

    private var observeUploadsJob: Job? = null
    var entryListener: EntryListener? = null
    var observerID: String = ""
    private var isExecuted = false

    init {
        observerID = UUID.randomUUID().toString()
        singleProcessDefinition(state.parent.id)
    }

    /**
     * returns the current logged in APS user profile data
     */
    fun getAPSUser() = repository.getAPSUser()

    /**
     * delete content locally
     */
    fun deleteAttachment(contentId: String) = stateFlow.execute {
        deleteUploads(contentId)
    }

    private fun observeUploads(state: FormViewState) {
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

    private fun singleProcessDefinition(appDefinitionId: String) = withState { state ->
        viewModelScope.launch {
            repository::singleProcessDefinition.asFlow(appDefinitionId).execute {
                when (it) {
                    is Loading -> copy(requestProcessDefinition = Loading())
                    is Fail -> copy(requestProcessDefinition = Fail(it.error))
                    is Success -> {
                        val updatedState = updateSingleProcessDefinition(it())
                        observeUploads(updatedState)
                        updatedState.parent.let { processEntry ->
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

    private fun getStartForm(processEntry: ProcessEntry) {
        requireNotNull(processEntry.id)
        viewModelScope.launch {
            repository::startForm.asFlow(processEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestStartForm = Loading())
                    is Fail -> {
                        it.error.printStackTrace()
                        copy(requestStartForm = Fail(it.error))
                    }

                    is Success -> {
                        copy(
                            parent = processEntry,
                            formFields = it().fields.flatMap { listData -> listData.fields },
                            processOutcomes = it().outcomes,
                            requestStartForm = Success(it()),
                        )
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    fun updateFieldValue(fieldId: String, newValue: Any?, state: FormViewState) {
        val updatedState = state.copy(
            formFields = state.formFields.map { field ->
                if (field.id == fieldId) {
                    var updateValue = newValue
                    when {
                        (updateValue is String) && updateValue.isEmpty() -> {
                            updateValue = null
                        }

                        (updateValue is Boolean) && !updateValue -> {
                            updateValue = null
                        }

                        (updateValue is UserGroupDetails) && updateValue.id == 0 -> {
                            updateValue = null
                        }
                    }
                    field.copy(value = updateValue)
                } else {
                    field
                }
            },
        )

        val hasAllRequiredData = hasFieldRequiredData(updatedState)

        setState { updatedState.copy(enabledOutcomes = hasAllRequiredData) }
    }

    fun startWorkflow() = withState { state ->
        viewModelScope.launch {
            repository::startWorkflow.asFlow(state.parent, "", state.formFields).execute {
                when (it) {
                    is Loading -> copy(requestStartWorkflow = Loading())
                    is Fail -> copy(requestStartWorkflow = Fail(it.error))
                    is Success -> copy(requestStartWorkflow = Success(it()))
                    else -> this
                }
            }
        }
    }

    private fun hasFieldRequiredData(state: FormViewState): Boolean {
        return !state.formFields.filter { it.required }.any { it.value == null }
    }

    companion object : MavericksViewModelFactory<FormViewModel, FormViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: FormViewState,
        ) = FormViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
