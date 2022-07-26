package com.alfresco.content.browse.tasks

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.TaskState
import com.alfresco.content.data.TaskState.Active
import com.alfresco.content.listview.tasks.TaskListViewState

/**
 * Marked as TasksViewState class
 */
data class TasksViewState(
    val parent: Entry? = null,
    override val taskEntries: List<TaskEntry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponseList> = Uninitialized,
    val baseTaskEntries: List<TaskEntry> = emptyList(),
    val listSortDataChips: List<TaskSortData> = emptyList(),
    val listTaskState: List<TaskState> = emptyList(),
    val displayTask: TaskState = Active,
    val loadItemsCount: Int = 0,
    val page: Int = 0
) : TaskListViewState {

    override val isCompact = false

    override fun copy(_entries: List<TaskEntry>) = copy(taskEntries = _entries)

    /**
     * update the latest response
     */
    fun update(
        response: ResponseList?
    ): TasksViewState {
        if (response == null) return this

        val totalLoadCount: Int

        val taskPageEntries = response.listTask

        val newTaskEntries = if (response.start != 0) {
            totalLoadCount = loadItemsCount.plus(response.size)
            baseTaskEntries + taskPageEntries
        } else {
            totalLoadCount = response.size
            taskPageEntries
        }

        return copy(
            taskEntries = newTaskEntries,
            baseTaskEntries = newTaskEntries,
            loadItemsCount = totalLoadCount,
            hasMoreItems = totalLoadCount < response.total
        )
    }
}
