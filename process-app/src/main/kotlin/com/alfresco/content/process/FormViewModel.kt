package com.alfresco.content.process

import android.content.Context
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.ResponseListProcessDefinition
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

data class FormViewState(
    val parent: ProcessEntry = ProcessEntry(),
    val requestStartForm: Async<ResponseListForm> = Uninitialized,
    val requestProcessDefinition: Async<ResponseListProcessDefinition> = Uninitialized,
    val formFields: List<FieldsData> = emptyList(),
    val processOutcomes: List<OptionsModel> = emptyList(),
    val enabledOutcomes: Boolean = false,
    val requestStartWorkflow: Async<ProcessEntry> = Uninitialized,
) : MavericksState {
    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * update the single process definition entry
     */
    fun updateSingleProcessDefinition(response: ResponseListProcessDefinition): FormViewState {
        if (parent == null) {
            return this
        }
        val processEntry = ProcessEntry.with(response.listProcessDefinitions.first(), parent)
        return copy(parent = processEntry)
    }
}

class FormViewModel(
    val state: FormViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<FormViewState>(state) {

    init {
        singleProcessDefinition(state.parent.id)
    }

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
