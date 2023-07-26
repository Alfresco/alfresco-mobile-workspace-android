package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseAccountInfo
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.ResponseListForm
import com.alfresco.content.data.ResponseListProcessDefinition
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.process.models.ProfileData

/**
 * Marked as ProcessDetailViewState
 */
data class ProcessDetailViewState(
    val parent: ProcessEntry?,
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
    val formFields: List<FieldsData> = emptyList(),
    val listTask: List<TaskEntry> = emptyList(),
    val requestStartForm: Async<ResponseListForm> = Uninitialized,
    val requestProfile: Async<ProfileData> = Uninitialized,
    val requestAccountInfo: Async<ResponseAccountInfo> = Uninitialized,
    val requestContent: Async<Entry> = Uninitialized,
    val requestProcessDefinition: Async<ResponseListProcessDefinition> = Uninitialized,
    val requestStartWorkflow: Async<ProcessEntry> = Uninitialized,
    val requestTasks: Async<ResponseList> = Uninitialized,
) : MavericksState {

    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * update the task list related to workflow
     */
    fun updateTasks(response: ResponseList): ProcessDetailViewState {
        return copy(listTask = response.listTask)
    }

    /**
     * update ACS content data in process entry object
     */
    fun updateContent(entry: Entry?): ProcessDetailViewState {
        if (entry == null) {
            return this
        }
        println("data ==  1 :: $entry")
        println("data ==  2 :: ${listContents.size}")

        val list: List<Entry>
        if (listContents.isNotEmpty()) {
            list = listContents.toMutableList()
            list.add(entry)
        } else {
            list = listOf(entry)
        }

        return copy(baseEntries = listOf(entry), listContents = list.distinct())
    }

    /**
     * update the single process definition entry
     */
    fun updateSingleProcessDefinition(response: ResponseListProcessDefinition): ProcessDetailViewState {
        if (parent == null) {
            return this
        }
        val processEntry = ProcessEntry.with(response.listProcessDefinitions.first(), parent)
        return copy(parent = processEntry)
    }

    /**
     * update form fields data
     */
    fun updateFormFields(response: ResponseListForm, processEntry: ProcessEntry): ProcessDetailViewState {
        val formFields = response.fields.first().fields
        return copy(formFields = formFields, parent = ProcessEntry.updateReviewerType(processEntry, formFields))
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
        uploads: List<Entry>,
    ): ProcessDetailViewState {
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
