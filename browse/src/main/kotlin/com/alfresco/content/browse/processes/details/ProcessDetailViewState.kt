package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ProcessEntry

/**
 * Marked as ProcessDetailViewState
 */
data class ProcessDetailViewState(
    val parent: ProcessEntry,
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList()
) : MavericksState {

    constructor(target: ProcessEntry) : this(parent = target)

    /**
     * updating the uploads entries with the server entries.
     */
    fun updateUploads(uploads: List<Entry>): ProcessDetailViewState {
        // Merge data only after at least the first page loaded
        // [parent] is a good enough flag for the initial load.
        println("Check Server 6 = $uploads")
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
