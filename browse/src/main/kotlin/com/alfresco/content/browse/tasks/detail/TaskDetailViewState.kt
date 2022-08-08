package com.alfresco.content.browse.tasks.detail

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.TaskEntry

/**
 * Marked as TaskDetailViewState class
 */
data class TaskDetailViewState(
    val taskDetailObj: TaskEntry? = null,
    val request: Async<TaskEntry> = Uninitialized
) : MavericksState {

    constructor(args: TaskDetailsArgs) : this(taskDetailObj = args.taskObj)

    /**
     * update the taskDetailObj params after getting the response from server.
     */
    fun update(response: TaskEntry?): TaskDetailViewState {
        if (response == null) return this

        return copy(taskDetailObj = response)
    }
}
