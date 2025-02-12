package com.alfresco.content.process.ui.fragments

import android.content.Context
import androidx.compose.animation.scaleOut
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_2_1
import com.alfresco.content.DATE_FORMAT_4_1
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.APIEvent
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.AttachFilesData
import com.alfresco.content.data.AttachFolderSearchData
import com.alfresco.content.data.DefaultOutcomesID
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseAccountInfo
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.data.payloads.LinkContentPayload
import com.alfresco.content.data.payloads.convertModelToMapValues
import com.alfresco.content.getFormattedDate
import com.alfresco.coroutines.asFlow
import com.alfresco.events.on
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FormViewModel(
    val state: FormViewState,
    val context: Context,
    private val repository: TaskRepository,
    offlineRepository: OfflineRepository?,
) : MavericksViewModel<FormViewState>(state) {

    private var observeUploadsJob: Job? = null
    var selectedField: FieldsData? = null
    private var entryListener: EntryListener? = null
    var optionsModel: OptionsModel? = null
    var onLinkContentToProcess: ((Pair<Entry, FieldsData?>) -> Unit)? = null
    private var successLinkContent = false

    init {

        offlineRepository?.removeCompletedUploadsProcess()


        if (state.parent.processInstanceId != null) {
            getTaskDetails()
        } else {
            singleProcessDefinition(state.parent.id)
        }

        viewModelScope.on<AttachFolderSearchData> {
            it.entry?.let { entry ->
                entryListener?.onAttachFolder(entry)
            }
        }

        viewModelScope.on<AttachFilesData> {
            it.field?.let { field ->
                entryListener?.onAttachFiles(field)
            }
        }
    }

    fun observeUploads(state: FormViewState) {
        val parentId = state.parent.id.ifEmpty { state.parent.processDefinitionId } ?: ""

        val repo = OfflineRepository()
        OfflineRepository().removeCompletedUploadsProcess(parentId)
        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeProcessUploads(parentId, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    withState { newState ->
                        val listFields = newState.formFields.filter { fieldsData -> fieldsData.type == FieldType.UPLOAD.value() }
                        listFields.forEach { field ->
                            val listContents = it().filter { content -> content.observerID == field.id }
                            val isError = field.required && listContents.isEmpty() && listContents.all { content -> !content.isUpload }
                            updateFieldValue(field.id, listContents, Pair(isError, ""))
                        }
                    }

                    this
                } else {
                    this
                }
            }
    }

    /**
     * returns the current logged in APS user profile data
     */
    fun getAPSUser() = repository.getAPSUser()

    private fun singleProcessDefinition(appDefinitionId: String) = withState { state ->
        viewModelScope.launch {
            repository::singleProcessDefinition.asFlow(appDefinitionId).execute {
                when (it) {
                    is Loading -> copy(requestProcessDefinition = Loading())
                    is Fail -> copy(requestProcessDefinition = Fail(it.error))
                    is Success -> {
                        val updatedState = updateSingleProcessDefinition(it())
                        getStartForm(updatedState.parent)
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

                        updatedState.copy(requestForm = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    fun fetchUserProfile() {
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

    fun fetchAccountInfo() = withState { state ->
        viewModelScope.launch {
            repository::getAccountInfo.execute {
                when (it) {
                    is Loading -> copy(requestAccountInfo = Loading())
                    is Fail -> copy(requestAccountInfo = Fail(it.error))
                    is Success -> {
                        val response = it()
                        repository.saveSourceName(response.listAccounts.first())
                        updateAccountInfo(response).copy(requestAccountInfo = Success(response))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun getTaskDetails() = withState { state ->
        viewModelScope.launch {
            // Fetch tasks detail data
            repository::getTaskDetails.asFlow(
                state.parent.taskEntry.id,
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        val updateState = update(it())
                        getTaskForms(updateState.parent)
                        updateState.copy(request = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    internal fun isAssigneeAndLoggedInSame(assignee: UserGroupDetails?) = getAPSUser().id == assignee?.id

    internal fun isStartedByAndLoggedInSame(initiatorId: String?) = getAPSUser().id.toString() == initiatorId

    fun linkContentToProcess(state: FormViewState, entry: Entry, sourceName: String, field: FieldsData?) =
        viewModelScope.launch {
            repository::linkADWContentToProcess
                .asFlow(LinkContentPayload.with(entry, sourceName))
                .execute {
                    when (it) {
                        is Loading -> copy(requestContent = Loading())
                        is Fail -> copy(requestContent = Fail(it.error))
                        is Success -> {
                            if (!successLinkContent) {
                                successLinkContent = true
                                val updateEntry = Entry.with(
                                    data = entry,
                                    parentId = state.parent.id,
                                    observerID = field?.id ?: "",
                                )

                                OfflineRepository().update(updateEntry)

                                onLinkContentToProcess?.invoke(Pair(updateEntry, field))
                            }

                            copy(requestContent = Success(it()))
                        }

                        else -> this
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

    fun updateFieldValue(fieldId: String, newValue: Any?, errorData: Pair<Boolean, String>) = withState { state ->
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

                    (updatedValue is List<*>) && updatedValue.isEmpty() -> {
                        updatedValue = null
                    }
                }
                updatedFieldList.add(FieldsData.withUpdateField(field, updatedValue, errorData))
            } else {
                updatedFieldList.add(field)
            }
        }

        val hasAllRequiredData = hasFieldValidData(updatedFieldList)

        println("data 11")
        updateStateData(hasAllRequiredData, updatedFieldList)
    }

    fun performOutcomes(optionsModel: OptionsModel) {
        when (optionsModel.id) {
            DefaultOutcomesID.DEFAULT_START_WORKFLOW.value() -> startWorkflow()
            DefaultOutcomesID.DEFAULT_COMPLETE.value() -> completeTask()
            DefaultOutcomesID.DEFAULT_SAVE.value() -> saveForm()
            DefaultOutcomesID.DEFAULT_CLAIM.value() -> claimTask()
            DefaultOutcomesID.DEFAULT_RELEASE.value() -> releaseTask()
            else -> actionOutcome(optionsModel.outcome)
        }
    }

    /**
     * execute API to claim the task
     */
    internal fun claimTask() = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            repository::claimTask.asFlow(state.parent.taskEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestClaimRelease = Loading())
                    is Fail -> {
                        copy(requestClaimRelease = Fail(it.error))
                    }

                    is Success -> {
                        copy(requestClaimRelease = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    /**
     * execute API to release the task
     */
    internal fun releaseTask() = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            repository::releaseTask.asFlow(state.parent.taskEntry.id).execute {
                when (it) {
                    is Loading -> copy(requestClaimRelease = Loading())
                    is Fail -> {
                        copy(requestClaimRelease = Fail(it.error))
                    }

                    is Success -> {
                        copy(requestClaimRelease = Success(it()))
                    }

                    else -> {
                        this
                    }
                }
            }
        }
    }

    internal fun completeTask() = withState { state ->
        viewModelScope.launch {
            repository::actionCompleteOutcome.asFlow(state.parent.taskEntry.id, convertFieldsToValues(state.formFields)).execute {
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
    internal fun saveForm() = withState { state ->
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

    internal fun startWorkflow() = withState { state ->
        viewModelScope.launch {
            repository::startWorkflow.asFlow(state.parent, "", convertFieldsToValues(state.formFields)).execute {
                when (it) {
                    is Loading -> copy(requestStartWorkflow = Loading())
                    is Fail -> {
                        AnalyticsManager().apiTracker(APIEvent.StartWorkflow, false)
                        copy(requestStartWorkflow = Fail(it.error))
                    }

                    is Success -> {
                        AnalyticsManager().apiTracker(APIEvent.StartWorkflow, true)
                        copy(requestStartWorkflow = Success(it()))
                    }

                    else -> this
                }
            }
        }
    }

    /**
     * execute the outcome api
     */
    internal fun actionOutcome(outcome: String) = withState { state ->
        requireNotNull(state.parent)
        viewModelScope.launch {
            repository::actionOutcomes.asFlow(
                outcome,
                state.parent.taskEntry,
                convertFieldsToValues(
                    state.formFields
                        .filter { it.type !in listOf(FieldType.READONLY.value(), FieldType.READONLY_TEXT.value()) },
                ),
            ).execute {
                when (it) {
                    is Loading -> copy(requestOutcomes = Loading())
                    is Fail -> {
                        AnalyticsManager().apiTracker(APIEvent.Outcomes, false, outcome = outcome)
                        copy(requestOutcomes = Fail(it.error))
                    }

                    is Success -> {
                        AnalyticsManager().apiTracker(APIEvent.Outcomes, true, outcome = outcome)
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

        fields.forEach { field ->
            when (field.type) {
                FieldType.PEOPLE.value(), FieldType.FUNCTIONAL_GROUP.value() -> {
                    when {
                        field.value != null -> {
                            values[field.id] = convertModelToMapValues(field.getUserGroupDetails(getAPSUser()))
                        }

                        else -> {
                            values[field.id] = null
                        }
                    }
                }

                FieldType.DATETIME.value() -> {
                    val convertedDate = (field.value as? String)?.getFormattedDate(DATE_FORMAT_4_1, DATE_FORMAT_5)
                    values[field.id] = convertedDate
                }

                FieldType.DATE.value() -> {
                    val date = field.getDate(DATE_FORMAT_1, DATE_FORMAT_2_1)
                    val convertedDate = date.first.takeIf { it.isNotEmpty() }?.getFormattedDate(date.second, DATE_FORMAT_5) ?: ""
                    values[field.id] = convertedDate
                }

                FieldType.RADIO_BUTTONS.value(), FieldType.DROPDOWN.value() -> {
                    values[field.id] = convertModelToMapValues(field)
                }

                FieldType.UPLOAD.value() -> {
                    val listContents = (field.value as? List<*>)?.mapNotNull { it as? Entry } ?: emptyList()
                    values[field.id] = listContents.joinToString(separator = ",") { content -> content.id }
                }

                FieldType.SELECT_FOLDER.value() -> {
                    val selectedFolder = (field.value as? Entry)?.id ?: ""
                    values[field.id] = selectedFolder
                }

                else -> {
                    values[field.id] = field.value
                }
            }
        }

        return values
    }

    internal fun updateStateData(enabledOutcomes: Boolean, fields: List<FieldsData>) {
        setState { copy(enabledOutcomes = enabledOutcomes, formFields = fields) }
    }

    internal fun hasFieldValidData(fields: List<FieldsData>): Boolean {
        val hasValidDataInRequiredFields = !fields.filter { it.required }.any { (it.value == null || it.errorData.first) }
        val hasValidDataInDropDownRequiredFields = fields.filter { it.required && it.options.isNotEmpty() }.let { list ->
            list.isEmpty() || list.any { field -> field.options.any { option -> option.name == field.value && option.id != "empty" } }
        }
        val hasValidDataInOtherFields = !fields.filter { !it.required }.any { it.errorData.first }

//        println("hasValidDataInOtherFields ==> $hasValidDataInOtherFields")
//        println("hasValidDataInRequiredFields ==> $hasValidDataInRequiredFields")
//        println("hasValidDataInDropDownRequiredFields ==> $hasValidDataInDropDownRequiredFields")

        return (hasValidDataInRequiredFields && hasValidDataInOtherFields && hasValidDataInDropDownRequiredFields)
    }

    fun setListener(listener: EntryListener) {
        entryListener = listener
    }

    fun getContents(state: FormViewState, fieldId: String) = OfflineRepository().fetchProcessEntries(parentId = state.parent.id.ifEmpty { state.parent.processDefinitionId } ?: "", observerId = fieldId)

    fun resetRequestState(request: Async<*>) {
        when (request.invoke()) {
            is ResponseListForm -> {
                setState { copy(requestForm = Uninitialized) }
            }

            is ResponseAccountInfo -> {
                setState { copy(requestAccountInfo = Uninitialized) }
            }

            is Entry -> {
                setState { copy(requestContent = Uninitialized) }
            }
        }
    }

    companion object : MavericksViewModelFactory<FormViewModel, FormViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: FormViewState,
        ) = FormViewModel(state, viewModelContext.activity(), TaskRepository(), OfflineRepository())
    }
}
