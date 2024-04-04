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
import com.alfresco.content.data.AttachFolderSearchData
import com.alfresco.content.data.DefaultOutcomesID
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.data.payloads.convertModelToMapValues
import com.alfresco.content.getFormattedDate
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class FormViewModel(
    val state: FormViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<FormViewState>(state) {

    private var observeUploadsJob: Job? = null
    var observerID: String = ""
    var folderFieldId = ""
    private var entryListener: EntryListener? = null

    init {
        observerID = UUID.randomUUID().toString()

        if (state.parent.processInstanceId != null) {
            getTaskForms(state.parent)
        } else {
            singleProcessDefinition(state.parent.id)
        }

        viewModelScope.on<AttachFolderSearchData> {
            it.entry?.let { entry ->
                entryListener?.onAttachFolder(entry)
            }
        }
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
                    is Loading -> copy(requestForm = Loading())
                    is Fail -> {
                        it.error.printStackTrace()
                        copy(requestForm = Fail(it.error))
                    }

                    is Success -> {
                        val fields = it().fields.flatMap { listData -> listData.fields }

                        val updatedState = copy(
                            parent = processEntry,
                            formFields = fields,
                            processOutcomes = it().outcomes,
                            requestForm = Success(it()),
                        )

                        val hasAllRequiredData = hasFieldValidData(fields)
                        updateStateData(hasAllRequiredData, fields)

                        updatedState
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun getTaskForms(processEntry: ProcessEntry) = withState { state ->
        viewModelScope.launch {
            repository::getTaskForm.asFlow(processEntry.taskEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestForm = Loading())
                    is Fail -> {
                        it.error.printStackTrace()
                        copy(requestForm = Fail(it.error))
                    }

                    is Success -> {
                        val fields = it().fields.flatMap { listData -> listData.fields }

                        val updatedState = copy(
                            parent = processEntry,
                            formFields = fields,
                            processOutcomes = it().outcomes,
                            requestForm = Success(it()),
                        )

                        val hasAllRequiredData = hasFieldValidData(fields)
                        updateStateData(hasAllRequiredData, fields)

                        updatedState
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    fun updateFieldValue(fieldId: String, newValue: Any?, state: FormViewState, errorData: Pair<Boolean, String>) {
        val updatedFieldList: MutableList<FieldsData> = mutableListOf()

        state.formFields.forEach { field ->
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
                updatedFieldList.add(FieldsData.withUpdateField(field, updatedValue, errorData))
            } else {
                updatedFieldList.add(field)
            }
        }

        val hasAllRequiredData = hasFieldValidData(updatedFieldList)

        updateStateData(hasAllRequiredData, updatedFieldList)
    }

    fun performOutcomes(optionsModel: OptionsModel) {
        when (optionsModel.id) {
            DefaultOutcomesID.DEFAULT_START_WORKFLOW.value() -> startWorkflow()
            DefaultOutcomesID.DEFAULT_COMPLETE.value() -> completeTask()
            DefaultOutcomesID.DEFAULT_SAVE.value() -> saveForm()
            else -> actionOutcome(optionsModel.outcome)
        }
    }

    private fun completeTask() = withState { state ->
        viewModelScope.launch {
            repository::completeTask.asFlow(state.parent.taskEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestOutcomes = Loading())
                    is Fail -> {
                        copy(requestOutcomes = Fail(it.error))
                    }

                    is Success -> {
                        copy(requestOutcomes = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * execute the save-form api
     */
    private fun saveForm() = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            repository::saveForm.asFlow(
                state.parent.taskEntry.id,
                convertFieldsToValues(
                    state.formFields
                        .filter { it.type !in listOf(FieldType.READONLY.value(), FieldType.READONLY_TEXT.value()) },
                ),
            ).execute {
                when (it) {
                    is Loading -> copy(requestSaveForm = Loading())
                    is Fail -> {
                        copy(requestSaveForm = Fail(it.error))
                    }

                    is Success -> {
                        copy(requestSaveForm = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun startWorkflow() = withState { state ->
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

    /**
     * execute the outcome api
     */
    private fun actionOutcome(outcome: String) = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            repository::actionOutcomes.asFlow(outcome, state.parent.taskEntry).execute {
                when (it) {
                    is Loading -> copy(requestOutcomes = Loading())
                    is Fail -> {
                        copy(requestOutcomes = Fail(it.error))
                    }

                    is Success -> {
                        copy(requestOutcomes = Success(it()))
                    }

                    else -> {
                        this
                    }
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
                            values[it.id] = convertModelToMapValues(it.value as? UserGroupDetails)
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
                    values[it.id] = convertModelToMapValues(it)
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

    private fun updateStateData(enabledOutcomes: Boolean, fields: List<FieldsData>) {
        setState { copy(enabledOutcomes = enabledOutcomes, formFields = fields) }
    }

    private fun hasFieldValidData(fields: List<FieldsData>): Boolean {
        val hasValidDataInRequiredFields = !fields.filter { it.required }.any { (it.value == null || it.errorData.first) }
        val hasValidDataInOtherFields = !fields.filter { !it.required }.any { it.errorData.first }
        return (hasValidDataInRequiredFields && hasValidDataInOtherFields)
    }

    fun setListener(listener: EntryListener) {
        entryListener = listener
    }

    companion object : MavericksViewModelFactory<FormViewModel, FormViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: FormViewState,
        ) = FormViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
