package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseListProcessDefinition
import com.alfresco.content.data.ResponseListStartForm
import com.alfresco.content.data.payloads.FieldsData

/**
 * Marked as ProcessDetailViewState
 */
data class ProcessDetailViewState(
    val parent: ProcessEntry?,
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
    val formFields: List<FieldsData> = emptyList(),
    val requestStartForm: Async<ResponseListStartForm> = Uninitialized,
    val requestContent: Async<Entry> = Uninitialized,
    val requestProcessDefinition: Async<ResponseListProcessDefinition> = Uninitialized
) : MavericksState {

    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * update ACS content data in process entry object
     */
    fun updateContent(entry: Entry?): ProcessDetailViewState {
        if (entry == null)
            return this
        return copy(baseEntries = listOf(entry), listContents = listOf(entry))
    }

    /**
     * update the single process definition entry
     */
    fun updateSingleProcessDefinition(response: ResponseListProcessDefinition): ProcessDetailViewState {
        if (parent == null)
            return this
        val processEntry = ProcessEntry.with(response.listProcessDefinitions.first(), parent)
        return copy(parent = processEntry)
    }

    /**
     * update form fields data
     */
    fun updateFormFields(response: ResponseListStartForm): ProcessDetailViewState {
        requireNotNull(parent)
        val formFields = response.fields.first().fields
        return copy(formFields = formFields, parent = ProcessEntry.updateReviewerType(parent, formFields))
    }

    /**
     * delete content locally and update UI
     */
    fun deleteUploads(contentId: String): ProcessDetailViewState {
        val listBaseEntries = baseEntries.filter { it.id != contentId }
        val listUploads = uploads.filter { it.id != contentId }
        return copyIncludingUploads(listBaseEntries, listUploads)
    }

    /**
     * updating the uploads entries with the server entries.
     */
    fun updateUploads(uploads: List<Entry>): ProcessDetailViewState {
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
        uploads: List<Entry>
    ): ProcessDetailViewState {
        val mixedUploads = uploads.transformCompletedUploads()
        val mergedEntries = mergeInUploads(entries, mixedUploads)
        val baseEntries = mergedEntries.filter { !it.isUpload }

        return copy(
            listContents = mergedEntries,
            baseEntries = baseEntries,
            uploads = uploads
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
