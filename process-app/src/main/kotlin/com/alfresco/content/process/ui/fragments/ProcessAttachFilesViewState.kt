package com.alfresco.content.process.ui.fragments

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.UploadData

data class ProcessAttachFilesViewState(
    val parent: UploadData = UploadData(),
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
) : MavericksState {
    constructor(target: UploadData) : this(parent = target)

    val isProcessInstance: Boolean
        get() = when (parent.process.processInstanceId) {
            null -> false
            else -> true
        }

    val isReadOnlyField: Boolean
        get() = when (parent.field.type) {
            FieldType.READONLY.value(), FieldType.READONLY_TEXT.value() -> true
            else -> false
        }

    /**
     * delete content locally and update UI
     */
    fun deleteUploads(contentId: String): ProcessAttachFilesViewState {
        val listBaseEntries = baseEntries.filter { it.observerID == parent.field.id }.filter { it.id != contentId }
        val listUploads = uploads.filter { it.observerID == parent.field.id }.filter { it.id != contentId }
        return copyIncludingUploads(listBaseEntries, listUploads)
    }

    /**
     * updating the uploads entries with the server entries.
     */
    fun updateUploads(observerId: String, uploads: List<Entry>): ProcessAttachFilesViewState {
        // Merge data only after at least the first page loaded
        // [parent] is a good enough flag for the initial load
        return copyIncludingUploads(
            baseEntries.filter { it.observerID == observerId },
            uploads.filter { it.observerID == observerId },
        )
    }

    private fun copyIncludingUploads(
        entries: List<Entry>,
        uploads: List<Entry>,
    ): ProcessAttachFilesViewState {
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
