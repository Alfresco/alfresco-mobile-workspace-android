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
import com.alfresco.kotlin.FilenameComparator
import com.alfresco.list.merge
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
    val requestDeleteContent: Async<Response<Unit>> = Uninitialized,
    val hasMoreItems: Boolean = false
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

        return copyIncludingUploads(response.listContents, uploads, false)
//        return copy(listContents = response.listContents)
    }

    fun updateUploads(uploads: List<Entry>): TaskDetailViewState {
        // Merge data only after at least the first page loaded
        // [parent] is a good enough flag for the initial load.
        return if (parent != null) {
            copyIncludingUploads(baseEntries, uploads, hasMoreItems)
        } else {
            copy(uploads = uploads)
        }
    }

    private fun copyIncludingUploads(
        entries: List<Entry>,
        uploads: List<Entry>,
        hasMoreItems: Boolean
    ): TaskDetailViewState {
        val mixedUploads = uploads.transformCompletedUploads()
        val mergedEntries = mergeInUploads(entries, mixedUploads, !hasMoreItems)
        val baseEntries = mergedEntries.filter { !it.isUpload }

        return copy(
            listContents = mergedEntries,
            baseEntries = baseEntries,
            uploads = uploads,
            hasMoreItems = hasMoreItems
        )
    }

    private fun mergeInUploads(base: List<Entry>, uploads: List<Entry>, includeRemaining: Boolean): List<Entry> {
        return merge(base, uploads, includeRemainingRight = includeRemaining) { left: Entry, right: Entry ->
            if (left.isFolder || right.isFolder) {
                val cmp = right.isFolder.compareTo(left.isFolder)
                if (cmp == 0) {
                    FilenameComparator.compare(left.name, right.name)
                } else {
                    cmp
                }
            } else {
                FilenameComparator.compare(left.name, right.name)
            }
        }.distinctBy { if (it.id.isEmpty()) it.boxId else it.id }
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
