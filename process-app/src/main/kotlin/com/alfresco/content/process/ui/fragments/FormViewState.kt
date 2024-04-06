package com.alfresco.content.process.ui.fragments

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.ResponseListProcessDefinition
import com.alfresco.content.data.payloads.FieldsData
import retrofit2.Response

data class FormViewState(
    val parent: ProcessEntry = ProcessEntry(),
    val requestForm: Async<ResponseListForm> = Uninitialized,
    val requestProcessDefinition: Async<ResponseListProcessDefinition> = Uninitialized,
    val formFields: List<FieldsData> = emptyList(),
    val processOutcomes: List<OptionsModel> = emptyList(),
    val enabledOutcomes: Boolean = false,
    val requestStartWorkflow: Async<ProcessEntry> = Uninitialized,
    val requestOutcomes: Async<Response<Unit>> = Uninitialized,
    val requestSaveForm: Async<Response<Unit>> = Uninitialized,
) : MavericksState {
    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * update the single process definition entry
     */
    fun updateSingleProcessDefinition(response: ResponseListProcessDefinition): FormViewState {
        val processEntry = ProcessEntry.with(response.listProcessDefinitions.first(), parent)
        return copy(parent = processEntry)
    }
}
