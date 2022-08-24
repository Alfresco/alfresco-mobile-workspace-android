package com.alfresco.content.browse.tasks.detail

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.ResponseComments
import com.alfresco.content.data.ResponseContents
import com.alfresco.content.data.TaskEntry
import retrofit2.Response

/**
 * Marked as TaskDetailViewState class
 */
data class TaskDetailViewState(
    val parent: TaskEntry?,
    val listComments: List<CommentEntry> = emptyList(),
    val listContents: List<ContentEntry> = emptyList(),
    val request: Async<TaskEntry> = Uninitialized,
    val requestComments: Async<ResponseComments> = Uninitialized,
    val requestContents: Async<ResponseContents> = Uninitialized,
    val requestAddComment: Async<CommentEntry> = Uninitialized,
    val requestCompleteTask: Async<Response<Unit>> = Uninitialized
) : MavericksState {

    constructor(target: TaskEntry) : this(parent = target)
    /**
     * update the taskDetailObj params after getting the response from server.
     */
    fun update(response: TaskEntry?): TaskDetailViewState {
        if (response == null) return this

        return copy(parent = response)
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
        return copy(listContents = response.listContents)
    }
}
