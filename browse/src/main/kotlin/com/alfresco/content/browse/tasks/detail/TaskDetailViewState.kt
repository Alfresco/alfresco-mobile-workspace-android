package com.alfresco.content.browse.tasks.detail

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.TaskEntry

/**
 * Marked as TaskDetailViewState class
 */
data class TaskDetailViewState(
    val taskID: String,
    val request: Async<TaskEntry> = Uninitialized,
    val taskDetailObj: TaskEntry? = null
) : MavericksState {

    constructor(args: TaskDetailsArgs) : this(args.taskID)

    /**
     * update the taskDetailObj params after getting the response from server.
     */
    fun update(response: TaskEntry?): TaskDetailViewState {
        if (response == null) return this

        return copy(taskDetailObj = response)
    }
}
