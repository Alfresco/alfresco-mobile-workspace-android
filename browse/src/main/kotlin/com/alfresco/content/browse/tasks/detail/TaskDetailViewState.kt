package com.alfresco.content.browse.tasks.detail

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ResponseComments
import com.alfresco.content.data.ResponseContents
import com.alfresco.content.data.TaskEntry
import retrofit2.Response

/**
 * Marked as TaskDetailViewState class
 */
data class TaskDetailViewState(
    val parent: TaskEntry?,
    val taskEntry: TaskEntry? = null,
    val listComments: List<CommentEntry> = emptyList(),
    val listContents: List<Entry> = emptyList(),
    val baseEntries: List<Entry> = emptyList(),
    val uploads: List<Entry> = emptyList(),
    val request: Async<TaskEntry> = Uninitialized,
    val requestUpdateTask: Async<TaskEntry> = Uninitialized,
    val requestComments: Async<ResponseComments> = Uninitialized,
    val requestContents: Async<ResponseContents> = Uninitialized,
    val requestAddComment: Async<CommentEntry> = Uninitialized,
    val requestCompleteTask: Async<Response<Unit>> = Uninitialized,
    val requestDeleteContent: Async<Response<Unit>> = Uninitialized
) : MavericksState {

    constructor(target: TaskEntry) : this(parent = target)

    /**
     * update the taskDetailObj params after getting the response from server.
     */
    fun update(response: TaskEntry?): TaskDetailViewState {
        if (response == null) return this

        return copy(parent = response, taskEntry = response)
    }

    /**
     * update the listComments params after getting the response from server.
     */
    fun update(response: ResponseComments?): TaskDetailViewState {
        if (response == null) return this
        return copy(listComments = response.listComments)
    }

    /**
     * update the listContents params after getting the response from server.
     */
    fun update(response: ResponseContents?): TaskDetailViewState {
        if (response == null) return this

        return copyIncludingUploads(response.listContents, uploads)
    }

    /**
     * updating the uploads entries with the server entries.
     */
    fun updateUploads(uploads: List<Entry>): TaskDetailViewState {
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
    ): TaskDetailViewState {
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
        return (uploads + base).distinctBy { if (it.id.isEmpty()) it.boxId else it.id }
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

    fun updateDelete(contentId: String): TaskDetailViewState {
        val filterList = listContents.filter { it.id != contentId }
        return copy(listContents = filterList)
    }
}
