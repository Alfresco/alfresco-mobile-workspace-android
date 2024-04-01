package com.alfresco.content.process.ui.fragments

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
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
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
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

    /**
     * delete content locally and update UI
     */
    fun deleteUploads(contentId: String): FormViewState {
        val listBaseEntries = baseEntries.filter { it.id != contentId }
        val listUploads = uploads.filter { it.id != contentId }
        return copyIncludingUploads(listBaseEntries, listUploads)
    }

    /**
     * updating the uploads entries with the server entries.
     */
    fun updateUploads(uploads: List<Entry>): FormViewState {
        // Merge data only after at least the first page loaded
        // [parent] is a good enough flag for the initial load.
        return if (parent != null) {
            copyIncludingUploads(baseEntries, uploads)
        } else {
            copy(uploads = uploads)
        }
    }

    private fun copyIncludingUploads(
        entries: List<Entry>,
        uploads: List<Entry>,
    ): FormViewState {
        val mixedUploads = uploads.transformCompletedUploads()
        val mergedEntries = mergeInUploads(entries, mixedUploads)
        val baseEntries = mergedEntries.filter { !it.isUpload }

        return copy(
            listContents = mergedEntries,
            baseEntries = baseEntries,
            uploads = uploads,
        )
    }

    private fun mergeInUploads(base: List<Entry>, uploads: List<Entry>): List<Entry> {
        return (uploads + base).distinctBy { it.id.ifEmpty { it.boxId } }
    }

    /*
     * Transforms completed uploads into network items, so further interaction with them
     * doesn't require special logic.
     */
    private fun List<Entry>.transformCompletedUploads(): List<Entry> =
        map {
            if (it.isUpload && it.isSynced) {
                // Marking as partial avoids needing to store allowableOperations
                it.copy(isUpload = false, offlineStatus = OfflineStatus.UNDEFINED, isPartial = true)
            } else {
                it
            }
        }
}
