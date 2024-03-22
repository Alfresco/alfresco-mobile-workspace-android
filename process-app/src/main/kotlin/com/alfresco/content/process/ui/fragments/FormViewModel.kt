package com.alfresco.content.process.ui.fragments

import android.content.Context
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.getFormattedDate
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
                        val updatedState = copy(
                            parent = processEntry,
                            formFields = it().fields.flatMap { listData -> listData.fields },
                            processOutcomes = it().outcomes,
                            requestStartForm = Success(it()),
                        )
                        enableDisableActions(updatedState)
                        updatedState
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    fun updateFieldValue(fieldId: String, newValue: Any?, state: FormViewState, hasError: Boolean) {
        val updatedFieldList = state.formFields.map { field ->
            if (field.id == fieldId) {
                var updatedValue = newValue
                when {
                    (updatedValue is String) && updatedValue.isEmpty() -> {
                        updatedValue = null
                    }

                    (updatedValue is Boolean) && !updatedValue -> {
                        updatedValue = null
                    }

                    (updatedValue is UserGroupDetails) && updatedValue.id == 0 -> {
                        updatedValue = null
                    }

                    (updatedValue is OptionsModel) && updatedValue.id.isEmpty() -> {
                        updatedValue = null
                    }
                }
                field.copy(value = updatedValue, hasErrorInValue = hasError)
            } else {
                field
            }
        }

        val updatedState = state.copy(
            formFields = updatedFieldList,
        )

        enableDisableActions(updatedState)
    }

    fun startWorkflow() = withState { state ->
        viewModelScope.launch {
            repository::startWorkflow.asFlow(state.parent, "", convertFieldsToValues(state.formFields)).execute {
                when (it) {
                    is Loading -> copy(requestStartWorkflow = Loading())
                    is Fail -> copy(requestStartWorkflow = Fail(it.error))
                    is Success -> copy(requestStartWorkflow = Success(it()))
                    else -> this
                }
            }
        }
    }

    private fun convertFieldsToValues(fields: List<FieldsData>): Map<String, Any?> {
        val values = mutableMapOf<String, Any?>()

        fields.forEach {
            when (it.type) {
                FieldType.PEOPLE.value(), FieldType.FUNCTIONAL_GROUP.value() -> {
                    when {
                        it.value != null -> {
                            values[it.id] = repository.getUserOrGroup(it.value as? UserGroupDetails)
                        }

                        else -> {
                            values[it.id] = null
                        }
                    }
                }

                FieldType.DATETIME.value(), FieldType.DATE.value() -> {
                    val convertedDate = (it.value as? String)?.getFormattedDate(DATE_FORMAT_4, DATE_FORMAT_5)
                    values[it.id] = convertedDate
                }

                FieldType.RADIO_BUTTONS.value(), FieldType.DROPDOWN.value() -> {
                    values[it.id] = repository.mapStringToOptionValues(it)
                }

                FieldType.UPLOAD.value() -> {
                    values[it.id] = state.listContents.joinToString(separator = ",") { content -> content.id }
                }

                else -> {
                    values[it.id] = it.value
                }
            }
        }

        return values
    }

    private fun enableDisableActions(state: FormViewState) {
        val hasAllRequiredData = hasFieldValidData(state)

        setState { state.copy(enabledOutcomes = hasAllRequiredData) }
    }

    private fun hasFieldValidData(state: FormViewState): Boolean {
        val hasValidDataInRequiredFields = !state.formFields.filter { it.required }.any { (it.value == null || it.hasErrorInValue) }
        val hasValidDataInOtherFields = !state.formFields.filter { !it.required }.any { it.hasErrorInValue }
        println("Test 1 == $hasValidDataInRequiredFields || $hasValidDataInOtherFields")

        return (hasValidDataInRequiredFields && hasValidDataInOtherFields)
    }

    companion object : MavericksViewModelFactory<FormViewModel, FormViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: FormViewState,
        ) = FormViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
