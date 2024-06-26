package com.alfresco.content.process.ui.fragments

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.AccountInfoData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseAccountInfo
import com.alfresco.content.data.ResponseFormVariables
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.ResponseListProcessDefinition
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.process.models.ProfileData
import retrofit2.Response

data class FormViewState(
    val parent: ProcessEntry = ProcessEntry(),
    val formFields: List<FieldsData> = emptyList(),
    val processOutcomes: List<OptionsModel> = emptyList(),
    val enabledOutcomes: Boolean = false,
    val listAccountInfo: List<AccountInfoData> = emptyList(),
    val requestForm: Async<ResponseListForm> = Uninitialized,
    val requestProcessDefinition: Async<ResponseListProcessDefinition> = Uninitialized,
    val requestStartWorkflow: Async<ProcessEntry> = Uninitialized,
    val requestOutcomes: Async<Response<Unit>> = Uninitialized,
    val requestSaveForm: Async<Response<Unit>> = Uninitialized,
    val requestFormVariables: Async<ResponseFormVariables> = Uninitialized,
    val requestContent: Async<Entry> = Uninitialized,
    val requestAccountInfo: Async<ResponseAccountInfo> = Uninitialized,
    val requestProfile: Async<ProfileData> = Uninitialized,
    val requestClaimRelease: Async<Response<Unit>> = Uninitialized,
    val request: Async<TaskEntry> = Uninitialized,
) : MavericksState {
    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * update the taskDetailObj params after getting the response from server.
     */
    fun update(response: TaskEntry?): FormViewState {
        if (response == null) return this

        val processEntry = ProcessEntry.with(response)

        return copy(parent = processEntry)
    }

    /**
     * update the single process definition entry
     */
    fun updateSingleProcessDefinition(response: ResponseListProcessDefinition): FormViewState {
        val processEntry = ProcessEntry.with(response.listProcessDefinitions.first(), parent)
        return copy(parent = processEntry)
    }

    fun updateAccountInfo(it: ResponseAccountInfo): FormViewState {
        return copy(listAccountInfo = it.listAccounts)
    }
}
