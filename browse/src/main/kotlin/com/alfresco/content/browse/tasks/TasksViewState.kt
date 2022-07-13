package com.alfresco.content.browse.tasks

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.Tasks
import com.alfresco.content.data.Tasks.Active
import com.alfresco.content.listview.tasks.TaskListViewState

data class TasksViewState(
    val parent: Entry? = null,
    override val taskEntries: List<TaskEntry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponseList> = Uninitialized,
    val displayTask: Tasks = Active
) : TaskListViewState {


    override val isCompact = false

    override fun copy(_entries: List<TaskEntry>): TaskListViewState {
        TODO("Not yet implemented")
    }

    fun update(
        response: ResponseList?
    ): TasksViewState {
        if (response == null) return this

        val pageTasks = response.listTask

        return copy(taskEntries = pageTasks)
    }


}
